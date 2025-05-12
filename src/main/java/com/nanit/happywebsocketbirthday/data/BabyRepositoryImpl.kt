package com.nanit.happywebsocketbirthday.data

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.nanit.happywebsocketbirthday.data.network.WebSocketClient
import com.nanit.happywebsocketbirthday.domain.model.BabyInfo
import com.nanit.happywebsocketbirthday.domain.model.Result
import com.nanit.happywebsocketbirthday.domain.repository.BabyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BabyRepositoryImpl @Inject constructor(
    private val webSocketClient: WebSocketClient,
    private val sharedPreferences: SharedPreferences
) : BabyRepository {
    private val babyInfoNameKey = "baby_info_name" // Key for Shared Preferences
    private val babyInfoDobKey = "baby_info_date_of_birth" // Key for Shared Preferences
    private val babyInfoThemeKey = "baby_info_theme" // Key for Shared Preferences

    // Function to fetch BabyInfo directly from the WebSocket
    override suspend fun sendMessageToWS(message: String): Result<Unit> { // Return Result<Unit>
        return webSocketClient.sendMessage(message)
    }

    // Function to fetch BabyInfo directly from the WebSocket
    override suspend fun connectToWS(ipAddress: String): Flow<Result<String>> = flow {
        try {
            // Call the suspend functions within the coroutine
            webSocketClient.connect(ipAddress)
            emit(Result.Success("Connected to $ipAddress"))
        } catch (e: Exception) {
            Log.e("BabyRepository", "Error during WebSocket connection or sending message", e)
            // Handle the error appropriately, perhaps by emitting an error state
            // through a separate Flow or channel if needed.
            emit(Result.Error(e.message ?: "Unknown error", e))
        }
    }

    // Function to save BabyInfo to Shared Preferences
    override fun saveBabyInfo(babyInfo: BabyInfo): Result<Unit> {
        try {
            sharedPreferences.edit {
                putString(babyInfoNameKey, babyInfo.name)
                putLong(babyInfoDobKey, babyInfo.dateOfBirth)
                putString(babyInfoThemeKey, babyInfo.theme)
                apply()
            }
            Log.d("BabyRepository", "BabyInfo saved to Shared Preferences")
            return Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("BabyRepository", "Error saving BabyInfo to Shared Preferences: ${e.message}")
            return Result.Error(e.message ?: "Unknown error", e)
        }
    }

    // Function to read BabyInfo from Shared Preferences
    override fun getBabyInfoFromPreferences(): Result<BabyInfo> {
        try {
            val name = sharedPreferences.getString(babyInfoNameKey, "")
            val dob = sharedPreferences.getLong(babyInfoDobKey, 0)
            val theme = sharedPreferences.getString(babyInfoThemeKey, "pelican")
            if (name.isNullOrBlank() || dob == 0L || theme.isNullOrBlank()) {
                return Result.Success(BabyInfo(name ?: "", dob, theme ?: "pelican"))
            } else return Result.Error("No saved baby info found")
        } catch (e: Exception) {
            Log.e(
                "BabyRepository",
                "Error reading BabyInfo from Shared Preferences: ${e.message}"
            )
            return Result.Error(e.message ?: "Unknown error", e)
        }
    }
}