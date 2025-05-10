package com.nanit.happywebsocketbirthday.domain.usecase

import com.nanit.happywebsocketbirthday.data.BabyRepository
import com.nanit.happywebsocketbirthday.domain.model.BabyInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class GetBabyInfoUseCase @Inject constructor(private val babyRepository: BabyRepository) {
    operator fun invoke(ipAddress: String): Flow<BabyInfo?> {
        return babyRepository.getBabyInfoFromNetwork(ipAddress, "HappyBirthday")
            .onEach { babyInfo ->
                // Save the received babyInfo to Shared Preferences
                if (babyInfo != null) {
                    babyRepository.saveBabyInfo(babyInfo)
                } else {
                    // Handle the case where babyInfo is null or an error occurred

                }
            }
    }
}