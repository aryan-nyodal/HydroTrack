package com.hydrotrack.domain

import com.hydrotrack.data.local.entity.DailyRecordEntity
import com.hydrotrack.domain.model.StreakInfo
import com.hydrotrack.util.DateUtils
import java.time.LocalDate
import javax.inject.Inject

class StreakCalculator @Inject constructor() {

    /**
     * Computes streak info from daily records.
     * Rules:
     * - A day is "successful" if goalMet = true
     * - Consecutive successful days form a streak
     * - If a full day ended without goal being met, streak resets
     * - Today is special: if goal not met yet, streak is "at risk" but not broken
     */
    fun calculateStreak(
        records: List<DailyRecordEntity>,
        todayDate: String = DateUtils.today(),
        todayTotal: Int = 0,
        todayGoal: Int = 2500
    ): StreakInfo {
        if (records.isEmpty()) {
            return StreakInfo(
                currentStreak = 0,
                longestStreak = 0,
                totalSuccessfulDays = 0,
                isAtRisk = false,
                todayGoalMet = todayTotal >= todayGoal,
                remainingMl = maxOf(0, todayGoal - todayTotal)
            )
        }

        // Sort records by date descending
        val sortedRecords = records.sortedByDescending { it.date }
        val recordMap = sortedRecords.associateBy { it.date }

        val todayGoalMet = todayTotal >= todayGoal
        val totalSuccessfulDays = records.count { it.goalMet }

        // Calculate current streak
        var currentStreak = 0
        var checkDate = LocalDate.parse(todayDate)

        // Check today first
        val todayRecord = recordMap[todayDate]
        if (todayRecord != null && todayRecord.goalMet) {
            currentStreak = 1
            checkDate = checkDate.minusDays(1)
        } else if (todayGoalMet) {
            // Today's total meets goal even if record not yet updated
            currentStreak = 1
            checkDate = checkDate.minusDays(1)
        } else {
            // Today's goal not met yet - check if yesterday started a streak
            // Today is still in progress, so we check from yesterday
            checkDate = checkDate.minusDays(1)
        }

        // Walk backwards through consecutive days
        while (true) {
            val dateStr = DateUtils.formatDate(checkDate)
            val record = recordMap[dateStr]
            if (record != null && record.goalMet) {
                currentStreak++
                checkDate = checkDate.minusDays(1)
            } else {
                break
            }
        }

        // If today's goal not met but there's a streak from previous days, mark at risk
        val isAtRisk = !todayGoalMet && currentStreak > 0

        // Calculate longest streak from all records
        val longestStreak = calculateLongestStreak(records)

        return StreakInfo(
            currentStreak = currentStreak,
            longestStreak = maxOf(longestStreak, currentStreak),
            totalSuccessfulDays = totalSuccessfulDays + if (todayGoalMet && todayRecord?.goalMet != true) 1 else 0,
            isAtRisk = isAtRisk,
            todayGoalMet = todayGoalMet,
            remainingMl = maxOf(0, todayGoal - todayTotal)
        )
    }

    /**
     * Calculate the longest streak from historical records.
     */
    fun calculateLongestStreak(records: List<DailyRecordEntity>): Int {
        if (records.isEmpty()) return 0

        val sortedDates = records
            .filter { it.goalMet }
            .map { LocalDate.parse(it.date) }
            .sorted()

        if (sortedDates.isEmpty()) return 0

        var longest = 1
        var current = 1

        for (i in 1 until sortedDates.size) {
            if (sortedDates[i] == sortedDates[i - 1].plusDays(1)) {
                current++
                longest = maxOf(longest, current)
            } else {
                current = 1
            }
        }

        return longest
    }

    /**
     * Check if yesterday's goal was missed (for midnight worker).
     * Returns true if the streak should be broken.
     */
    fun shouldBreakStreak(
        yesterdayRecord: DailyRecordEntity?
    ): Boolean {
        return yesterdayRecord == null || !yesterdayRecord.goalMet
    }

    /**
     * Get the streak count at a specific date.
     */
    fun getStreakAtDate(
        records: List<DailyRecordEntity>,
        targetDate: String
    ): Int {
        val recordMap = records.associateBy { it.date }
        var streak = 0
        var checkDate = LocalDate.parse(targetDate)

        while (true) {
            val dateStr = DateUtils.formatDate(checkDate)
            val record = recordMap[dateStr]
            if (record != null && record.goalMet) {
                streak++
                checkDate = checkDate.minusDays(1)
            } else {
                break
            }
        }

        return streak
    }
}
