package com.hydrotrack.domain.usecase

import com.hydrotrack.data.local.entity.WaterLogEntity
import com.hydrotrack.data.repository.SettingsRepository
import com.hydrotrack.data.repository.WaterRepository
import com.hydrotrack.domain.PointsCalculator
import com.hydrotrack.util.DateUtils
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DeleteWaterLogUseCase @Inject constructor(
    private val waterRepository: WaterRepository,
    private val settingsRepository: SettingsRepository,
    private val pointsCalculator: PointsCalculator
) {
    data class Result(
        val newTotal: Int,
        val goalMl: Int,
        val goalNowUnmet: Boolean,
        val pointsRevoked: Long
    )

    suspend operator fun invoke(log: WaterLogEntity): Result {
        val date = log.localDate
        val goalMl = settingsRepository.dailyGoal.first()

        // Check if goal was met before deletion
        val totalBefore = waterRepository.getTotalForDate(date)
        val wasGoalMet = totalBefore >= goalMl

        // Delete the log
        waterRepository.deleteWaterLog(log)

        // Recalculate total
        val newTotal = waterRepository.getTotalForDate(date)
        val isGoalStillMet = newTotal >= goalMl

        // Revoke base points for the deleted amount
        val basePoints = pointsCalculator.calculateBasePoints(log.amountMl)
        settingsRepository.subtractPoints(basePoints)
        var totalPointsRevoked = basePoints

        val goalNowUnmet = wasGoalMet && !isGoalStillMet

        if (goalNowUnmet) {
            // Goal was met but now isn't - revoke bonuses
            val record = waterRepository.getDailyRecord(date)
            if (record != null && record.bonusAwarded) {
                val allRecords = waterRepository.getAllDailyRecordsOnce()
                val streakAtDate = com.hydrotrack.domain.StreakCalculator()
                    .getStreakAtDate(allRecords, date)
                val bonusPoints = pointsCalculator.calculateGoalReachedBonus(streakAtDate)
                settingsRepository.subtractPoints(bonusPoints)
                totalPointsRevoked += bonusPoints

                // Update record
                waterRepository.updateDailyRecord(
                    record.copy(
                        totalMl = newTotal,
                        goalMet = false,
                        bonusAwarded = false,
                        streakBonusAwarded = false
                    )
                )
            }
        } else {
            // Update record total
            val record = waterRepository.getDailyRecord(date)
            if (record != null) {
                waterRepository.updateDailyRecord(record.copy(totalMl = newTotal))
            }
        }

        return Result(
            newTotal = newTotal,
            goalMl = goalMl,
            goalNowUnmet = goalNowUnmet,
            pointsRevoked = totalPointsRevoked
        )
    }
}
