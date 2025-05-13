package com.nanit.happywebsocketbirthday.domain.usecase

import com.nanit.happywebsocketbirthday.domain.model.AgeResult
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class GetAgeDisplayInfoUseCase @Inject constructor() {
    operator fun invoke(dateOfBirth: Long?): AgeResult {
        val wholeMonthsAge = dateOfBirth?.let {
            val currentTime = Clock.System.now() // Get the current time as Instant
            val timeZone = TimeZone.currentSystemDefault() // Get the current system time zone
            calculateWholeMonthsOfAge(
                it,
                currentTime,
                timeZone
            )
        }

        return when {
            wholeMonthsAge == null -> AgeResult.Default
            wholeMonthsAge <= 12 -> AgeResult.Months(wholeMonthsAge)
            else -> {
                val calculatedYears = wholeMonthsAge / 12
                AgeResult.Years(calculatedYears)
            }
        }
    }


    /**
     * Calculates the age of a baby in months (up to 1 year) or years (after 1 year).
     *
     * @param dob The baby's date of birth as a timestamp.
     * @param currentInstant The current time as an Instant.
     * @param timeZone The time zone to use for date calculations.
     * @return baby`s whole months of age.
     */
    private fun calculateWholeMonthsOfAge(
        dob: Long,
        currentInstant: Instant,
        timeZone: TimeZone
    ): Int {

        val dobInstant: Instant = Instant.fromEpochMilliseconds(dob)

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
            return if (adjustedMonths >= 0) { // Ensure we don't go negative for very young babies
                adjustedMonths
            } else {
                0
            }
        } else {
            return months
        }
    }
}