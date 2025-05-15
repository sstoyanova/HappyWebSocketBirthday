package com.nanit.happywebsocketbirthday.presentation.ipsetup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nanit.happywebsocketbirthday.ValidationResult
import com.nanit.happywebsocketbirthday.data.network.WebSocketClient
import com.nanit.happywebsocketbirthday.domain.model.Result
import com.nanit.happywebsocketbirthday.domain.usecase.ConnectToWSUseCase
import com.nanit.happywebsocketbirthday.domain.usecase.DisconnectFromWSUseCase
import com.nanit.happywebsocketbirthday.domain.usecase.ReceiveBabyInfoWSUseCase
import com.nanit.happywebsocketbirthday.domain.usecase.SendMessageUseCase
import com.nanit.happywebsocketbirthday.isValidIpPortFormat
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val receiveBabyInfoWSUseCase: ReceiveBabyInfoWSUseCase,
    private val disconnectFromWSUseCase: DisconnectFromWSUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(IpSetupScreenState())
    val state: StateFlow<IpSetupScreenState> = _state.asStateFlow()

    init {
        //start listening for baby info when the ViewModel is initialized
        startBabyInfoCollector()
    }

    override fun onCleared() {
        Log.d("IpSetupViewModel", "ViewModel cleared. Cancelling jobs.")
        // Disconnect the WebSocket when the ViewModel is cleared
        viewModelScope.launch { // Launch in ViewModel scope since disconnect is suspend
            disconnectFromWSUseCase()
        }
        super.onCleared()
    }

    private fun startBabyInfoCollector() {
        viewModelScope.launch {
            Log.d("IpSetupViewModel", "receiveBabyInfoWSUseCase")
            receiveBabyInfoWSUseCase().collect { result -> // Collect each emission from the Flow
                Log.d("IpSetupViewModel", "Received baby info result in collector: $result")
                when (result) {
                    is Result.Success -> {
                        val babyInfo = result.data
                        Log.d(
                            "IpSetupViewModel",
                            "Successfully parsed and received ApiBabyInfo in collector."
                        )
                        _state.update {
                            it.copy(
                                isLoading = false, // Stop loading when info is received
                                navigateToBabyInfoEvent = babyInfo, // Set the event to trigger navigation
                                babyInfoStatusText = "Received: ${babyInfo}",
                            )
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
    // A function to consume the navigation event after navigation
    fun onBabyInfoNavigationHandled() {
        _state.update { it.copy(navigateToBabyInfoEvent = null) }
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
                            when (result.data) {
                                is WebSocketClient.ConnectionState.Idle ->
                                    _state.update {
                                        it.copy(
                                            isLoading = false,
                                            isConnected = false,
                                            connectionStatusText = "Connection Idle"
                                        )
                                    }

                                is WebSocketClient.ConnectionState.Connecting ->
                                    _state.update {
                                        it.copy(
                                            isLoading = true,
                                            isConnected = false,
                                            connectionStatusText = "Connecting..."
                                        )
                                    }

                                is WebSocketClient.ConnectionState.Connected -> {
                                    _state.update {
                                        it.copy(
                                            isLoading = false,
                                            isConnected = true,
                                            connectionStatusText = "Connected"
                                        )
                                    }
                                }

                                is WebSocketClient.ConnectionState.Disconnected ->
                                    _state.update {
                                        it.copy(
                                            isLoading = false,
                                            isConnected = false,
                                            connectionStatusText = "Disconnected: ${result.data.reason ?: "Unknown reason"}"
                                        )
                                    }

                                is WebSocketClient.ConnectionState.Failed -> {
                                    Log.e(
                                        "ViewModelConnectionState",
                                        "!!! Connection Failed: ${result.data.error?.localizedMessage ?: result.data.error?.message ?: "Unknown error"}",
                                        result.data.error
                                    ) // Log failure with error
                                    _state.update {
                                        it.copy(
                                            isLoading = false,
                                            isConnected = false,
                                            connectionStatusText = "Connection Failed: ${result.data.error?.localizedMessage ?: result.data.error?.message ?: "Unknown error"}"
                                        )
                                    }
                                }
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
                            Log.e(
                                "IpSetupViewModel",
                                "Error: ${result.message}",
                                result.exception
                            )
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
            Log.d(
                "IpSetupViewModel",
                "Connect button clicked with invalid IP/Port: $currentIpPort"
            )
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