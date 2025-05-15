package com.nanit.happywebsocketbirthday.presentation.ipsetup

import com.nanit.happywebsocketbirthday.ValidationResult
import com.nanit.happywebsocketbirthday.domain.model.BabyInfo


// A data class to hold the UI state for the IpSetupScreen
data class IpSetupScreenState(
    val ipPort: String = "",
    val validationResult: ValidationResult = ValidationResult(true),
    val isLoading: Boolean = false,
    val isConnected: Boolean = false, // Flag to indicate if the client is connected to the WS
    val connectionStatusText: String = "No attempt to connect has been made", // Default initial value
    val messageStatusText: String = "Send message", // Default initial value
    val babyInfoStatusText: String = "No baby info received yet", // Default initial value
    val navigateToBabyInfoEvent: BabyInfo? = null // Event to trigger navigation
)