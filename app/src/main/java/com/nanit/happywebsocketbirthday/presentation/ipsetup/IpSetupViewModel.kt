package com.nanit.happywebsocketbirthday.presentation.ipsetup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nanit.happywebsocketbirthday.ValidationResult
import com.nanit.happywebsocketbirthday.data.network.WebSocketClient
import com.nanit.happywebsocketbirthday.domain.model.Result
import com.nanit.happywebsocketbirthday.domain.usecase.ConnectToWSUseCase
import com.nanit.happywebsocketbirthday.domain.usecase.SendMessageUseCase
import com.nanit.happywebsocketbirthday.isValidIpPortFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class IpSetupViewModel @Inject constructor(
    private val connectToWSUseCase: ConnectToWSUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val webSocketClient: WebSocketClient
) : ViewModel() {

    private val _state = MutableStateFlow(IpSetupScreenState())
    val state: StateFlow<IpSetupScreenState> = _state.asStateFlow()


    // Initialize a job to monitor and react to the status of the WebSocket connection
    private var connectionStateCollectorJob: Job? = null

    // Job for collecting and processing incoming BabyInfo
    private var babyInfoCollectorJob: Job? = null

    init {
        // Start collecting connection state when the ViewModel is created
        startConnectionStateCollector()
        startBabyInfoCollector()

    }

    override fun onCleared() {
        Log.d("IpSetupViewModel", "ViewModel cleared. Cancelling jobs.")
        // Disconnect the WebSocket when the ViewModel is cleared
        viewModelScope.launch { // Launch in ViewModel scope since disconnect is suspend
            webSocketClient.disconnect()
        }
        connectionStateCollectorJob?.cancel()
        babyInfoCollectorJob?.cancel()
        super.onCleared()
    }

    private fun startConnectionStateCollector() {
        // Cancel any existing collector job before starting a new one
        connectionStateCollectorJob?.cancel()

        connectionStateCollectorJob = viewModelScope.launch {
            webSocketClient.connectionState.collect { state ->
                Log.d(
                    "ViewModelConnectionState",
                    "--- START State Change --- ViewModel: ${this@IpSetupViewModel}"
                )
                Log.d("ViewModelConnectionState", "Collected ConnectionState: $state")

                // Update the ViewModel state based on the connection state
                _state.update { currentState ->
                    val newState = when (state) {
                        is WebSocketClient.ConnectionState.Idle ->
                            currentState.copy(
                                isLoading = false,
                                isConnected = false,
                                connectionStatusText = "Connection Idle"
                            )

                        is WebSocketClient.ConnectionState.Connecting ->
                            currentState.copy(
                                isLoading = true,
                                isConnected = false,
                                connectionStatusText = "Connecting..."
                            )

                        is WebSocketClient.ConnectionState.Connected ->
                            currentState.copy(
                                isLoading = false,
                                isConnected = true,
                                connectionStatusText = "Connected"
                            )

                        is WebSocketClient.ConnectionState.Disconnected -> {
                            Log.e(
                                "ViewModelConnectionState",
                                "!!! Connection Disconnected: ${state.reason}"
                            ) // Log disconnection specifically
                            currentState.copy(
                                isLoading = false,
                                isConnected = false,
                                connectionStatusText = "Disconnected: ${state.reason ?: "Unknown reason"}"
                            )
                        }

                        is WebSocketClient.ConnectionState.Failed -> {
                            Log.e(
                                "ViewModelConnectionState",
                                "!!! Connection Failed: ${state.error?.localizedMessage ?: state.error?.message ?: "Unknown error"}",
                                state.error
                            ) // Log failure with error
                            currentState.copy(
                                isLoading = false,
                                isConnected = false,
                                connectionStatusText = "Connection Failed: ${state.error?.localizedMessage ?: state.error?.message ?: "Unknown error"}"
                            )
                        }
                    }
                    Log.d(
                        "ViewModelConnectionState",
                        "Updated UI State (relevant parts): isConnected=${newState.isConnected}, connectionStatusText=${newState.connectionStatusText}"
                    )
                    newState
                }
                Log.d("ViewModelConnectionState", "--- END State Change ---")
            }
        }
    }

    private fun startBabyInfoCollector() {
        babyInfoCollectorJob?.cancel() // Cancel any existing collector
        babyInfoCollectorJob = viewModelScope.launch {
            Log.d("IpSetupViewModel", "Starting BabyInfo collector.")
            // *** COLLECT FROM webSocketClient.receiveBabyInfo() HERE ***
            webSocketClient.receiveBabyInfo()
                .collect { result ->
                    Log.d("IpSetupViewModel", "Received baby info result in collector: $result")
                    when (result) {
                        is Result.Success -> {
                            val babyInfo = result.data
                            if (babyInfo != null) {
                                Log.d(
                                    "IpSetupViewModel",
                                    "Successfully parsed and received ApiBabyInfo in collector."
                                )
                                _state.update {
                                    it.copy(
                                        isLoading = false, // Stop loading when info is received
                                        babyInfo = babyInfo, // Indicate reception
                                        babyInfoStatusText = "Received: ${babyInfo}",
                                    )
                                }
                                // If you only expect ONE baby info message and then want to stop
                                // babyInfoCollectorJob?.cancel() // Uncomment this if you only need the first one
                            } else {
                                Log.w(
                                    "IpSetupViewModel",
                                    "Received null BabyInfo in Success state in collector."
                                )
                                _state.update {
                                    it.copy(
                                        isLoading = false, // Stop loading
                                        babyInfoStatusText = "Received empty baby info from server"
                                    )
                                }
                            }
                        }

                        is Result.Error -> {
                            Log.e(
                                "IpSetupViewModel",
                                "Error receiving/parsing BabyInfo in collector: ${result.message}",
                                result.exception
                            )
                            _state.update {
                                it.copy(
                                    isLoading = false, // Stop loading on error
                                    babyInfoStatusText = result.message
                                )
                            }
                        }

                        is Result.Loading -> {
                            // Handle loading state if receiveBabyInfo emits it
                            Log.d(
                                "IpSetupViewModel",
                                "BabyInfo Flow emitting Loading state in collector."
                            )
                            // Consider if this indicates waiting for the *first* message after connect/send
                            _state.update {
                                it.copy(
                                    isLoading = true,
                                    babyInfoStatusText = "Waiting for baby info..."
                                )
                            }
                        }
                    }
                }
        }
    }


    private fun connectToWebSocket(ipAddress: String) {
        viewModelScope.launch {
            // Set loading initially before starting the process
            _state.value =
                _state.value.copy(
                    isLoading = true,
                    connectionStatusText = "Connecting...",
                    isConnected = false
                )

            connectToWSUseCase(ipAddress)
                .collectLatest { result -> // Collect the Result
                    when (result) {
                        is Result.Loading -> {
                            // Update state to indicate loading
                            _state.update {
                                it.copy(
                                    isLoading = true,
                                    connectionStatusText = "Connecting...",
                                    isConnected = false
                                )
                            }
                        }

                        is Result.Success -> {
                            // Connected to Web Socket
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    connectionStatusText = "Connected",
                                    isConnected = true
                                )
                            }
                        }

                        is Result.Error -> {
                            // Handle connection or data error
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    connectionStatusText = "Connection Error: ${result.message}",
                                    isConnected = false
                                )
                            }
                            Log.e("IpSetupViewModel", "Error: ${result.message}", result.exception)
                        }
                    }
                }
        }
    }

    fun onSendMessageClick() {
        // Check if connected before sending
        if (_state.value.isConnected) {
            viewModelScope.launch {
                Log.d("IpSetupViewModel", "Attempting to send message.")
                // Update state to indicate sending/loading
                _state.update {
                    it.copy(
                        isLoading = true,
                        messageStatusText = "Sending message..."
                    )
                }

                val sendResult = sendMessageUseCase() // Call the Use Case, get the Result<Unit>

                when (sendResult) {
                    is Result.Success -> {
                        Log.d("IpSetupViewModel", "Message sent successfully.")
                        _state.update {
                            it.copy(
                                isLoading = false,
                                messageStatusText = "Message sent successfully."
                            )
                        }
                    }

                    is Result.Error -> {
                        Log.e(
                            "IpSetupViewModel",
                            "Error sending message: ${sendResult.message}",
                            sendResult.exception
                        )
                        // Update state to show the error and stop loading
                        _state.update {
                            it.copy(
                                isLoading = false,
                                messageStatusText = "Failed to send message: ${sendResult.message}"
                            )
                        }
                    }
                    // No Loading state from a suspend function returning Result
                    is Result.Loading -> {
                        _state.update {
                            it.copy(
                                isLoading = true,
                                messageStatusText = "Loading..."
                            )
                        }
                    }
                }
            }
        } else {
            Log.w("IpSetupViewModel", "Cannot send message, not connected.")
            _state.update { it.copy(messageStatusText = "Cannot send message: Not connected.") }
        }
    }

    // Resets the navigation trigger state
    fun resetNavigationTriggerState() {
        _state.update { currentState ->
            currentState.copy(
                messageStatusText = "Send message",
                babyInfoStatusText = "Waiting for new baby info",
                babyInfo = null
            )
        }
    }

    private fun onValidationResultChanged(newValidationResult: ValidationResult) {
        _state.value = _state.value.copy(validationResult = newValidationResult)
    }


    fun onConnectClick() {
        // Get the current IP and port from the ViewModel's state
        val currentIpPort = _state.value.ipPort

        // Perform the validation
        val validationResult = isValidIpPortFormat(currentIpPort)

        // Update the validation state within the ViewModel
        onValidationResultChanged(validationResult)

        // If validation is successful and the IP/Port is not empty, proceed with connection
        if (validationResult.isValid && currentIpPort.isNotEmpty()) {
            // Call the existing connectToWebSocket function with the validated IP
            connectToWebSocket(currentIpPort)
        } else {
            Log.d("IpSetupViewModel", "Connect button clicked with invalid IP/Port: $currentIpPort")
            // The UI composable observing state.validationResult will show the error message
        }
    }

    // Function to handle changes to the IP/Port input
    fun onIpPortChanged(newIpPort: String) {
        // Update the ipPort state immediately
        _state.update { it.copy(ipPort = newIpPort) }

        // Perform validation on the new value
        val validationResult = isValidIpPortFormat(newIpPort)

        // Update the validation result state based on the validation
        _state.update { it.copy(validationResult = validationResult) }
    }
}