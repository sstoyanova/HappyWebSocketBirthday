package com.nanit.happywebsocketbirthday.domain.usecase

import com.nanit.happywebsocketbirthday.domain.model.Result
import com.nanit.happywebsocketbirthday.domain.repository.BabyRepository
import javax.inject.Inject


//todo later rename to GetBabyInfoUseCase
class SendMessageUseCase @Inject constructor(private val babyRepository: BabyRepository) {
    suspend operator fun invoke():Result<Unit>{ // Return Result<Unit>
        val message = "HappyBirthday" // Your fixed message
        return babyRepository.sendMessageToWS(message)
    }
}