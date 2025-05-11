package com.nanit.happywebsocketbirthday.domain.usecase

import com.nanit.happywebsocketbirthday.data.model.ApiBabyInfo
import com.nanit.happywebsocketbirthday.domain.model.Result
import com.nanit.happywebsocketbirthday.domain.repository.BabyRepository
import javax.inject.Inject

class SaveBabyInfoToPreferencesUseCase @Inject constructor(
    private val babyRepository: BabyRepository
) {
    operator fun invoke(apiBabyInfo: ApiBabyInfo): Result<Unit> {
        // This Use Case simply delegates to the repository's function
        return babyRepository.saveBabyInfo(apiBabyInfo = apiBabyInfo)
    }
}