package com.nanit.happywebsocketbirthday.presentation.birthday

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nanit.happywebsocketbirthday.data.BabyRepository
import com.nanit.happywebsocketbirthday.domain.usecase.GetAgeDisplayInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class BirthdayScreenViewModel @Inject constructor(
    private val babyRepository: BabyRepository,
    private val getAgeDisplayInfoUseCase: GetAgeDisplayInfoUseCase
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(BirthdayScreenState(ageDisplayInfo = getAgeDisplayInfoUseCase(null)))
    val uiState: StateFlow<BirthdayScreenState> = _uiState.asStateFlow()

    // Use a SharedFlow to trigger the dialog as a one-time event
    private val _showPictureSourceDialog = MutableSharedFlow<Unit>()
    val showPictureSourceDialog = _showPictureSourceDialog.asSharedFlow()


    init {
        fetchBabyInfo()
    }

    private fun fetchBabyInfo() {
        // Load baby info from Shared Preferences when the ViewModel is created
        viewModelScope.launch {
            // Set loading state immediately
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null)
            }

            val loadedBabyInfo = babyRepository.getBabyInfoFromPreferences()

            _uiState.update {
                if (loadedBabyInfo != null) {
                    it.copy(
                        babyInfo = loadedBabyInfo,
                        isLoading = false,
                        errorMessage = null,
                        ageDisplayInfo = getAgeDisplayInfoUseCase(loadedBabyInfo)
                    )
                } else {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No baby info found in preferences.",
                        ageDisplayInfo = getAgeDisplayInfoUseCase(null)
                    )
                }
            }
        }
    }

    fun onCameraIconClick() {
        // Emit an event to show the picture source dialog
        viewModelScope.launch {
            _showPictureSourceDialog.emit(Unit)
        }
    }

    fun updateBabyPictureUri(uri: Uri?) { // Renamed parameter for clarity
        _uiState.update { currentState ->
            currentState.copy(babyPictureUri = uri)
        }
    }
}