package com.nanit.happywebsocketbirthday.presentation.ipsetup

import com.nanit.happywebsocketbirthday.ValidationResult


// A data class to hold the UI state for the IpSetupScreen
data class IpSetupScreenState( // Or IpSetupScreenState if that's the name you're using
    val ipPort: String = "192.168.100.12:8080",
    val validationResult: ValidationResult = ValidationResult(true),
    val isLoading: Boolean = false,
    val babyInfoReceived: Boolean = false, // Flag to indicate if baby info was received
    val isConnected: Boolean = false, // Flag to indicate if the client is connected to the WS
    val connectionStatusText: String = "No attempt to connect has been made", // Default initial value
    val messageStatusText: String = "Send message", // Default initial value
    val babyInfoStatusText: String = "No baby info received yet" // Default initial value
)