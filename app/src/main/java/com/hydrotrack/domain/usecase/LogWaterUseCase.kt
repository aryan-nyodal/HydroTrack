package com.hydrotrack.domain.usecase

import com.hydrotrack.data.local.entity.DailyRecordEntity
import com.hydrotrack.data.repository.SettingsRepository
import com.hydrotrack.data.repository.WaterRepository
import com.hydrotrack.domain.PointsCalculator
import com.hydrotrack.domain.StreakCalculator
import com.hydrotrack.util.DateUtils
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class LogWaterUseCase @Inject constructor(
    private val waterRepository: WaterRepository,
    private val settingsRepository: SettingsRepository,
    private val pointsCalculator: PointsCalculator,
    private val streakCalculator: StreakCalculator
) {
    data class Result(
        val logId: Long,
        val newTotal: Int,
        val goalMl: Int,
        val goalJustReached: Boolean,
        val streakCount: Int,
        val pointsEarned: Long
    )

    suspend operator fun invoke(amountMl: Int, date: String = DateUtils.today()): Result {
        // Log the water
        val logId = waterRepository.logWater(amountMl, date)

        // Get current state
        val goalMl = settingsRepository.dailyGoal.first()
        val previousTotal = waterRepository.getTotalForDate(date) // already includes new log since Room
        val newTotal = previousTotal

        // Calculate base points
        val basePoints = pointsCalculator.calculateBasePoints(amountMl)
        settingsRepository.addPoints(basePoints)
        var totalPointsEarned = basePoints

        // Update daily record
        val record = waterRepository.getOrCreateDailyRecord(date, goalMl)
        val wasGoalMet = record.goalMet
        val isGoalNowMet = newTotal >= goalMl

        var goalJustReached = false
        var streakCount = 0

        if (isGoalNowMet && !wasGoalMet) {
            // Goal just reached!
            goalJustReached = true

            // Calculate streak
            val allRecords = waterRepository.getAllDailyRecordsOnce()
            val streakInfo = streakCalculator.calculateStreak(allRecords, date, newTotal, goalMl)
            streakCount = streakInfo.currentStreak + 1 // +1 for today

            // Award bonuses
            val bonusPoints = pointsCalculator.calculateGoalReachedBonus(streakCount)
            settingsRepository.addPoints(bonusPoints)
            totalPointsEarned += bonusPoints

            // Update record
            waterRepository.updateDailyRecord(
                record.copy(
                    totalMl = newTotal,
                    goalMet = true,
                    bonusAwarded = true,
                    streakBonusAwarded = true
                )
            )
        } else {
            // Just update total
            waterRepository.updateDailyRecord(
                record.copy(totalMl = newTotal, goalMet = isGoalNowMet)
            )

            if (isGoalNowMet) {
                val allRecords = waterRepository.getAllDailyRecordsOnce()
                val streakInfo = streakCalculator.calculateStreak(allRecords, date, newTotal, goalMl)
                streakCount = streakInfo.currentStreak
            }
        }

        return Result(
            logId = logId,
            newTotal = newTotal,
            goalMl = goalMl,
            goalJustReached = goalJustReached,
            streakCount = streakCount,
            pointsEarned = totalPointsEarned
        )
    }
}
