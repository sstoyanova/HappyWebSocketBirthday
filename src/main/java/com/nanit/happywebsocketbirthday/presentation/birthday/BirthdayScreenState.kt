package com.nanit.happywebsocketbirthday.presentation.birthday

import com.nanit.happywebsocketbirthday.domain.model.AgeDisplayInfo
import com.nanit.happywebsocketbirthday.domain.model.BabyInfo

// A data class to hold the UI state for the BabyInfoScreen
data class BirthdayScreenState(
    val babyInfo: BabyInfo = BabyInfo("", 0L, ""),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val ageDisplayInfo: AgeDisplayInfo
)