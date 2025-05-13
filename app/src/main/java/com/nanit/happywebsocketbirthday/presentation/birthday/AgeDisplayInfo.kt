package com.nanit.happywebsocketbirthday.presentation.birthday

import androidx.annotation.DrawableRes
import androidx.annotation.PluralsRes

data class AgeDisplayInfo(
    @DrawableRes val numberIconDrawableId: Int,
    @PluralsRes val ageLabelPluralResId: Int,
    val age: Int
)