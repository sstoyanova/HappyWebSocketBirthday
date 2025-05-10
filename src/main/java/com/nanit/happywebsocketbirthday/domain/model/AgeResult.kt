package com.nanit.happywebsocketbirthday.domain.model

sealed class AgeResult {
    data class Months(val months: Int) : AgeResult()
    data class Years(val years: Int) : AgeResult()
    object Default : AgeResult() // For the null case
}