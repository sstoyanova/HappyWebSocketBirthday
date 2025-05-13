package com.nanit.happywebsocketbirthday.presentation.birthday

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nanit.happywebsocketbirthday.R
import com.nanit.happywebsocketbirthday.domain.model.AgeResult
import com.nanit.happywebsocketbirthday.domain.model.BabyInfo
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

    private val numberIconMap: Map<Int, Int> by lazy {
        mapOf(
            0 to R.drawable.icon_0,
            1 to R.drawable.icon_1,
            2 to R.drawable.icon_2,
            3 to R.drawable.icon_3,
            4 to R.drawable.icon_4,
            5 to R.drawable.icon_5,
            6 to R.drawable.icon_6,
            7 to R.drawable.icon_7,
            8 to R.drawable.icon_8,
            9 to R.drawable.icon_9,
            10 to R.drawable.icon_10,
            11 to R.drawable.icon_11,
            12 to R.drawable.icon_12
        )
    }

    // Function to map DomainBabyInfo (or null) to AgeDisplayInfo presentation model
    private fun calculateAndGetAgeDisplayInfo(domainBabyInfo: BabyInfo?): AgeDisplayInfo {
        val ageResult = getAgeDisplayInfoUseCase(domainBabyInfo)

        // Map the AgeResult to the presentation-level AgeDisplayInfo
        return when (ageResult) {
            is AgeResult.Months -> AgeDisplayInfo(
                numberIconDrawableId = getIconDrawableId(ageResult.months), // Mapping to resources
                ageLabelPluralResId = R.plurals.months_old, // Using presentation layer resources
                age = ageResult.months
            )

            is AgeResult.Years -> AgeDisplayInfo(
                numberIconDrawableId = getIconDrawableId(ageResult.years), // Mapping to resources
                ageLabelPluralResId = R.plurals.years_old, // Using presentation layer resources
                age = ageResult.years
            )

            AgeResult.Default -> AgeDisplayInfo(
                numberIconDrawableId = R.drawable.icon_0, // Using presentation layer resources
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
            currentState.copy(babyPictureUri = uri)
        }
    }

    //returns @DrawableRes icon corresponding to the passed number
    private fun getIconDrawableId(number: Int): Int {
        return numberIconMap[number] ?: R.drawable.icon_0
    }

    fun initialize(name: String, dateOfBirth: Long, theme: String) {
        _uiState.update {
            it.copy(
                babyInfo = BabyInfo(name, dateOfBirth, theme),
                isLoading = false,
                errorMessage = null,
                //todo pass only the needed data(date of birth) here not the whole baby info object
                ageDisplayInfo = calculateAndGetAgeDisplayInfo(
                    domainBabyInfo = BabyInfo(
                        name,
                        dateOfBirth,
                        theme
                    )
                )
            )
        }
    }
}