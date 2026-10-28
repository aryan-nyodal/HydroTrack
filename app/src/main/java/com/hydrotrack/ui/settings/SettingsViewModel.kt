package com.hydrotrack.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hydrotrack.data.local.entity.DailyRecordEntity
import com.hydrotrack.data.local.entity.WaterLogEntity
import com.hydrotrack.data.repository.SettingsRepository
import com.hydrotrack.data.repository.WaterRepository
import com.hydrotrack.domain.usecase.UpdateDailyRecordUseCase
import com.hydrotrack.notifications.NotificationHelper
import com.hydrotrack.notifications.ReminderScheduler
import com.hydrotrack.util.Constants
import com.hydrotrack.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val dailyGoal: Int = Constants.DEFAULT_GOAL_ML,
    val reminderInterval: Int = Constants.DEFAULT_REMINDER_INTERVAL_MINUTES,
    val activeHourStart: Int = Constants.DEFAULT_ACTIVE_HOUR_START,
    val activeHourEnd: Int = Constants.DEFAULT_ACTIVE_HOUR_END,
    val notificationsEnabled: Boolean = true,
    val streakRemindersEnabled: Boolean = true,
    val streakReminderHour: Int = Constants.DEFAULT_STREAK_REMINDER_HOUR,
    val streakReminderMinute: Int = Constants.DEFAULT_STREAK_REMINDER_MINUTE,
    val themeMode: String = "system",
    val useFlOz: Boolean = false,
    val showResetDialog: Boolean = false,
    val showGoalDialog: Boolean = false,
    val isDebugMode: Boolean = true // default enabled so streak simulation can be easily tested
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val waterRepository: WaterRepository,
    private val updateDailyRecordUseCase: UpdateDailyRecordUseCase,
    private val reminderScheduler: ReminderScheduler,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                settingsRepository.dailyGoal,
                settingsRepository.reminderInterval,
                settingsRepository.activeHourStart,
                settingsRepository.activeHourEnd,
                settingsRepository.notificationsEnabled
            ) { goal, interval, start, end, notifEnabled ->
                _uiState.update {
                    it.copy(
                        dailyGoal = goal,
                        reminderInterval = interval,
                        activeHourStart = start,
                        activeHourEnd = end,
                        notificationsEnabled = notifEnabled
                    )
                }
            }.collect {}
        }

        viewModelScope.launch {
            combine(
                settingsRepository.streakRemindersEnabled,
                settingsRepository.streakReminderHour,
                settingsRepository.streakReminderMinute,
                settingsRepository.themeMode,
                settingsRepository.useFlOz
            ) { streakEnabled, sHour, sMin, theme, flOz ->
                _uiState.update {
                    it.copy(
                        streakRemindersEnabled = streakEnabled,
                        streakReminderHour = sHour,
                        streakReminderMinute = sMin,
                        themeMode = theme,
                        useFlOz = flOz
                    )
                }
            }.collect {}
        }
    }

    fun updateGoal(newGoal: Int) {
        viewModelScope.launch {
            settingsRepository.setDailyGoal(newGoal)
            updateDailyRecordUseCase()
            reminderScheduler.scheduleNextReminder()
            _uiState.update { it.copy(showGoalDialog = false) }
        }
    }

    fun updateReminderInterval(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.setReminderInterval(minutes)
            reminderScheduler.scheduleNextReminder()
        }
    }

    fun updateActiveHours(start: Int, end: Int) {
        viewModelScope.launch {
            settingsRepository.setActiveHours(start, end)
            reminderScheduler.scheduleNextReminder()
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
            if (enabled) {
                reminderScheduler.scheduleNextReminder()
            } else {
                reminderScheduler.cancelReminder()
            }
        }
    }

    fun toggleStreakReminders(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setStreakRemindersEnabled(enabled)
            if (enabled) {
                reminderScheduler.scheduleStreakReminder()
            } else {
                reminderScheduler.cancelStreakReminder()
            }
        }
    }

    fun updateStreakReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            settingsRepository.setStreakReminderTime(hour, minute)
            reminderScheduler.scheduleStreakReminder()
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun toggleUnit(useFlOz: Boolean) {
        viewModelScope.launch {
            settingsRepository.setUseFlOz(useFlOz)
        }
    }

    fun sendTestReminder() {
        notificationHelper.showNotification(
            Constants.NOTIFICATION_ID_REMINDER,
            notificationHelper.buildReminderNotification()
        )
    }

    fun showResetDialog() {
        _uiState.update { it.copy(showResetDialog = true) }
    }

    fun hideResetDialog() {
        _uiState.update { it.copy(showResetDialog = false) }
    }

    fun showGoalDialog() {
        _uiState.update { it.copy(showGoalDialog = true) }
    }

    fun hideGoalDialog() {
        _uiState.update { it.copy(showGoalDialog = false) }
    }

    fun resetData() {
        viewModelScope.launch {
            waterRepository.deleteAllData()
            settingsRepository.setTotalPoints(0)
            updateDailyRecordUseCase()
            _uiState.update { it.copy(showResetDialog = false) }
        }
    }

    /**
     * Debug feature to simulate past days and test streak calculation
     */
    fun simulatePreviousDay(daysAgo: Int, amountMl: Int) {
        viewModelScope.launch {
            val date = DateUtils.daysAgo(daysAgo.toLong())
            val goal = settingsRepository.dailyGoal.first()
            val isMet = amountMl >= goal

            // Insert simulated water log
            waterRepository.logWater(amountMl, date)

            // Update daily record for that day
            waterRepository.updateDailyRecord(
                DailyRecordEntity(
                    date = date,
                    goalMl = goal,
                    totalMl = amountMl,
                    goalMet = isMet,
                    bonusAwarded = isMet,
                    streakBonusAwarded = isMet
                )
            )
        }
    }
}
