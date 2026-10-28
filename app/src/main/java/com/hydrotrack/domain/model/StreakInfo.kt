package com.hydrotrack.domain.model

data class StreakInfo(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalSuccessfulDays: Int = 0,
    val isAtRisk: Boolean = false,
    val todayGoalMet: Boolean = false,
    val remainingMl: Int = 0
)
