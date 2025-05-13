package com.nanit.happywebsocketbirthday.data

import android.util.Log
import com.nanit.happywebsocketbirthday.data.network.WebSocketClient
import com.nanit.happywebsocketbirthday.domain.model.Result
import com.nanit.happywebsocketbirthday.domain.repository.BabyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
}