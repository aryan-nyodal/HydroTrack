package com.hydrotrack.domain.usecase

import com.hydrotrack.data.local.entity.WaterLogEntity
import com.hydrotrack.data.repository.WaterRepository
import com.hydrotrack.util.DateUtils
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTodayLogsUseCase @Inject constructor(
    private val waterRepository: WaterRepository
) {
    operator fun invoke(): Flow<List<WaterLogEntity>> {
        return waterRepository.getTodayLogs()
    }
}
