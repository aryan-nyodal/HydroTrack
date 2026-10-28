package com.hydrotrack.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hydrotrack.data.local.entity.WaterLogEntity
import com.hydrotrack.data.repository.SettingsRepository
import com.hydrotrack.data.repository.WaterRepository
import com.hydrotrack.domain.StreakCalculator
import com.hydrotrack.domain.model.StreakInfo
import com.hydrotrack.domain.usecase.DeleteWaterLogUseCase
import com.hydrotrack.domain.usecase.GetTodayLogsUseCase
import com.hydrotrack.domain.usecase.LogWaterUseCase
import com.hydrotrack.domain.usecase.UpdateDailyRecordUseCase
import com.hydrotrack.ui.components.DayItem
import com.hydrotrack.ui.components.DayStatus
import com.hydrotrack.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

data class HomeUiState(
    val todayTotal: Int = 0,
    val dailyGoal: Int = 2500,
    val progress: Float = 0f,
    val todayLogs: List<WaterLogEntity> = emptyList(),
    val streakInfo: StreakInfo = StreakInfo(),
    val totalPoints: Long = 0,
    val weeklyStatus: List<DayItem> = emptyList(),
    val useFlOz: Boolean = false,
    val showConfetti: Boolean = false,
    val showCustomDialog: Boolean = false,
    val streakBrokenMessage: String? = null,
    val deletedLog: WaterLogEntity? = null,
    val snackbarMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val waterRepository: WaterRepository,
    private val settingsRepository: SettingsRepository,
    private val logWaterUseCase: LogWaterUseCase,
    private val deleteWaterLogUseCase: DeleteWaterLogUseCase,
    private val getTodayLogsUseCase: GetTodayLogsUseCase,
    private val updateDailyRecordUseCase: UpdateDailyRecordUseCase,
    private val streakCalculator: StreakCalculator
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            updateDailyRecordUseCase()
            checkStreakBroken()
            observeData()
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                getTodayLogsUseCase(),
                settingsRepository.dailyGoal,
                settingsRepository.totalPoints,
                settingsRepository.useFlOz
            ) { logs, goal, points, useFlOz ->
                val total = logs.sumOf { it.amountMl }
                val progress = if (goal > 0) total.toFloat() / goal else 0f
                val allRecords = waterRepository.getAllDailyRecordsOnce()
                val streak = streakCalculator.calculateStreak(
                    records = allRecords,
                    todayDate = DateUtils.today(),
                    todayTotal = total,
                    todayGoal = goal
                )
                val weeklyItems = computeWeeklyStatus(allRecords, total, goal)

                _uiState.update { current ->
                    current.copy(
                        todayTotal = total,
                        dailyGoal = goal,
                        progress = progress,
                        todayLogs = logs,
                        streakInfo = streak,
                        totalPoints = points,
                        weeklyStatus = weeklyItems,
                        useFlOz = useFlOz
                    )
                }
            }.collect {}
        }
    }

    private suspend fun computeWeeklyStatus(
        allRecords: List<com.hydrotrack.data.local.entity.DailyRecordEntity>,
        todayTotal: Int,
        todayGoal: Int
    ): List<DayItem> {
        val today = LocalDate.now()
        val recordMap = allRecords.associateBy { it.date }

        return (6 downTo 0).map { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            val dateStr = DateUtils.formatDate(date)
            val dayLabel = date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault())
            val isToday = daysAgo == 0

            val status = if (isToday) {
                if (todayTotal >= todayGoal) DayStatus.MET else DayStatus.IN_PROGRESS
            } else {
                val record = recordMap[dateStr]
                if (record != null && record.goalMet) DayStatus.MET else DayStatus.MISSED
            }

            DayItem(dayLabel = dayLabel, status = status, isToday = isToday)
        }
    }

    private suspend fun checkStreakBroken() {
        val allRecords = waterRepository.getAllDailyRecordsOnce()
        val yesterdayStr = DateUtils.yesterday()
        val yesterdayRecord = allRecords.find { it.date == yesterdayStr }

        if (yesterdayRecord != null && !yesterdayRecord.goalMet) {
            val shownDate = settingsRepository.streakBrokenShown.first()
            if (shownDate != yesterdayStr) {
                val longest = streakCalculator.calculateLongestStreak(allRecords)
                if (longest > 1) {
                    _uiState.update {
                        it.copy(
                            streakBrokenMessage = "Your streak ended yesterday, but your best was $longest days. You can start a fresh streak today!"
                        )
                    }
                    settingsRepository.setStreakBrokenShown(yesterdayStr)
                }
            }
        }
    }

    fun addWater(amount: Int) {
        viewModelScope.launch {
            val result = logWaterUseCase(amount)
            if (result.goalJustReached) {
                _uiState.update { it.copy(showConfetti = true) }
            }
        }
    }

    fun deleteLog(log: WaterLogEntity) {
        viewModelScope.launch {
            val result = deleteWaterLogUseCase(log)
            _uiState.update {
                it.copy(
                    deletedLog = log,
                    snackbarMessage = "Deleted ${log.amountMl} ml entry"
                )
            }
        }
    }

    fun undoDelete() {
        val logToRestore = _uiState.value.deletedLog ?: return
        viewModelScope.launch {
            logWaterUseCase(logToRestore.amountMl, logToRestore.localDate)
            _uiState.update { it.copy(deletedLog = null, snackbarMessage = null) }
        }
    }

    fun showCustomDialog() {
        _uiState.update { it.copy(showCustomDialog = true) }
    }

    fun hideCustomDialog() {
        _uiState.update { it.copy(showCustomDialog = false) }
    }

    fun dismissConfetti() {
        _uiState.update { it.copy(showConfetti = false) }
    }

    fun dismissStreakBroken() {
        _uiState.update { it.copy(streakBrokenMessage = null) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
