package com.nanit.happywebsocketbirthday.domain.usecase

import com.nanit.happywebsocketbirthday.domain.model.BabyInfo
import com.nanit.happywebsocketbirthday.domain.repository.BabyRepository
import javax.inject.Inject

class GetBabyInfoFromPreferencesUseCase @Inject constructor(
    private val babyRepository: BabyRepository // Depend on the domain repository interface
) {
    operator fun invoke(): BabyInfo? {
        // This Use Case simply delegates to the repository's function
        return babyRepository.getBabyInfoFromPreferences()
    }
}