package com.hydrotrack.domain

import com.hydrotrack.util.Constants
import javax.inject.Inject

class PointsCalculator @Inject constructor() {

    /**
     * Calculate base points for a water amount.
     * 1 point per 10 ml.
     */
    fun calculateBasePoints(amountMl: Int): Long {
        return (amountMl / 10).toLong()
    }

    /**
     * Calculate the daily goal bonus.
     * +50 points when daily goal is reached, awarded once per day.
     */
    fun calculateDailyBonus(): Long {
        return Constants.DAILY_GOAL_BONUS.toLong()
    }

    /**
     * Calculate streak bonus when goal is hit.
     * +10 x current streak count, capped at +100.
     */
    fun calculateStreakBonus(currentStreak: Int): Long {
        return minOf(
            (currentStreak * Constants.STREAK_BONUS_PER_DAY).toLong(),
            Constants.MAX_STREAK_BONUS.toLong()
        )
    }

    /**
     * Calculate total bonus when goal is first reached today.
     * = daily bonus + streak bonus
     */
    fun calculateGoalReachedBonus(currentStreak: Int): Long {
        return calculateDailyBonus() + calculateStreakBonus(currentStreak)
    }

    /**
     * Calculate points to revoke when an entry is deleted and goal becomes unmet.
     * Revokes the daily bonus + streak bonus that were awarded.
     */
    fun calculatePointsToRevoke(currentStreak: Int): Long {
        return calculateGoalReachedBonus(currentStreak)
    }
}
