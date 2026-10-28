package com.hydrotrack.domain.usecase

import com.hydrotrack.data.local.dao.DailyTotal
import com.hydrotrack.data.local.entity.DailyRecordEntity
import com.hydrotrack.data.repository.WaterRepository
import javax.inject.Inject

class GetHistoryUseCase @Inject constructor(
    private val waterRepository: WaterRepository
) {
    suspend fun getDailyTotals(startDate: String, endDate: String): List<DailyTotal> {
        return waterRepository.getDailyTotals(startDate, endDate)
    }

    suspend fun getRecordsInRange(startDate: String, endDate: String): List<DailyRecordEntity> {
        return waterRepository.getRecordsInRange(startDate, endDate)
    }

    suspend fun getAllRecords(): List<DailyRecordEntity> {
        return waterRepository.getAllDailyRecordsOnce()
    }
}
