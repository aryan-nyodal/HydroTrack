package com.hydrotrack.domain.model

data class DailyRecord(
    val date: String,
    val goalMl: Int,
    val totalMl: Int,
    val goalMet: Boolean,
    val bonusAwarded: Boolean = false,
    val streakBonusAwarded: Boolean = false
)
