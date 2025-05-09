package com.nanit.happywebsocketbirthday.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class BabyInfo(
    @SerialName("name") val name: String,
    @SerialName("dob") val dob: Instant,
    @SerialName("theme") val theme: String
) {
    companion object {
        fun fromJson(jsonString: String): BabyInfo {
            return Json.decodeFromString(serializer(), jsonString)
        }
    }
}