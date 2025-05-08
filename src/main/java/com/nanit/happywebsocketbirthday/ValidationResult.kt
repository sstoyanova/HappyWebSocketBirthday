package com.nanit.happywebsocketbirthday

import java.util.regex.Pattern

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
    val ipPattern = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)(\\.(?!$)|$)){4}$"
    return Pattern.compile(ipPattern).matcher(ip).matches()
}

fun isValidPort(port: String): Boolean {
    return try {
        val portNumber = port.toInt()
        portNumber in 0..65535
    } catch (e: NumberFormatException) {
        false
    }
}