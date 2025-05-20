package com.nanit.happywebsocketbirthday.presentation.ipsetup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nanit.happywebsocketbirthday.data.network.WebSocketClient
import com.nanit.happywebsocketbirthday.domain.model.Result
import com.nanit.happywebsocketbirthday.domain.usecase.ConnectToWSUseCase
import com.nanit.happywebsocketbirthday.domain.usecase.DisconnectFromWSUseCase
import com.nanit.happywebsocketbirthday.domain.usecase.ObserveWSConnectionUseCase
import com.nanit.happywebsocketbirthday.domain.usecase.ReceiveBabyInfoWSUseCase
import com.nanit.happywebsocketbirthday.domain.usecase.SendMessageUseCase
import com.nanit.happywebsocketbirthday.isValidIpPortFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException


@HiltViewModel
class IpSetupViewModel @Inject constructor(
    private val observeWSConnectionUseCase: ObserveWSConnectionUseCase,
    private val connectToWSUseCase: ConnectToWSUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val receiveBabyInfoWSUseCase: ReceiveBabyInfoWSUseCase,
    private val disconnectFromWSUseCase: DisconnectFromWSUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(IpSetupScreenState())
    val state: StateFlow<IpSetupScreenState> = _state.asStateFlow()
    private var babyInfoCollectorJob: Job? = null
    private var connectionCollectorJob: Job? = null


    override fun onCleared() {
        babyInfoCollectorJob?.cancel() // Explicitly cancel the collector job
        connectionCollectorJob?.cancel() // Explicitly cancel the collector job
        viewModelScope.launch { // Launch in ViewModel scope since disconnect is suspend
            disconnectFromWSUseCase()
        }
        super.onCleared()
    }

    private fun startBabyInfoCollector() {
        // Check if already running to avoid multiple collectors if init were called again (unlikely for ViewModel)
        if (babyInfoCollectorJob?.isActive == true) return
        // Launch on Dispatchers.IO or Dispatchers.Default for the initial setup
        babyInfoCollectorJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                receiveBabyInfoWSUseCase().collect { result ->
                    Log.d("IpSetupViewModel", "receiveBabyInfoWSUseCase emitted: $result")
                    // IMPORTANT: Updates to _state MUST happen on the Main thread
                    withContext(Dispatchers.Main) {
                        when (result) {
                            is Result.Success -> {
                                val babyInfo = result.data
                                _state.update {
                                    it.copy(
                                        isLoading = false,
                                        navigateToBabyInfoEvent = babyInfo,
                                        babyInfoStatusText = "Received: $babyInfo",
                                    )
                                }
                            }

                            is Result.Error -> {
                                _state.update {
                                    it.copy(isLoading = false, babyInfoStatusText = result.message)
                                }
                            }

                            is Result.Loading -> {
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
            } catch (e: CancellationException) {
                Log.i("IpSetupViewModel", "BabyInfoCollector job was cancelled.", e)
                // It's good to re-throw CancellationException if you don't have specific cleanup
                // In this case, just logging it is fine as the job is meant to be stopped.
                // Or you could update UI state here if needed for cancellation.
                withContext(Dispatchers.Main) {
                    _state.update { it.copy(babyInfoStatusText = "Stopped listening for baby info.") }
                }
            } catch (e: Exception) {
                // Catch any exceptions from the flow collection itself (e.g., if the UseCase throws)
                Log.e("IpSetupViewModel", "Error in receiveBabyInfoWSUseCase collector", e)
                withContext(Dispatchers.Main) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            babyInfoStatusText = "Error collecting baby info: ${e.message}"
                        )
                    }
                }
            }
        }
    }

    // A function to consume the navigation event after navigation
    fun onBabyInfoNavigationHandled() {
        _state.update { it.copy(navigateToBabyInfoEvent = null) }
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

                val sendResult =
                    sendMessageUseCase() // Call the Use Case, get the Result<Unit>

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

    fun onConnectClick() {
        observeConnection()
        val ipAddress = _state.value.ipPort
        // Check if already running to avoid multiple collectors if init were called again (unlikely for ViewModel)
        viewModelScope.launch {
            connectToWSUseCase(ipAddress)
        }
    }

    private fun observeConnection() {
        if (connectionCollectorJob?.isActive == true) return
        // Launch on Dispatchers.IO or Dispatchers.Default for the initial setup
        connectionCollectorJob = viewModelScope.launch {
            observeWSConnectionUseCase()
                .collectLatest { result -> // Collect the Result
                    Log.d("IpSetupViewModel", "connectToWSUseCase emitted: $result")
                    when (result) {
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
                            startBabyInfoCollector()
                        }

                        is WebSocketClient.ConnectionState.Disconnected -> {
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    isConnected = false,
                                    connectionStatusText = "Disconnected: ${result.reason ?: "Unknown reason"}"
                                )
                            }
                            babyInfoCollectorJob?.cancel()
                            Log.d(
                                "IpSetupViewModel",
                                "Disconnected. BabyInfoCollector job cancelled."
                            )
                        }

                        is WebSocketClient.ConnectionState.Failed -> {
                            Log.e(
                                "ViewModelConnectionState",
                                "!!! Connection Failed: ${result.error?.localizedMessage ?: result.error?.message ?: "Unknown error"}",
                                result.error
                            ) // Log failure with error
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    isConnected = false,
                                    connectionStatusText = "Connection Failed: ${result.error?.localizedMessage ?: result.error?.message ?: "Unknown error"}"
                                )
                            }

                            babyInfoCollectorJob?.cancel()
                            Log.d(
                                "IpSetupViewModel",
                                "Connection failed. BabyInfoCollector job cancelled."
                            )
                        }
                    }
                }
        }
    }

    fun onDisconnectClick() {
        viewModelScope.launch {
            disconnectFromWSUseCase()
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