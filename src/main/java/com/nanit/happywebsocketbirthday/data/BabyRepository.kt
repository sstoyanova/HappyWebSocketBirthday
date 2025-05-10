package com.nanit.happywebsocketbirthday.data

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.nanit.happywebsocketbirthday.domain.model.BabyInfo
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import java.net.ConnectException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BabyRepository @Inject constructor(
    private val client: HttpClient,
    private val sharedPreferences: SharedPreferences
) {
    private val BABY_INFO_KEY = "baby_info" // Key for Shared Preferences

    fun connectToWebSocket(ipAddress: String, message: String): Flow<BabyInfo?> = flow {
        val (ip, port) = ipAddress.split(":")
        val url = "ws://$ip:$port/nanit"

        try {
            client.webSocket(
                method = HttpMethod.Get,
                host = ip,
                port = port.toInt(),
                path = "/nanit"
            ) {
                Log.d("WebSocket", "WebSocket connection established to: $url")
                send(Frame.Text(message))
                Log.d("WebSocket", "Message sent: $message")

                while (isActive) {
                    when (val frame = incoming.receive()) {
                        is Frame.Text -> {
                            val receivedText = frame.readText()
                            Log.d("WebSocket", "Received message: $receivedText")

                            val babyInfo = try {
                                BabyInfo.fromJson(receivedText)
                            } catch (e: Exception) {
                                Log.e("WebSocket", "Error decoding JSON: ${e.message}")
                                null
                            }
                            emit(babyInfo)

                            if (babyInfo != null) {
                                // Only read one message
                                break
                            }
                        }

                        else -> {
                            Log.w("WebSocket", "Received non-text frame")
                        }
                    }
                }
            }
        } catch (e: ConnectException) {
            Log.e("WebSocket", "Connection failed: ${e.message}")
            emit(null)
        } catch (e: Exception) {
            Log.e("WebSocket", "General exception: ${e.message}")
            emit(null)
        } finally {
            Log.d("WebSocket", "WebSocket connection closed")
        }
    }


    // Function to save BabyInfo to Shared Preferences
    fun saveBabyInfo(babyInfo: BabyInfo) {
        try {
            val babyInfoJsonString = babyInfo.toJson() // Use the toJson() instance function
            sharedPreferences.edit {
                putString(BABY_INFO_KEY, babyInfoJsonString)
                apply()
            }
            Log.d("BabyRepository", "BabyInfo saved to Shared Preferences")
        } catch (e: Exception) {
            Log.e("BabyRepository", "Error saving BabyInfo to Shared Preferences: ${e.message}")
        }
    }

    // Function to read BabyInfo from Shared Preferences
    fun getBabyInfoFromPreferences(): BabyInfo? {
        val babyInfoJsonString = sharedPreferences.getString(BABY_INFO_KEY, null)
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