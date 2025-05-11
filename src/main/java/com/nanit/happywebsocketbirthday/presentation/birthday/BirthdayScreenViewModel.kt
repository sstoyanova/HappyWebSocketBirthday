package com.nanit.happywebsocketbirthday.presentation.birthday

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nanit.happywebsocketbirthday.R
import com.nanit.happywebsocketbirthday.domain.model.AgeResult
import com.nanit.happywebsocketbirthday.domain.model.BabyInfo
import com.nanit.happywebsocketbirthday.domain.usecase.GetAgeDisplayInfoUseCase
import com.nanit.happywebsocketbirthday.domain.usecase.GetBabyInfoFromPreferencesUseCase
import com.nanit.happywebsocketbirthday.presentation.model.AgeDisplayInfo
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
    private val getBabyInfoFromPreferencesUseCase: GetBabyInfoFromPreferencesUseCase,
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


    init {
        loadInitialBabyInfoFromPreferences()
    }

    // Function to load initial baby info from Shared Preferences
    private fun loadInitialBabyInfoFromPreferences() {
        viewModelScope.launch {
            // Set loading state immediately
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val loadedBabyInfo = getBabyInfoFromPreferencesUseCase() // Call the Use Case
            _uiState.update {
                if (loadedBabyInfo != null) {
                    Log.d("BirthdayScreenViewModel", "loadInitialBabyInfoFromPreferences: loaded baby info from prefs:$loadedBabyInfo")
                    it.copy(
                        babyInfo = loadedBabyInfo,
                        isLoading = false,
                        errorMessage = null,
                        ageDisplayInfo = calculateAndGetAgeDisplayInfo(loadedBabyInfo)
                    )
                } else {
                    it.copy(
                        isLoading = false,
                        // Maybe a different error message if it's just not found initially
                        errorMessage = "No saved baby info found.",
                        ageDisplayInfo = calculateAndGetAgeDisplayInfo(null)
                    )
                }
            }
        }
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
}