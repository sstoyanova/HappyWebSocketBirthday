package com.nanit.happywebsocketbirthday.domain.usecase

import com.nanit.happywebsocketbirthday.domain.model.Result
import com.nanit.happywebsocketbirthday.domain.repository.BabyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ConnectToWSUseCase @Inject constructor(private val babyRepository: BabyRepository) {
    suspend operator fun invoke(ipAddress: String): Flow<Result<String>> {
        return babyRepository.connectToWS(ipAddress)
    }
}