package com.nanit.happywebsocketbirthday.data

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.nanit.happywebsocketbirthday.data.model.ApiBabyInfo
import com.nanit.happywebsocketbirthday.data.network.WebSocketClient
import com.nanit.happywebsocketbirthday.domain.model.BabyInfo
import com.nanit.happywebsocketbirthday.domain.repository.BabyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BabyRepositoryImpl @Inject constructor(
    private val webSocketClient: WebSocketClient,
    private val sharedPreferences: SharedPreferences
) : BabyRepository {
    private val babyInfoKey = "baby_info" // Key for Shared Preferences

    // Function to fetch BabyInfo directly from the WebSocket
    override fun getBabyInfoFromNetwork(ipAddress: String, message: String): Flow<BabyInfo?> {
        // Directly delegate the connection and data reception to the WebSocketClient
        // The Repository acts as an access point to this data source.
        return webSocketClient.connectAndReceiveBabyInfo(ipAddress, message)
            .onEach { babyInfo ->
                if (babyInfo != null) {
                    Log.d("BabyRepository", "Received BabyInfo from WebSocket")
                } else {
                    Log.d(
                        "BabyRepository",
                        "Received null BabyInfo from WebSocket or error occurred"
                    )
                }
            }
            .map { apiBabyInfo ->
                apiBabyInfo?.toDomainModel()
            } // Map ApiBabyInfo to DomainBabyInfo for the use case
    }

    // Function to save BabyInfo to Shared Preferences
    override fun saveBabyInfo(babyInfo: BabyInfo) {
        try {
            val apiBabyInfo = babyInfo.toApiModel() // Map domain model to data model
            val babyInfoJsonString = apiBabyInfo.toJson() // Use toJson from ApiBabyInfo
            sharedPreferences.edit {
                putString(babyInfoKey, babyInfoJsonString)
                apply()
            }
            Log.d("BabyRepository", "BabyInfo saved to Shared Preferences")
        } catch (e: Exception) {
            Log.e("BabyRepository", "Error saving BabyInfo to Shared Preferences: ${e.message}")
        }
    }

    // Function to read BabyInfo from Shared Preferences
    override fun getBabyInfoFromPreferences(): BabyInfo? {
        val babyInfoJsonString = sharedPreferences.getString(babyInfoKey, null)
        return if (babyInfoJsonString != null) {
            try {
                val apiBabyInfo =
                    ApiBabyInfo.fromJson(babyInfoJsonString) // Use fromJson from ApiBabyInfo
                val domainBabyInfo = apiBabyInfo.toDomainModel() // Map data model to domain model
                Log.d("BabyRepository", "DomainBabyInfo read from Shared Preferences")
                domainBabyInfo
            } catch (e: Exception) {
                Log.e(
                    "BabyRepository",
                    "Error reading BabyInfo from Shared Preferences: ${e.message}"
                )
                null
            }
        } else {
            null
        }
    }
}

// Extension functions for mapping between domain and data models (can be in a separate file in data layer)
fun ApiBabyInfo.toDomainModel(): BabyInfo {
    return BabyInfo(
        name = this.name,
        dateOfBirth = this.dob,
        theme = this.theme
    )
}

fun BabyInfo.toApiModel(): ApiBabyInfo {
    return ApiBabyInfo(
        name = this.name,
        dob = this.dateOfBirth,
        theme = this.theme
    )
}