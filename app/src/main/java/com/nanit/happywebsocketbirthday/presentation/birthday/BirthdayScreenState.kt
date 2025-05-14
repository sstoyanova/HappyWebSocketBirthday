package com.nanit.happywebsocketbirthday.presentation.birthday

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.PluralsRes
import com.nanit.happywebsocketbirthday.R

/**
 * A data class to hold the UI state for the BirthdayScreen.
 *
 * This class represents the current state of the UI displayed on the BirthdayScreen.
 * It includes data for visual presentation like name, theme, age information, and a picture,
 * as well as state related to loading status and potential error messages.
 *
 * @property name The name to be displayed on the screen.
 * @property theme The theme or background to be used for visual representation.
 * @property ageDisplayInfo Information about how the baby's age should be displayed,
 *                           including the number icon, plural resource for the age label, and the age itself.
 *                           Can be null if the age information is not available.
 * @property pictureUri The Uri of the baby's picture to be displayed. Can be null if no picture is available.
 * @property isLoading Indicates whether the screen is currently in a loading state.
 */
data class BirthdayScreenState(
    //for visual representation:
    val name: String = "",
    val theme: String = "pelican",
    val ageDisplayInfo: AgeDisplayInfo = AgeDisplayInfo.Default,
    val pictureUri: Uri? = null, // Uri for the baby's picture
    val isLoading: Boolean = false, // Loading state
)

data class AgeDisplayInfo(
    @DrawableRes val numberIconDrawableId: Int,
    @PluralsRes val ageLabelPluralResId: Int,
    val age: Int
) {
    companion object {
        val Default: AgeDisplayInfo = AgeDisplayInfo(
            numberIconDrawableId = R.drawable.icon_0,
            ageLabelPluralResId = R.plurals.months_old,
            age = 0
        )
    }
}