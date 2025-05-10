package com.nanit.happywebsocketbirthday.domain.model

import androidx.annotation.DrawableRes

data class AgeDisplayInfo(
    @DrawableRes val numberIconDrawableId: Int,
    val ageLabelText: String
)