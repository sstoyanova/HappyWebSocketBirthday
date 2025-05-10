package com.nanit.happywebsocketbirthday.data

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.nanit.happywebsocketbirthday.data.network.WebSocketClient
import com.nanit.happywebsocketbirthday.domain.model.BabyInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BabyRepository @Inject constructor(
    private val webSocketClient: WebSocketClient,
    private val sharedPreferences: SharedPreferences
) {
    private val babyInfoKey = "baby_info" // Key for Shared Preferences

    // Function to fetch BabyInfo directly from the WebSocket
    fun getBabyInfoFromNetwork(ipAddress: String, message: String): Flow<BabyInfo?> {
        // Directly delegate the connection and data reception to the WebSocketClient
        // The Repository acts as an access point to this data source.
        return webSocketClient.connectAndReceiveBabyInfo(ipAddress, message)
            .onEach { babyInfo ->
                if (babyInfo != null) {
                    Log.d("BabyRepository", "Received BabyInfo from WebSocket")
                } else {
                    Log.d("BabyRepository", "Received null BabyInfo from WebSocket or error occurred")
                }
            }
    }

    // Function to save BabyInfo to Shared Preferences
    fun saveBabyInfo(babyInfo: BabyInfo) {
        try {
            val babyInfoJsonString = babyInfo.toJson() // Use the toJson() instance function
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
    fun getBabyInfoFromPreferences(): BabyInfo? {
        val babyInfoJsonString = sharedPreferences.getString(babyInfoKey, null)
        return if (babyInfoJsonString != null) {
            try {
                BabyInfo.fromJson(babyInfoJsonString)
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