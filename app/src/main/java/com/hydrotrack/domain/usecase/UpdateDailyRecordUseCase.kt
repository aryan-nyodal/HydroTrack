package com.hydrotrack.domain.usecase

import com.hydrotrack.data.repository.SettingsRepository
import com.hydrotrack.data.repository.WaterRepository
import com.hydrotrack.util.DateUtils
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateDailyRecordUseCase @Inject constructor(
    private val waterRepository: WaterRepository,
    private val settingsRepository: SettingsRepository
) {
    /**
     * Ensures today's daily record exists and is up-to-date.
     * Called on app open and after settings changes.
     */
    suspend operator fun invoke(date: String = DateUtils.today()) {
        val goalMl = settingsRepository.dailyGoal.first()
        val totalMl = waterRepository.getTotalForDate(date)
        val record = waterRepository.getDailyRecord(date)

        if (record == null) {
            waterRepository.getOrCreateDailyRecord(date, goalMl)
            val newRecord = waterRepository.getDailyRecord(date)
            if (newRecord != null) {
                waterRepository.updateDailyRecord(
                    newRecord.copy(totalMl = totalMl, goalMet = totalMl >= goalMl)
                )
            }
        } else {
            // Update total but preserve goal snapshot (don't change past days' goals)
            val goalToUse = if (DateUtils.isToday(date)) goalMl else record.goalMl
            val isGoalMet = totalMl >= goalToUse
            waterRepository.updateDailyRecord(
                record.copy(
                    totalMl = totalMl,
                    goalMl = goalToUse,
                    goalMet = isGoalMet
                )
            )
        }
    }
}
