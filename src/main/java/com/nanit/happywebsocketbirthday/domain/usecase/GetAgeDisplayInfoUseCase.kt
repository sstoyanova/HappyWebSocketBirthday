package com.nanit.happywebsocketbirthday.domain.usecase

import androidx.annotation.DrawableRes
import com.nanit.happywebsocketbirthday.R
import com.nanit.happywebsocketbirthday.domain.model.AgeDisplayInfo
import com.nanit.happywebsocketbirthday.domain.model.BabyInfo
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class GetAgeDisplayInfoUseCase @Inject constructor() {
    operator fun invoke(babyInfo: BabyInfo?): AgeDisplayInfo {
        val wholeMonthsAge = babyInfo?.let {
            val currentTime = Clock.System.now() // Get the current time as Instant
            val timeZone = TimeZone.currentSystemDefault() // Get the current system time zone
            calculateWholeMonthsOfAge(
                it.dob,
                currentTime,
                timeZone
            )
        }
        val monthsOrYearsLabel: String
        @DrawableRes val numberIconDrawableId: Int

        if (wholeMonthsAge != null) {
            if (wholeMonthsAge <= 12) {
                monthsOrYearsLabel = if (wholeMonthsAge == 1) "MONTH OLD!"
                else "MONTHS OLD!"
                numberIconDrawableId =
                    numberIconMap[wholeMonthsAge] ?: R.drawable.icon_0
            } else {
                val calculatedYears = wholeMonthsAge / 12

                monthsOrYearsLabel = if (calculatedYears == 1) "YEAR OLD!"
                else "YEARS OLD!"

                numberIconDrawableId = when (calculatedYears) {
                    in 1..9 -> numberIconMap[calculatedYears] ?: R.drawable.icon_0
                    else -> R.drawable.icon_0 // Handle other year values with a default
                }
            }
            return AgeDisplayInfo(numberIconDrawableId, monthsOrYearsLabel)
        } else {
            //default
            return AgeDisplayInfo(R.drawable.icon_0, "MONTH OLD!")
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
            if (adjustedMonths >= 0) { // Ensure we don't go negative for very young babies
                return adjustedMonths
            } else {
                return 0
            }
        } else {
            return months
        }
    }

    private val numberIconMap = mapOf(
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