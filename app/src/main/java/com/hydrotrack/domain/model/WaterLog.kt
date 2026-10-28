package com.hydrotrack.domain.model

data class WaterLog(
    val id: Long = 0,
    val amountMl: Int,
    val timestamp: Long,
    val localDate: String
)
