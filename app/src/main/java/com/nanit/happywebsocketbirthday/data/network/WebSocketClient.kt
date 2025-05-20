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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.ConnectException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

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

    // A StateFlow to broadcast connection state (initial state: Idle)
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    // Use a SharedFlow to broadcast incoming messages
    private val _incomingMessages = MutableSharedFlow<Result<String>>(
        replay = 0, // No replay for messages
        extraBufferCapacity = 1, // Add a positive buffer capacity
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val incomingMessages: SharedFlow<Result<String>> = _incomingMessages.asSharedFlow()

    suspend fun connect(ipAddress: String) {
        if (session?.isActive == true) {
            Log.d("WebSocketClient", "Already connected.")
            _connectionState.emit(ConnectionState.Connected)
            return
        }
        if (sessionJob?.isActive == true) {
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
        // This job's lifetime is the lifetime of the connection management attempt
        sessionJob = client.launch {
            var currentSession: WebSocketSession? =
                null // Use a local variable for the session reference during setup
            try {
                // Call webSocketSession to get the session object
                currentSession = client.webSocketSession( // Assign to local variable first
                    method = HttpMethod.Get,
                    host = ip,
                    port = port,
                    path = "/nanit"
                )

                session =
                    currentSession // Assign to the class property ONLY after successful session creation
                Log.d("WebSocketClient", "WebSocket session created to: ws://$ip:$port/nanit")
                _connectionState.value = ConnectionState.Connected // Emit Connected State

                // Launch a separate coroutine to handle incoming messages
                // This child job will automatically be cancelled if the parent sessionJob is cancelled
                val receiveJob = launch {
                    try {
                        // Use a for loop for graceful channel consumption until it's closed
                        for (frame in currentSession.incoming) {
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
                        Log.d(
                            "WebSocketClient",
                            "Receive loop finished gracefully (channel closed)."
                        )
                    } catch (e: CancellationException) {
                        Log.d("WebSocketClient", "Receive job cancelled.")
                        throw e // Re-throw
                    } catch (e: Exception) {
                        Log.e("WebSocketClient", "Exception in receive loop", e)
                        if (currentSession.isActive) { // Check local attempt
                            _incomingMessages.emit(
                                Result.Error(
                                    "Error processing received message: ${e.message}",
                                    e
                                )
                            )
                        } else {
                            Log.e(
                                "WebSocketClient",
                                "Receive loop error when currentSessionAttempt is inactive."
                            )
                        }
                    } finally {
                        Log.d("WebSocketClient", "Receive job finally block reached.")
                    }
                }

                // Keep the parent job alive by joining the receiveJob.
                // The parent will now suspend until the receiveJob completes.
                // The receiveJob completes when the session's incoming channel is closed or receives an error.
                Log.d("WebSocketClient", "Connect job joining receiveJob...")
                receiveJob.join()
                Log.d("WebSocketClient", "Connect job finished joining receiveJob.")

            } catch (e: ConnectException) { // This will now refer to java.net.ConnectException
                Log.e("WebSocketClient", "Connection failed (java.net.ConnectException)", e)
                _connectionState.value = ConnectionState.Failed(e)
            } catch (e: io.ktor.network.sockets.SocketTimeoutException) { // Catch Ktor's timeout specifically if needed
                Log.e(
                    "WebSocketClient",
                    "Connection attempt timed out (Ktor SocketTimeoutException)",
                    e
                )
                _connectionState.value =
                    ConnectionState.Failed(e) // Or a specific timeout error state
            } catch (e: CancellationException) {
                Log.d("WebSocketClient", "Connect job was cancelled.")
                throw e
            } catch (e: Exception) { // Catch other potential Ktor or general exceptions
                Log.e(
                    "WebSocketClient",
                    "Exception during WebSocket setup or operation: ${e.javaClass.name}",
                    e
                )
                _connectionState.value = ConnectionState.Failed(e)
            } finally {
                Log.d(
                    "WebSocketClient",
                    "Connect job's MAIN finally. This job's active status: ${this.coroutineContext.isActive}"
                )

                val jobWasCancelled = !this.coroutineContext.isActive // Use this job's context

                if (currentSession != null && currentSession.isActive) {
                    Log.d("WebSocketClient", "Connect finally: Closing currentSessionAttempt.")
                    try {
                        currentSession.close(
                            CloseReason(
                                CloseReason.Codes.NORMAL,
                                "Client closing connection in finally"
                            )
                        )
                    } catch (e: Exception) {
                        Log.e(
                            "WebSocketClient",
                            "Error closing currentSessionAttempt in finally",
                            e
                        )
                    }
                }

                val disconnectReason = when {
                    jobWasCancelled -> "Connection job cancelled"
                    currentSession?.isActive == false -> "Session inactive or closed"
                    currentSession == null && _connectionState.value !is ConnectionState.Failed -> "Session never established"
                    else -> {
                        Log.w(
                            "WebSocketClient",
                            "Disconnect reason UNKNOWN. jobWasCancelled=$jobWasCancelled, currentSessionAttempt.isActive=${currentSession?.isActive}, _connectionState=${_connectionState.value}"
                        )
                        "Unknown reason"
                    }
                }

                if (_connectionState.value !is ConnectionState.Failed) {
                    val currentDisconnectedState =
                        _connectionState.value as? ConnectionState.Disconnected
                    if (currentDisconnectedState == null || (currentDisconnectedState.reason != "Disconnected by user" && currentDisconnectedState.reason != disconnectReason)) {
                        Log.d(
                            "WebSocketClient",
                            "Connect finally: Emitting Disconnected. Reason: $disconnectReason. Prev state: ${_connectionState.value}"
                        )
                        _connectionState.value = ConnectionState.Disconnected(disconnectReason)
                    } else {
                        Log.d(
                            "WebSocketClient",
                            "Connect finally: State already Disconnected. Current reason: ${currentDisconnectedState.reason}. New reason attempt: $disconnectReason"
                        )
                    }
                } else {
                    Log.d(
                        "WebSocketClient",
                        "Connect finally: State already Failed. Not emitting Disconnected for reason: $disconnectReason"
                    )
                }

                // Critical: Only nullify class members if this job is the one currently assigned.
                if (this@WebSocketClient.sessionJob === this.coroutineContext.job) {
                    Log.d(
                        "WebSocketClient",
                        "Connect finally: Clearing class session and sessionJob references."
                    )
                    this@WebSocketClient.session = null
                    this@WebSocketClient.sessionJob = null
                } else {
                    Log.d(
                        "WebSocketClient",
                        "Connect finally: This job is not the current class sessionJob. Not clearing global references."
                    )
                }
            }
        }
    }

    // Function to send a message
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
        // Collect from the central incomingMessages flow and map/filter
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

    /**
     * Represents the information about a baby received from the API.
     *
     * This data class is used to deserialize the JSON response containing baby details.
     * It includes the baby's name, date of birth, and the selected theme.
     */
    @Serializable
    private data class ApiBabyInfo(
        @SerialName("name") val name: String,
        @SerialName("dob") val dob: Long,
        @SerialName("theme") val theme: String
    ) {

        fun toDomain(): BabyInfo {
            return BabyInfo(name, dob, theme)
        }

        companion object {
            fun fromJson(jsonString: String): ApiBabyInfo {
                return Json.decodeFromString(serializer(), jsonString)
            }
        }
    }
}