package com.nanit.happywebsocketbirthday.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nanit.happywebsocketbirthday.ValidationResult
import com.nanit.happywebsocketbirthday.domain.model.BabyInfo
import com.nanit.happywebsocketbirthday.domain.usecase.GetBabyInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BabyViewModel @Inject constructor(private val getBabyInfoUseCase: GetBabyInfoUseCase) :
    ViewModel() {

    private val _state = MutableStateFlow(BabyState())
    val state: StateFlow<BabyState> = _state.asStateFlow()

    fun connectToWebSocket(ipAddress: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            getBabyInfoUseCase(ipAddress)
                .onEach { babyInfo ->
                    _state.value = _state.value.copy(babyInfo = babyInfo, isLoading = false)
                }.launchIn(viewModelScope)
        }
    }

    fun resetBabyInfo() {
        _state.value = _state.value.copy(babyInfo = null)
    }
    fun updateIpPort(newIpPort: String) {
        _state.value = _state.value.copy(ipPort = newIpPort)
    }

    fun updateValidationResult(newValidationResult: ValidationResult) {
        _state.value = _state.value.copy(validationResult = newValidationResult)
    }
}

data class BabyState(
    val babyInfo: BabyInfo? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val ipPort: String = "192.168.100.12:8080",
    val validationResult: ValidationResult = ValidationResult(true) // Add validationResult
)