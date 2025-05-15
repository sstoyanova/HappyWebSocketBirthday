package com.nanit.happywebsocketbirthday.domain.usecase

import com.nanit.happywebsocketbirthday.domain.model.BabyInfo
import com.nanit.happywebsocketbirthday.domain.model.Result
import com.nanit.happywebsocketbirthday.domain.repository.BabyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ReceiveBabyInfoWSUseCase @Inject constructor(private val babyRepository: BabyRepository) {
    operator fun invoke(): Flow<Result<BabyInfo>> {
        return babyRepository.receiveBabyInfo()
    }
}