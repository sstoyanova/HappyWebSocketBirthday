package com.nanit.happywebsocketbirthday.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class BabyInfo(
    @SerialName("name") val name: String,
    @SerialName("dob") val dob: Long,
    @SerialName("theme") val theme: String
) {

    fun toJson(): String {
        return Json.encodeToString(this)
    }

    companion object {
        fun fromJson(jsonString: String): BabyInfo {
            return Json.decodeFromString(serializer(), jsonString)
        }
    }
}