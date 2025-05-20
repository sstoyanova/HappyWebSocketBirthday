package com.nanit.happywebsocketbirthday.domain.usecase

import com.nanit.happywebsocketbirthday.data.network.WebSocketClient
import com.nanit.happywebsocketbirthday.domain.repository.BabyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveWSConnectionUseCase @Inject constructor(private val babyRepository: BabyRepository) {
    operator fun invoke(): Flow<WebSocketClient.ConnectionState> {
        return babyRepository.observeConnectionState()
    }
}