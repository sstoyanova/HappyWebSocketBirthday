package com.nanit.happywebsocketbirthday.domain.repository

import com.nanit.happywebsocketbirthday.domain.model.BabyInfo
import com.nanit.happywebsocketbirthday.domain.model.Result
import kotlinx.coroutines.flow.Flow

interface BabyRepository {
    suspend fun connectToWS(ipAddress: String): Flow<Result<String>>
    suspend fun sendMessageToWS(message: String): Result<Unit>
    fun saveBabyInfo(babyInfo: BabyInfo): Result<Unit>
    fun getBabyInfoFromPreferences(): Result<BabyInfo>
}