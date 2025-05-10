package com.nanit.happywebsocketbirthday.presentation.birthday

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nanit.happywebsocketbirthday.data.BabyRepository
import com.nanit.happywebsocketbirthday.domain.usecase.GetAgeDisplayInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class BirthdayScreenViewModel @Inject constructor(
    private val babyRepository: BabyRepository, // Inject BabyRepository
    private val getAgeDisplayInfoUseCase: GetAgeDisplayInfoUseCase
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(BirthdayScreenState(ageDisplayInfo = getAgeDisplayInfoUseCase(null)))
    val uiState: StateFlow<BirthdayScreenState> = _uiState.asStateFlow()

    init {
        fetchBabyInfo() // Call the private function
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

}