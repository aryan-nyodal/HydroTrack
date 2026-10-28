package com.hydrotrack.data.repository

import com.hydrotrack.data.datastore.SettingsDataStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: SettingsDataStore
) {
    val dailyGoal: Flow<Int> = dataStore.dailyGoal
    val reminderInterval: Flow<Int> = dataStore.reminderInterval
    val activeHourStart: Flow<Int> = dataStore.activeHourStart
    val activeHourEnd: Flow<Int> = dataStore.activeHourEnd
    val notificationsEnabled: Flow<Boolean> = dataStore.notificationsEnabled
    val streakRemindersEnabled: Flow<Boolean> = dataStore.streakRemindersEnabled
    val streakReminderHour: Flow<Int> = dataStore.streakReminderHour
    val streakReminderMinute: Flow<Int> = dataStore.streakReminderMinute
    val themeMode: Flow<String> = dataStore.themeMode
    val useFlOz: Flow<Boolean> = dataStore.useFlOz
    val onboardingComplete: Flow<Boolean> = dataStore.onboardingComplete
    val totalPoints: Flow<Long> = dataStore.totalPoints
    val streakBrokenShown: Flow<String> = dataStore.streakBrokenShown

    suspend fun setDailyGoal(goal: Int) = dataStore.setDailyGoal(goal)
    suspend fun setReminderInterval(minutes: Int) = dataStore.setReminderInterval(minutes)
    suspend fun setActiveHours(start: Int, end: Int) = dataStore.setActiveHours(start, end)
    suspend fun setNotificationsEnabled(enabled: Boolean) = dataStore.setNotificationsEnabled(enabled)
    suspend fun setStreakRemindersEnabled(enabled: Boolean) = dataStore.setStreakRemindersEnabled(enabled)
    suspend fun setStreakReminderTime(hour: Int, minute: Int) = dataStore.setStreakReminderTime(hour, minute)
    suspend fun setThemeMode(mode: String) = dataStore.setThemeMode(mode)
    suspend fun setUseFlOz(useFlOz: Boolean) = dataStore.setUseFlOz(useFlOz)
    suspend fun setOnboardingComplete(complete: Boolean) = dataStore.setOnboardingComplete(complete)
    suspend fun addPoints(points: Long) = dataStore.addPoints(points)
    suspend fun subtractPoints(points: Long) = dataStore.subtractPoints(points)
    suspend fun setTotalPoints(points: Long) = dataStore.setTotalPoints(points)
    suspend fun setStreakBrokenShown(date: String) = dataStore.setStreakBrokenShown(date)
    suspend fun clearAll() = dataStore.clearAll()
}
