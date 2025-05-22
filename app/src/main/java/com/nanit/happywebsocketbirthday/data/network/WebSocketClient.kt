package com.nanit.happywebsocketbirthday.data.network

import android.util.Log
import com.nanit.happywebsocketbirthday.domain.model.BabyInfo
import com.nanit.happywebsocketbirthday.domain.model.Result
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.http.HttpMethod
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readReason
import io.ktor.websocket.readText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import java.net.ConnectException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.coroutineContext

@Singleton
class WebSocketClient @Inject constructor(
    private val client: HttpClient
) {
    sealed class ConnectionState {
        data object Idle : ConnectionState() // Initial state
        data object Connecting : ConnectionState()
        data object Connected : ConnectionState()
        data class Disconnected(val reason: String?) : ConnectionState()
        data class Failed(val error: Throwable?) : ConnectionState()
    }

    private var session: WebSocketSession? = null
    private var sessionJob: Job? = null

    // StateFlow to broadcast connection state (initial state: Idle)
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    // SharedFlow to broadcast incoming messages
    private val _incomingMessages = MutableSharedFlow<Result<String>>(
        replay = 0, // No replay for messages
        extraBufferCapacity = 1, // Add a positive buffer capacity
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val incomingMessages: SharedFlow<Result<String>> = _incomingMessages.asSharedFlow()

    suspend fun connect(ipAddress: String) {
        if (session?.isActive == true) { // the connection is active and is connected
            Log.d("WebSocketClient", "Already connected.")
            _connectionState.emit(ConnectionState.Connected)
            return
        }
        if (sessionJob?.isActive == true) { // a connection attempt is active
            Log.d("WebSocketClient", "Emitting ConnectionState.Connecting")
            Log.d("WebSocketClient", "Connection attempt already in progress.")
            _connectionState.emit(ConnectionState.Connecting)
            return
        }

        _connectionState.emit(ConnectionState.Connecting)

        val (ip, port) = try {
            ipAddress.split(":").let { Pair(it[0], it[1].toInt()) }
        } catch (e: Exception) {
            Log.e("WebSocketClient", "Invalid IP address format: $ipAddress", e)
            _connectionState.emit(ConnectionState.Failed(IllegalArgumentException("Invalid IP address or port format.")))
            return
        }

        // Launch a job that will manage the WebSocket session lifecycle
        sessionJob = client.launch {
            manageWebSocketSession(ip, port)
        }
    }

    private suspend fun manageWebSocketSession(ip: String, port: Int) {
        var currentLocalSession: WebSocketSession? =
            null // Local variable for the session in this attempt
        val thisJob = coroutineContext.job // Get the Job of this manageWebSocketSession coroutine

        try {
            // Call webSocketSession to get the session object
            currentLocalSession = establishSession(ip, port)

            session =
                currentLocalSession // Assign to the class property ONLY after successful session creation
            Log.d("WebSocketClient", "WebSocket session established to: ws://$ip:$port/nanit")
            _connectionState.emit(ConnectionState.Connected) // Emit Connected State

            processIncomingMessages(currentLocalSession) // This will suspend until messages stop or error
        } catch (e: ConnectException) { // This will now refer to java.net.ConnectException
            Log.e("WebSocketClient", "Connection failed (java.net.ConnectException)", e)
            _connectionState.emit(ConnectionState.Failed(e))
        } catch (e: io.ktor.network.sockets.SocketTimeoutException) {
            Log.e("WebSocketClient", "Connection timed out (Ktor SocketTimeoutException)", e)
            _connectionState.emit(ConnectionState.Failed(e))
        } catch (e: CancellationException) {
            Log.d("WebSocketClient", "Connect job was cancelled.")
            throw e // Re-throw to ensure the job is properly cancelled
        } catch (e: Exception) { // Catch other potential Ktor or general exceptions
            Log.e("WebSocketClient", "Exception during WS setup/operation: ${e.javaClass.name}", e)
            _connectionState.emit(ConnectionState.Failed(e))
        } finally {
            val jobWasCancelled =
                !thisJob.isActive // Check the specific job for this session attempt
            Log.d(
                "WebSocketClient",
                "Connect job's MAIN finally. This job's active status: ${thisJob.isActive}"
            )

            performGeneralCleanupLogic(currentLocalSession, jobWasCancelled)

            // Only nullify class members if this job is the one currently assigned.
            if (this@WebSocketClient.sessionJob === thisJob) { // Compare with the Job of this coroutine
                Log.d("WebSocketClient", "Connect finally: Clearing CLASS sessionJob reference.")
                this@WebSocketClient.sessionJob = null
            } else {
                // This logic prevents an older, finishing connection attempt (jobA)
                // from incorrectly nullifying the sessionJob that belongs to a newer,
                // potentially active or preferred connection attempt (jobB).
                Log.d(
                    "WebSocketClient",
                    "Connect finally: This job ($thisJob) is not the current class sessionJob (${this@WebSocketClient.sessionJob}). Not clearing global sessionJob."
                )
            }

            // If the global session was the one managed by *this* job, clear it.
            if (this@WebSocketClient.session === currentLocalSession) {
                this@WebSocketClient.session = null
            }
        }
    }

    private suspend fun performGeneralCleanupLogic(
        sessionToClean: WebSocketSession?,
        jobWasCancelled: Boolean
    ) {
        if (sessionToClean != null && sessionToClean.isActive) {
            Log.d("WebSocketClient", "Connect finally: Closing currentSessionAttempt.")
            try {
                sessionToClean.close(
                    CloseReason(
                        CloseReason.Codes.NORMAL,
                        "Client closing connection"
                    )
                )
            } catch (e: Exception) {
                Log.e("WebSocketClient", "Error closing session in cleanup", e)
            }
        }

        val isInFailState = _connectionState.value is ConnectionState.Failed
        val isInConnectedState = _connectionState.value is ConnectionState.Connected

        val disconnectReason = when {
            jobWasCancelled -> "Connection job cancelled"
            // If sessionToClean is null, it means we never even established it successfully in this attempt
            sessionToClean == null && !isInFailState -> "Session never established"
            // If sessionToClean is not null but became inactive (and wasn't due to a reported failure)
            sessionToClean?.isActive == false && !isInFailState -> "Session disconnected"
            // If we were connected and are now cleaning up without a specific cancellation or failure
            isInConnectedState -> "Connection closed" // Generic reason if connected then cleaned
            else -> {
                Log.w(
                    "WebSocketClient",
                    "Disconnect reason UNKNOWN during cleanup. jobWasCancelled=$jobWasCancelled, sessionActive=${sessionToClean?.isActive}, connectionState=${_connectionState.value}"
                )
                "Unknown reason"
            }
        }

        // Only transition to Disconnected if not already Failed and not already appropriately Disconnected
        if (!isInFailState) {
            val isInDisconnectedState = _connectionState.value as? ConnectionState.Disconnected
            if (isInDisconnectedState == null || (isInDisconnectedState.reason != "Disconnected by user" && isInDisconnectedState.reason != disconnectReason)) {
                Log.d(
                    "WebSocketClient",
                    "Cleanup: Emitting Disconnected. Reason: $disconnectReason. Prev state: ${_connectionState.value}"
                )
                _connectionState.emit(ConnectionState.Disconnected(disconnectReason))
            } else {
                Log.d(
                    "WebSocketClient",
                    "Cleanup: State already Disconnected. Current reason: ${isInDisconnectedState.reason}. New reason attempt: $disconnectReason"
                )
            }
        } else {
            Log.d(
                "WebSocketClient",
                "Cleanup: State already Failed. Not emitting Disconnected for reason: $disconnectReason"
            )
        }

        // Critical: Only nullify class members if this job is the one currently assigned.
        if (this@WebSocketClient.session === sessionToClean && sessionToClean != null) {
            Log.d(
                "WebSocketClient",
                "Cleanup: Nullifying class 'session' property as it matches the cleaned session."
            )
            this@WebSocketClient.session = null
        }
    }


    private suspend fun processIncomingMessages(session: WebSocketSession) =
        coroutineScope {
            // `this` is a new CoroutineScope, child of the calling scope (connect job).
            // This new scope will complete when all its children (like the job below) complete.
            // If the outer scope (connect job from manageWebSocketSession) is cancelled,
            // this scope and this job below will also be cancelled.
            launch {
                try {
                    // Use a for loop for graceful channel consumption until it's closed
                    for (frame in session.incoming) {
                        when (frame) {
                            is Frame.Text -> {
                                val receivedText = frame.readText()
                                Log.d("WebSocketClient", "Received message: $receivedText")
                                _incomingMessages.emit(Result.Success(receivedText))
                            }

                            is Frame.Close -> {
                                Log.d(
                                    "WebSocketClient",
                                    "Received Close frame: ${frame.readReason()}"
                                )
                            }

                            else -> {
                                Log.d(
                                    "WebSocketClient",
                                    "Received non-text frame: ${frame.frameType.name}"
                                )
                            }
                        }
                    }
                    // This block is reached if the incoming channel closes gracefully
                    Log.d("WebSocketClient", "Receive loop finished gracefully (channel closed).")
                } catch (e: CancellationException) {
                    Log.d("WebSocketClient", "Receive job cancelled.")
                    throw e // Re-throw
                } catch (e: Exception) {
                    Log.e("WebSocketClient", "Exception in receive loop", e)
                    if (session.isActive) { // Check local attempt
                        _incomingMessages.emit(
                            Result.Error(
                                "Error processing received message: ${e.message}",
                                e
                            )
                        )
                    } else {
                        Log.e(
                            "WebSocketClient",
                            "Error in message processing - session is inactive."
                        )
                    }
                } finally {
                    Log.d("WebSocketClient", "Message processing job finally block.")
                }
            }
            // The parent will suspend until the job above completes.
            // The job above completes when the session's incoming channel is closed or receives an error.
        }

    private suspend fun establishSession(ip: String, port: Int): WebSocketSession {
        Log.d(
            "WebSocketClient",
            "Attempting to establish WebSocket session to: ws://$ip:$port/nanit"
        )
        return client.webSocketSession( // Assign to local variable first
            method = HttpMethod.Get,
            host = ip,
            port = port,
            path = "/nanit"
        )
    }

    suspend fun sendMessage(message: String): Result<Unit> {
        try {
            if (session?.isActive == true) {
                session?.send(Frame.Text(message))
                Log.d("WebSocketClient", "Message sent: $message")
                return Result.Success(Unit)
            } else {
                Log.w("WebSocketClient", "Cannot send message: WebSocket session is not active.")
                return Result.Error(message = " session?.isActive = false")
            }
        } catch (e: Exception) {
            Log.e("WebSocketClient", "Error sending message", e)
            return Result.Error(" Error sending message", e)
        }
    }

    // Flow to collect single BabyInfo
    fun receiveBabyInfo(): Flow<Result<BabyInfo>> {
        // Collect from the central incomingMessages flow and map
        return incomingMessages
            .map { result -> // Map the Result<String>
                when (result) {
                    is Result.Success -> {
                        try {
                            // Attempt to parse the received text as ApiBabyInfo
                            val apiBabyInfo = ApiBabyInfo.fromJson(result.data)
                            Result.Success(apiBabyInfo.toDomain()) // Map to BabyInfo), Wrap successful parsing in Success
                        } catch (e: Exception) {
                            // Log parsing errors and emit an Error Result
                            Log.d("WebSocketClient", "Parsing error for message: ${result.data}", e)
                            Result.Error(
                                "Failed to parse baby info",
                                e
                            ) // Emit Error on parsing failure
                        }
                    }

                    is Result.Error -> result // Pass through existing errors from message reception
                    is Result.Loading -> Result.Loading
                }
            }
            .flowOn(Dispatchers.Default) // Perform map (including JSON parsing) on a background thread
    }

    // Function to disconnect the WebSocket
    suspend fun disconnect() {
        Log.d("WebSocketClient", "Disconnect requested.")
        // Capture references before nulling, to ensure we operate on the correct instances
        val jobToCancel = this.sessionJob
        val sessionToClose = this.session

        // Null out shared references early to prevent other operations from using them.
        this.sessionJob = null
        this.session = null

        try {
            if (sessionToClose?.isActive == true) {
                Log.d("WebSocketClient", "disconnect: sessionToClose.close()")
                sessionToClose.close()
            }
            // Cancel the job and WAIT for its completion (including its finally block)
            jobToCancel?.cancelAndJoin()
            Log.d("WebSocketClient", "disconnect: jobToCancel.cancelAndJoin() completed.")

        } catch (e: Exception) {
            Log.e("WebSocketClient", "Error during disconnect's core logic", e)
        } finally {
            // This finally is for the disconnect() method itself.
            // The sessionJob's finally block should be the one setting the detailed disconnected state.
            // This one ensures a general "Disconnected by user" if nothing else has set it.
            if (_connectionState.value !is ConnectionState.Disconnected && _connectionState.value !is ConnectionState.Failed) {
                Log.d(
                    "WebSocketClient",
                    "disconnect method finally: Emitting ConnectionState.Disconnected (Disconnected by user)"
                )
                _connectionState.emit(ConnectionState.Disconnected("Disconnected by user"))
            } else {
                Log.d(
                    "WebSocketClient",
                    "disconnect method finally: State already Disconnected or Failed. Current: ${_connectionState.value}"
                )
            }
        }
    }
}