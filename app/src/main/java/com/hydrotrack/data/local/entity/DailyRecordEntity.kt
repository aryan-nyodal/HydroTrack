package com.hydrotrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_records")
data class DailyRecordEntity(
    @PrimaryKey
    val date: String, // yyyy-MM-dd
    val goalMl: Int,
    val totalMl: Int,
    val goalMet: Boolean,
    val bonusAwarded: Boolean = false,
    val streakBonusAwarded: Boolean = false
)
