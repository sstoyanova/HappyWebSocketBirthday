package com.nanit.happywebsocketbirthday

import androidx.compose.runtime.Immutable
import java.util.regex.Pattern

// Pre-compile the pattern once when the file is loaded
private val COMPILED_IP_PATTERN: Pattern = Pattern.compile("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)(\\.(?!$)|$)){4}$")

@Immutable
data class ValidationResult(val isValid: Boolean, val errorMessage: String = "")

fun isValidIpPortFormat(input: String): ValidationResult {
    if (input.isEmpty()) {
        return ValidationResult(false, "IP:Port cannot be empty")
    }

    val parts = input.split(":")
    if (parts.size != 2) {
        return ValidationResult(false, "Invalid format: Use IP:Port")
    }

    val ip = parts[0]
    val port = parts[1]

    if (!isValidIpAddress(ip)) {
        return ValidationResult(false, "Invalid IP address format")
    }

    if (!isValidPort(port)) {
        return ValidationResult(false, "Invalid port number")
    }

    return ValidationResult(true)
}

fun isValidIpAddress(ip: String): Boolean {
    return COMPILED_IP_PATTERN.matcher(ip).matches()
}

fun isValidPort(port: String): Boolean {
    return try {
        val portNumber = port.toInt()
        portNumber in 0..65535
    } catch (e: NumberFormatException) {
        false
    }
}