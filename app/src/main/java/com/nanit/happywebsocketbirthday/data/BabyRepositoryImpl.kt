package com.nanit.happywebsocketbirthday.data

import android.util.Log
import com.nanit.happywebsocketbirthday.data.network.WebSocketClient
import com.nanit.happywebsocketbirthday.domain.model.BabyInfo
import com.nanit.happywebsocketbirthday.domain.model.Result
import com.nanit.happywebsocketbirthday.domain.repository.BabyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BabyRepositoryImpl @Inject constructor(
    private val webSocketClient: WebSocketClient
) : BabyRepository {

    // Function to fetch BabyInfo directly from the WebSocket
    override suspend fun sendMessageToWS(message: String): Result<Unit> { // Return Result<Unit>
        return webSocketClient.sendMessage(message)
    }


    // Function to initiate the WebSocket connection attempt
    override suspend fun connectToWS(ipAddress: String): Result<Unit> {
        return try {
            // Call the suspend function to initiate connection
            webSocketClient.connect(ipAddress) // This function should handle its own internal connection logic
            Result.Success(Unit) // Signifies the connection attempt was initiated
        } catch (e: Exception) {
            Log.e("BabyRepository", "Error initiating WebSocket connection to $ipAddress", e)
            Result.Error(e.message ?: "Failed to initiate connection", e)
        }
    }

    // Function to observe WebSocket connection state changes
    override fun observeConnectionState(): Flow<WebSocketClient.ConnectionState> {
        return webSocketClient.connectionState
    }

// Your existing disconnectFromWS might also need adjustment if webSocketClient.disconnect()
// now just triggers disconnection and you observe the state separately.
override suspend fun disconnectFromWS(): Result<Unit> { // Modified to signify attempt
    return try {
        webSocketClient.disconnect()
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("BabyRepository", "Error initiating WebSocket disconnection", e)
        Result.Error(e.message ?: "Failed to initiate disconnection", e)
    }
}

override fun receiveBabyInfo(): Flow<Result<BabyInfo>> {
    return webSocketClient.receiveBabyInfo()
}
}