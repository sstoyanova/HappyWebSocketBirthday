package com.nanit.happywebsocketbirthday.data.network

import android.util.Log
import com.nanit.happywebsocketbirthday.domain.model.BabyInfo
import com.nanit.happywebsocketbirthday.domain.model.Result
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.ConnectException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext

@Singleton
class WebSocketClient @Inject constructor(
    private val client: HttpClient
) {
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

    sealed class ConnectionState {
        data object Idle : ConnectionState() // Initial state
        data object Connecting : ConnectionState()
        data object Connected : ConnectionState()
        data class Disconnected(val reason: String?) : ConnectionState()
        data class Failed(val error: Throwable?) : ConnectionState()
    }

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
        sessionJob = client.launch(coroutineContext) {
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

                    } catch (e: Exception) {
                        Log.e("WebSocketClient", "Exception in receive loop", e)
                        // Handle exceptions during reception. This might or might not mean the
                        // entire session is invalid, depending on the exception.
                        if (currentSession.isActive == true) {
                            Log.d(
                                "WebSocketClient",
                                "Receive loop error while session still active."
                            )
                            _incomingMessages.emit(
                                Result.Error(
                                    "Error processing received message: ${e.message}",
                                    e
                                )
                            )
                        } else {
                            Log.e(
                                "WebSocketClient",
                                "Receive loop error when session is inactive. Likely session termination reason.",
                                e
                            )
                            // If session inactive, parent finally will handle.
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


            } catch (e: ConnectException) {
                Log.e("WebSocketClient", "Connection failed", e)
                _connectionState.value = ConnectionState.Failed(e)

            } catch (e: Exception) {
                // Catch any other exceptions during session setup or while waiting for the receive job to join
                Log.e(
                    "WebSocketClient",
                    "Exception during WebSocket setup or joining receive job",
                    e
                )
                _connectionState.value = ConnectionState.Failed(e)

            } finally {
                // This finally block is reached when the parent sessionJob:
                // 1. Completes after receiveJob.join() finishes (meaning the session's incoming channel closed).
                // 2. Catches an exception in its try block.
                // 3. Is cancelled externally.
                Log.d(
                    "WebSocketClient",
                    "Connect job finally block reached. Cleaning up references."
                )
                Log.d(
                    "WebSocketClient",
                    "Connect job finally: session is null: ${session == null}, session?.isActive: ${session?.isActive}"
                )
                Log.d(
                    "WebSocketClient",
                    "Connect job finally: sessionJob is null: ${sessionJob == null}, sessionJob?.isActive: ${sessionJob?.isActive}, sessionJob?.isCancelled: ${sessionJob?.isCancelled}"
                )


                // Attempt to close the session if it's still active.
                // Use the local currentSession reference as the class-level session might already be nulled by disconnect()
                // Check if the session is not null AND isActive before attempting close
                if (currentSession != null && currentSession.isActive) {
                    Log.d(
                        "WebSocketClient",
                        "Session still active in finally. Attempting graceful close."
                    )
                    try {
                        currentSession.close() // Gracefully close the Ktor session
                    } catch (e: Exception) {
                        Log.e("WebSocketClient", "Error closing session in finally", e)
                    }
                }

                // Determine the final state and emit Disconnected if it's not already Failed.
                // If the sessionJob was cancelled, we can infer a disconnection often due to cancellation.
                // If the session became inactive without the state being set to Failed, assume a disconnection.
                val disconnectReason = when {
                    sessionJob?.isCancelled == true -> "Job cancelled"
                    currentSession?.isActive == false -> "Session inactive or closed"
                    else -> "Unknown reason" // Should ideally not be reached if session is closed
                }


                // Emit Disconnected state ONLY if not already Failed
                if (_connectionState.value !is ConnectionState.Failed) {
                    Log.d(
                        "WebSocketClient",
                        "Emitting ConnectionState.Disconnected. Reason: $disconnectReason"
                    )
                    _connectionState.value = ConnectionState.Disconnected(disconnectReason)
                } else {
                    Log.d(
                        "WebSocketClient",
                        "ConnectionState is already Failed, not emitting Disconnected in finally."
                    )
                }


                // Clear the references. This should be the final cleanup for this job.
                Log.d("WebSocketClient", "Clearing session and sessionJob references in finally.")
                session = null
                sessionJob = null
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
    }

    // Function to disconnect the WebSocket
    suspend fun disconnect() {
        Log.d("WebSocketClient", "Disconnect requested.")
        try {
            if (session?.isActive == true) {
                session?.close() // Close the WebSocket gracefully
                Log.d("WebSocketClient", "disconect: session?.close()")
            }
            sessionJob?.cancel() // Cancel the job to stop consuming messages and clean up
        } catch (e: Exception) {
            Log.e("WebSocketClient", "Error during disconnect", e)
        } finally {
            Log.d("WebSocketClient", "disconnect: session = null")
            session = null
            sessionJob = null
            Log.d("WebSocketClient", "Emitting ConnectionState.Disconnected (Disconnected by user)")
            _connectionState.emit(ConnectionState.Disconnected("Disconnected by user")) // Ensure state is updated
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
            return BabyInfo(name,dob,theme)
        }

        companion object {
            fun fromJson(jsonString: String): ApiBabyInfo {
                return Json.decodeFromString(serializer(), jsonString)
            }
        }
    }
}