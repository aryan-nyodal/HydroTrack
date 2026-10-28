package com.hydrotrack.domain.model

data class UserSettings(
    val dailyGoalMl: Int = 2500,
    val reminderIntervalMinutes: Int = 60,
    val activeHourStart: Int = 8,
    val activeHourEnd: Int = 22,
    val notificationsEnabled: Boolean = true,
    val streakRemindersEnabled: Boolean = true,
    val streakReminderHour: Int = 20,
    val streakReminderMinute: Int = 0,
    val themeMode: String = "system",
    val useFlOz: Boolean = false,
    val onboardingComplete: Boolean = false
)
