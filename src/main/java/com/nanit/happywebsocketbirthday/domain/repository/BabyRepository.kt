package com.nanit.happywebsocketbirthday.domain.repository

import com.nanit.happywebsocketbirthday.domain.model.BabyInfo
import kotlinx.coroutines.flow.Flow

interface BabyRepository {
    fun getBabyInfoFromNetwork(ipAddress: String, message: String): Flow<BabyInfo?>
    fun saveBabyInfo(babyInfo: BabyInfo)
    fun getBabyInfoFromPreferences(): BabyInfo?
}