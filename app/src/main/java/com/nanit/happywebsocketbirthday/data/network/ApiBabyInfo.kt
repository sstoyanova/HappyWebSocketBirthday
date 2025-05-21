package com.nanit.happywebsocketbirthday.data.network

import com.nanit.happywebsocketbirthday.domain.model.BabyInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Represents the information about a baby received from the API.
 *
 * This data class is used to deserialize the JSON response containing baby details.
 * It includes the baby's name, date of birth, and the selected theme.
 */
@Serializable
data class ApiBabyInfo(
    @SerialName("name") val name: String,
    @SerialName("dob") val dob: Long,
    @SerialName("theme") val theme: String
) {
    fun toDomain(): BabyInfo {
        return BabyInfo(name, dob, theme)
    }

    companion object {
        fun fromJson(jsonString: String): ApiBabyInfo {
            return Json.decodeFromString(serializer(), jsonString)
        }
    }
}