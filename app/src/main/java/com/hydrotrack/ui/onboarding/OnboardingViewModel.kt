package com.hydrotrack.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hydrotrack.data.repository.SettingsRepository
import com.hydrotrack.notifications.ReminderScheduler
import com.hydrotrack.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val currentPage: Int = 0,
    val dailyGoal: Int = Constants.DEFAULT_GOAL_ML,
    val reminderInterval: Int = Constants.DEFAULT_REMINDER_INTERVAL_MINUTES,
    val activeHourStart: Int = Constants.DEFAULT_ACTIVE_HOUR_START,
    val activeHourEnd: Int = Constants.DEFAULT_ACTIVE_HOUR_END,
    val notificationsEnabled: Boolean = false,
    val isCompleting: Boolean = false
) {
    val totalPages: Int = 4
    val isFirstPage: Boolean get() = currentPage == 0
    val isLastPage: Boolean get() = currentPage == totalPages - 1
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun nextPage() {
        _uiState.update { state ->
            if (state.currentPage < state.totalPages - 1) {
                state.copy(currentPage = state.currentPage + 1)
            } else {
                state
            }
        }
    }

    fun previousPage() {
        _uiState.update { state ->
            if (state.currentPage > 0) {
                state.copy(currentPage = state.currentPage - 1)
            } else {
                state
            }
        }
    }

    fun setGoal(goal: Int) {
        val clampedGoal = goal.coerceIn(Constants.MIN_GOAL_ML, Constants.MAX_GOAL_ML)
        _uiState.update { it.copy(dailyGoal = clampedGoal) }
    }

    fun setInterval(minutes: Int) {
        _uiState.update { it.copy(reminderInterval = minutes) }
    }

    fun setActiveHours(start: Int, end: Int) {
        _uiState.update { it.copy(activeHourStart = start, activeHourEnd = end) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _uiState.update { it.copy(notificationsEnabled = enabled) }
    }

    fun completeOnboarding(onComplete: () -> Unit) {
        if (_uiState.value.isCompleting) return
        _uiState.update { it.copy(isCompleting = true) }

        viewModelScope.launch {
            val state = _uiState.value
            settingsRepository.setDailyGoal(state.dailyGoal)
            settingsRepository.setReminderInterval(state.reminderInterval)
            settingsRepository.setActiveHours(state.activeHourStart, state.activeHourEnd)
            settingsRepository.setNotificationsEnabled(state.notificationsEnabled)
            settingsRepository.setOnboardingComplete(true)

            if (state.notificationsEnabled) {
                reminderScheduler.scheduleNextReminder()
            }

            onComplete()
        }
    }
}
