package com.hydrotrack.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hydrotrack.data.repository.SettingsRepository
import com.hydrotrack.data.repository.WaterRepository
import com.hydrotrack.domain.StreakCalculator
import com.hydrotrack.domain.usecase.GetHistoryUseCase
import com.hydrotrack.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

data class HistoryUiState(
    val weeklyData: List<Pair<String, Int>> = emptyList(),
    val dailyGoal: Int = 2500,
    val monthRecords: Map<String, Boolean> = emptyMap(),
    val currentStreakDates: Set<String> = emptySet(),
    val selectedMonth: YearMonth = YearMonth.now(),
    val todayTotal: Int = 0,
    val average7Days: Int = 0,
    val average30Days: Int = 0,
    val totalAllTime: Long = 0,
    val bestDay: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalSuccessfulDays: Int = 0,
    val useFlOz: Boolean = false
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val waterRepository: WaterRepository,
    private val settingsRepository: SettingsRepository,
    private val getHistoryUseCase: GetHistoryUseCase,
    private val streakCalculator: StreakCalculator
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val goal = settingsRepository.dailyGoal.first()
            val useFlOz = settingsRepository.useFlOz.first()
            _uiState.update { it.copy(dailyGoal = goal, useFlOz = useFlOz) }

            loadWeeklyData()
            loadMonthData(_uiState.value.selectedMonth)
            calculateStats()
        }
    }

    fun loadWeeklyData() {
        viewModelScope.launch {
            val weekDates = DateUtils.getWeekDates()
            val startDate = weekDates.first()
            val endDate = weekDates.last()

            val dailyTotals = getHistoryUseCase.getDailyTotals(startDate, endDate)
            val totalsMap = dailyTotals.associate { it.localDate to it.total }

            val weeklyData = weekDates.map { dateStr ->
                val date = LocalDate.parse(dateStr)
                val dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                val total = totalsMap[dateStr] ?: 0
                dayLabel to total
            }

            _uiState.update { it.copy(weeklyData = weeklyData) }
        }
    }

    fun loadMonthData(yearMonth: YearMonth) {
        viewModelScope.launch {
            val startDate = DateUtils.formatDate(yearMonth.atDay(1))
            val endDate = DateUtils.formatDate(yearMonth.atEndOfMonth())

            val records = getHistoryUseCase.getRecordsInRange(startDate, endDate)
            val monthRecords = records.associate { it.date to it.goalMet }

            // Build current streak dates by walking back from today
            val allRecords = getHistoryUseCase.getAllRecords()
            val todayTotal = waterRepository.getTodayTotalOnce()
            val goal = _uiState.value.dailyGoal
            val streakInfo = streakCalculator.calculateStreak(allRecords, DateUtils.today(), todayTotal, goal)

            val streakDates = mutableSetOf<String>()
            if (streakInfo.currentStreak > 0) {
                var checkDate = LocalDate.now()
                // If today's goal is met, start from today; otherwise start from yesterday
                if (!streakInfo.todayGoalMet) {
                    checkDate = checkDate.minusDays(1)
                }
                for (i in 0 until streakInfo.currentStreak) {
                    streakDates.add(DateUtils.formatDate(checkDate))
                    checkDate = checkDate.minusDays(1)
                }
            }

            _uiState.update {
                it.copy(
                    selectedMonth = yearMonth,
                    monthRecords = monthRecords,
                    currentStreakDates = streakDates
                )
            }
        }
    }

    fun navigateMonth(forward: Boolean) {
        val current = _uiState.value.selectedMonth
        val newMonth = if (forward) current.plusMonths(1) else current.minusMonths(1)
        _uiState.update { it.copy(selectedMonth = newMonth) }
        loadMonthData(newMonth)
    }

    fun calculateStats() {
        viewModelScope.launch {
            val allRecords = getHistoryUseCase.getAllRecords()
            val todayTotal = waterRepository.getTodayTotalOnce()
            val goal = _uiState.value.dailyGoal

            // 7-day average
            val sevenDaysAgo = DateUtils.daysAgo(6)
            val today = DateUtils.today()
            val last7Totals = getHistoryUseCase.getDailyTotals(sevenDaysAgo, today)
            val avg7 = if (last7Totals.isNotEmpty()) {
                last7Totals.sumOf { it.total } / last7Totals.size
            } else 0

            // 30-day average
            val thirtyDaysAgo = DateUtils.daysAgo(29)
            val last30Totals = getHistoryUseCase.getDailyTotals(thirtyDaysAgo, today)
            val avg30 = if (last30Totals.isNotEmpty()) {
                last30Totals.sumOf { it.total } / last30Totals.size
            } else 0

            // Total all time
            val totalAllTime = waterRepository.getTotalAllTime().first()

            // Best day
            val bestDay = allRecords.maxOfOrNull { it.totalMl } ?: 0

            // Streaks
            val streakInfo = streakCalculator.calculateStreak(allRecords, today, todayTotal, goal)

            _uiState.update {
                it.copy(
                    todayTotal = todayTotal,
                    average7Days = avg7,
                    average30Days = avg30,
                    totalAllTime = totalAllTime,
                    bestDay = bestDay,
                    currentStreak = streakInfo.currentStreak,
                    longestStreak = streakInfo.longestStreak,
                    totalSuccessfulDays = streakInfo.totalSuccessfulDays
                )
            }
        }
    }
}
