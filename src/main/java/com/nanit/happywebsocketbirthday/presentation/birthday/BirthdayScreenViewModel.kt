package com.nanit.happywebsocketbirthday.presentation.birthday

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nanit.happywebsocketbirthday.data.BabyRepository
import com.nanit.happywebsocketbirthday.domain.model.BabyInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

/**
 * Calculates the age of a baby in months (up to 1 year) or years (after 1 year).
 *
 * @param dobInstant The baby's date of birth as an Instant.
 * @param currentInstant The current time as an Instant.
 * @param timeZone The time zone to use for date calculations.
 * @return baby`s whole months of age.
 */
fun calculateWholeMonthsOfAge(
    dobInstant: Instant,
    currentInstant: Instant,
    timeZone: TimeZone
): Int {
    // Convert Instants to LocalDate for easier comparison and calculation
    val dobDate = dobInstant.toLocalDateTime(timeZone).date
    val currentDate = currentInstant.toLocalDateTime(timeZone).date

    // Calculate the difference in months
    val months = (currentDate.year * 12 + currentDate.month.number) -
            (dobDate.year * 12 + dobDate.month.number)

    // Adjust months if the day of the month hasn't been reached yet in the current month
    if (currentDate.dayOfMonth < dobDate.dayOfMonth) {
        // If the current day is before the birth day in the current month,
        // the number of complete months is one less.
        val adjustedMonths = months - 1
        if (adjustedMonths >= 0) { // Ensure we don't go negative for very young babies
            return adjustedMonths
        } else {
            return 0
        }
    } else {
        return months
    }
}

// A data class to hold the UI state for the BabyInfoScreen
data class BirthdayScreenState(
    val babyInfo: BabyInfo? = null,
    val wholeMonthsAge: Int? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class BirthdayScreenViewModel @Inject constructor(
    private val babyRepository: BabyRepository // Inject BabyRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(BirthdayScreenState())
    val uiState: StateFlow<BirthdayScreenState> = _uiState.asStateFlow()

    init {
        // Load baby info from Shared Preferences when the ViewModel is created
        viewModelScope.launch {
            val loadedBabyInfo = babyRepository.getBabyInfoFromPreferences()
            val wholeMonthsAge = loadedBabyInfo?.let {
                val currentTime = Clock.System.now() // Get the current time as Instant
                val timeZone = TimeZone.currentSystemDefault() // Get the current system time zone
                calculateWholeMonthsOfAge(
                    Instant.fromEpochMilliseconds(it.dob),
                    currentTime,
                    timeZone
                ) // Calculate the age
            }

            _uiState.update {
                if (loadedBabyInfo != null) {
                    it.copy(
                        babyInfo = loadedBabyInfo,
                        wholeMonthsAge = wholeMonthsAge,
                        isLoading = false,
                        errorMessage = null
                    )
                } else {
                    it.copy(
                        babyInfo = null,
                        wholeMonthsAge = null,
                        isLoading = false,
                        errorMessage = "No baby info found in preferences."
                    )
                }
            }
        }
    }
}