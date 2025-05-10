package com.nanit.happywebsocketbirthday.presentation.ipsetup

import com.nanit.happywebsocketbirthday.ValidationResult
import com.nanit.happywebsocketbirthday.domain.model.BabyInfo


// A data class to hold the UI state for the IpSetupScreen
data class IpSetupScreenState( // Or IpSetupScreenState if that's the name you're using
    val ipPort: String = "192.168.100.12:8080",
    val validationResult: ValidationResult = ValidationResult(true),
    val isLoading: Boolean = false,
    val babyInfoReceived: Boolean = false, // Flag to indicate if baby info was received
    val errorMessage: String? = null,
    val babyInfo: BabyInfo? = null // Data class to hold baby information
)