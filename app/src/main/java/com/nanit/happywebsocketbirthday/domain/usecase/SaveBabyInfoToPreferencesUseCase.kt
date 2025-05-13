package com.nanit.happywebsocketbirthday.domain.usecase

import com.nanit.happywebsocketbirthday.domain.model.BabyInfo
import com.nanit.happywebsocketbirthday.domain.model.Result
import com.nanit.happywebsocketbirthday.domain.repository.BabyRepository
import javax.inject.Inject

class SaveBabyInfoToPreferencesUseCase @Inject constructor(
    private val babyRepository: BabyRepository
) {
    operator fun invoke(babyInfo: BabyInfo): Result<Unit> {
        // This Use Case simply delegates to the repository's function
        return babyRepository.saveBabyInfo(babyInfo = babyInfo)
    }
}