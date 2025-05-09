package com.nanit.happywebsocketbirthday.domain.usecase

import com.nanit.happywebsocketbirthday.data.BabyRepository
import com.nanit.happywebsocketbirthday.domain.model.BabyInfo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBabyInfoUseCase @Inject constructor(private val babyRepository: BabyRepository) {
    operator fun invoke(ipAddress: String): Flow<BabyInfo?> {
        return babyRepository.connectToWebSocket(ipAddress, "HappyBirthday")
    }
}