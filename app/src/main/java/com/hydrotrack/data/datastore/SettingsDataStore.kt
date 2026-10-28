package com.hydrotrack.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.hydrotrack.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hydro_settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val DAILY_GOAL = intPreferencesKey("daily_goal")
        val REMINDER_INTERVAL = intPreferencesKey("reminder_interval")
        val ACTIVE_HOUR_START = intPreferencesKey("active_hour_start")
        val ACTIVE_HOUR_END = intPreferencesKey("active_hour_end")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val STREAK_REMINDERS_ENABLED = booleanPreferencesKey("streak_reminders_enabled")
        val STREAK_REMINDER_HOUR = intPreferencesKey("streak_reminder_hour")
        val STREAK_REMINDER_MINUTE = intPreferencesKey("streak_reminder_minute")
        val THEME_MODE = stringPreferencesKey("theme_mode") // "light", "dark", "system"
        val USE_FL_OZ = booleanPreferencesKey("use_fl_oz")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val TOTAL_POINTS = longPreferencesKey("total_points")
        val STREAK_BROKEN_SHOWN = stringPreferencesKey("streak_broken_shown") // date when shown
    }

    val dailyGoal: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[DAILY_GOAL] ?: Constants.DEFAULT_GOAL_ML
    }

    val reminderInterval: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[REMINDER_INTERVAL] ?: Constants.DEFAULT_REMINDER_INTERVAL_MINUTES
    }

    val activeHourStart: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[ACTIVE_HOUR_START] ?: Constants.DEFAULT_ACTIVE_HOUR_START
    }

    val activeHourEnd: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[ACTIVE_HOUR_END] ?: Constants.DEFAULT_ACTIVE_HOUR_END
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[NOTIFICATIONS_ENABLED] ?: true
    }

    val streakRemindersEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[STREAK_REMINDERS_ENABLED] ?: true
    }

    val streakReminderHour: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[STREAK_REMINDER_HOUR] ?: Constants.DEFAULT_STREAK_REMINDER_HOUR
    }

    val streakReminderMinute: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[STREAK_REMINDER_MINUTE] ?: Constants.DEFAULT_STREAK_REMINDER_MINUTE
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[THEME_MODE] ?: "system"
    }

    val useFlOz: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[USE_FL_OZ] ?: false
    }

    val onboardingComplete: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ONBOARDING_COMPLETE] ?: false
    }

    val totalPoints: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[TOTAL_POINTS] ?: 0L
    }

    val streakBrokenShown: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[STREAK_BROKEN_SHOWN] ?: ""
    }

    suspend fun setDailyGoal(goal: Int) {
        context.dataStore.edit { it[DAILY_GOAL] = goal }
    }

    suspend fun setReminderInterval(minutes: Int) {
        context.dataStore.edit { it[REMINDER_INTERVAL] = minutes }
    }

    suspend fun setActiveHours(start: Int, end: Int) {
        context.dataStore.edit {
            it[ACTIVE_HOUR_START] = start
            it[ACTIVE_HOUR_END] = end
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setStreakRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { it[STREAK_REMINDERS_ENABLED] = enabled }
    }

    suspend fun setStreakReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit {
            it[STREAK_REMINDER_HOUR] = hour
            it[STREAK_REMINDER_MINUTE] = minute
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[THEME_MODE] = mode }
    }

    suspend fun setUseFlOz(useFlOz: Boolean) {
        context.dataStore.edit { it[USE_FL_OZ] = useFlOz }
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { it[ONBOARDING_COMPLETE] = complete }
    }

    suspend fun setTotalPoints(points: Long) {
        context.dataStore.edit { it[TOTAL_POINTS] = points }
    }

    suspend fun addPoints(points: Long) {
        context.dataStore.edit { prefs ->
            val current = prefs[TOTAL_POINTS] ?: 0L
            prefs[TOTAL_POINTS] = current + points
        }
    }

    suspend fun subtractPoints(points: Long) {
        context.dataStore.edit { prefs ->
            val current = prefs[TOTAL_POINTS] ?: 0L
            prefs[TOTAL_POINTS] = maxOf(0L, current - points)
        }
    }

    suspend fun setStreakBrokenShown(date: String) {
        context.dataStore.edit { it[STREAK_BROKEN_SHOWN] = date }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
