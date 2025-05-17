package com.nanit.happywebsocketbirthday.presentation.birthday

import androidx.annotation.DrawableRes
import androidx.annotation.PluralsRes
import com.nanit.happywebsocketbirthday.R


data class AgeDisplayInfo(
    @PluralsRes val ageLabelPluralResId: Int,
    val age: Int
) {
    @DrawableRes
    val numberIconDrawableId: Int = getIconDrawableId(age)

    companion object {
        private val numberIconMap: Map<Int, Int> =
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
        val Default: AgeDisplayInfo = AgeDisplayInfo(
            ageLabelPluralResId = R.plurals.months_old,
            age = 0
        )

        //returns @DrawableRes icon corresponding to the passed number
        @JvmStatic
        internal fun getIconDrawableId(number: Int): Int {
            return numberIconMap[number] ?: R.drawable.icon_0
        }
    }
}