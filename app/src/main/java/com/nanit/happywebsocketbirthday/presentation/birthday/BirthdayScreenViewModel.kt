package com.nanit.happywebsocketbirthday.presentation.birthday

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nanit.happywebsocketbirthday.R
import com.nanit.happywebsocketbirthday.domain.model.AgeResult
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
    private val getAgeDisplayInfoUseCase: GetAgeDisplayInfoUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(BirthdayScreenState())
    val uiState: StateFlow<BirthdayScreenState> = _uiState.asStateFlow()

    // Use a SharedFlow to trigger the dialog as a one-time event
    private val _showPictureSourceDialog = MutableSharedFlow<Unit>()
    val showPictureSourceDialog = _showPictureSourceDialog.asSharedFlow()


    // Function to map DomainBabyInfo (or null) to AgeDisplayInfo presentation model
    private fun calculateAndGetAgeDisplayInfo(dateOfBirth: Long?): AgeDisplayInfo {
        // Map the AgeResult to the presentation-level AgeDisplayInfo
        return when (val ageResult = getAgeDisplayInfoUseCase(dateOfBirth)) {
            is AgeResult.Months -> AgeDisplayInfo(
                ageLabelPluralResId = R.plurals.months_old, // Using presentation layer resources
                age = ageResult.months
            )

            is AgeResult.Years -> AgeDisplayInfo(
                ageLabelPluralResId = R.plurals.years_old, // Using presentation layer resources
                age = ageResult.years
            )

            AgeResult.Default -> AgeDisplayInfo(
                ageLabelPluralResId = R.plurals.months_old, // Using presentation layer resources
                age = 0
            )
        }
    }

    fun onCameraIconClick() {
        // Emit an event to show the picture source dialog
        viewModelScope.launch {
            _showPictureSourceDialog.emit(Unit)
        }
    }

    fun onPictureSelected(uri: Uri?) { // Renamed parameter for clarity
        _uiState.update { currentState ->
            currentState.copy(pictureUri = uri)
        }
    }


    fun initialize(name: String, dateOfBirth: Long, theme: String) {
        _uiState.update {
            it.copy(
                name = name,
                theme = theme,
                ageDisplayInfo = calculateAndGetAgeDisplayInfo(
                    dateOfBirth
                ),
                isLoading = false
            )
        }
    }
}