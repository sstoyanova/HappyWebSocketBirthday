package com.nanit.happywebsocketbirthday.domain.usecase

import com.nanit.happywebsocketbirthday.domain.model.Result
import com.nanit.happywebsocketbirthday.domain.repository.BabyRepository
import javax.inject.Inject

class ConnectToWSUseCase @Inject constructor(private val babyRepository: BabyRepository) {
    suspend operator fun invoke(ipAddress: String): Result<Unit> {
        return babyRepository.connectToWS(ipAddress)
    }
}