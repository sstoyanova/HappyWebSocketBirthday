package com.nanit.happywebsocketbirthday.domain.model

import androidx.annotation.DrawableRes
import androidx.annotation.PluralsRes

data class AgeDisplayInfo(
    @DrawableRes val numberIconDrawableId: Int,
    @PluralsRes val ageLabelPluralResId: Int,
    val age: Int
)