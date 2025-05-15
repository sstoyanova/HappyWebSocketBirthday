package com.nanit.happywebsocketbirthday.domain.repository

import com.nanit.happywebsocketbirthday.data.network.WebSocketClient
import com.nanit.happywebsocketbirthday.domain.model.BabyInfo
import com.nanit.happywebsocketbirthday.domain.model.Result
import kotlinx.coroutines.flow.Flow

interface BabyRepository {
    suspend fun connectToWS(ipAddress: String): Flow<Result<WebSocketClient.ConnectionState>>
    suspend fun disconnectFromWS(): Result<String>
    suspend fun sendMessageToWS(message: String): Result<Unit>
    fun receiveBabyInfo(): Flow<Result<BabyInfo>>
}