package com.nanit.happywebsocketbirthday.presentation.ipsetup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nanit.happywebsocketbirthday.ValidationResult
import com.nanit.happywebsocketbirthday.domain.usecase.GetBabyInfoUseCase
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
    private val getBabyInfoUseCase: GetBabyInfoUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(IpSetupScreenState())
    val state: StateFlow<IpSetupScreenState> = _state.asStateFlow()

    fun connectToWebSocket(ipAddress: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            getBabyInfoUseCase(ipAddress)
                .collectLatest { babyInfo ->
                    if (babyInfo != null) {
                        // Baby info received and saved by the Use Case/Repository
                        _state.update {
                            it.copy(
                                isLoading = false,
                                babyInfoReceived = true,
                                errorMessage = null,
                                babyInfo = babyInfo // Store received babyInfo in the state
                            )
                        }
                    } else {
                        // Handle connection or data error
                        _state.update {
                            it.copy(
                                isLoading = false,
                                babyInfoReceived = false,
                                errorMessage = "Failed to get baby info.",
                                babyInfo = null
                            )
                        }
                    }
                }
        }
    }

    // Resets the navigation trigger state
    fun resetNavigationTriggerState() {
        _state.update { currentState ->
            currentState.copy(
                babyInfoReceived = false,
                babyInfo = null // Reset babyInfo
            )
        }
    }

    fun updateIpPort(newIpPort: String) {
        _state.value = _state.value.copy(ipPort = newIpPort)
    }

    fun updateValidationResult(newValidationResult: ValidationResult) {
        _state.value = _state.value.copy(validationResult = newValidationResult)
    }
}