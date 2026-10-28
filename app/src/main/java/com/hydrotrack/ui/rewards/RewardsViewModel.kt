package com.hydrotrack.ui.rewards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hydrotrack.data.repository.SettingsRepository
import com.hydrotrack.data.repository.WaterRepository
import com.hydrotrack.domain.StreakCalculator
import com.hydrotrack.domain.model.Badge
import com.hydrotrack.domain.model.BadgeType
import com.hydrotrack.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

data class RewardsUiState(
    val totalPoints: Long = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val badges: List<Badge> = emptyList(),
    val newlyUnlockedBadge: BadgeType? = null
)

@HiltViewModel
class RewardsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val waterRepository: WaterRepository,
    private val streakCalculator: StreakCalculator
) : ViewModel() {

    private val _uiState = MutableStateFlow(RewardsUiState())
    val uiState: StateFlow<RewardsUiState> = _uiState.asStateFlow()

    init {
        loadDataAndEvaluateBadges()
    }

    fun loadDataAndEvaluateBadges() {
        viewModelScope.launch {
            val points = settingsRepository.totalPoints.first()
            val allRecords = waterRepository.getAllDailyRecordsOnce()
            val todayTotal = waterRepository.getTodayTotalOnce()
            val todayGoal = settingsRepository.dailyGoal.first()

            val streakInfo = streakCalculator.calculateStreak(
                records = allRecords,
                todayTotal = todayTotal,
                todayGoal = todayGoal
            )

            val successfulDays = allRecords.count { it.goalMet } + (if (streakInfo.todayGoalMet) 1 else 0)
            val longestStreak = streakInfo.longestStreak
            val totalAllTime = allRecords.sumOf { it.totalMl.toLong() } + todayTotal

            // Check timestamps for Early Bird and Night Owl
            val recentLogs = waterRepository.getTodayLogsOnce()
            var hasEarlyBird = false
            var hasNightOwl = false

            for (log in recentLogs) {
                val hour = Instant.ofEpochMilli(log.timestamp).atZone(ZoneId.systemDefault()).hour
                if (hour < 8) hasEarlyBird = true
                if (hour >= 22) hasNightOwl = true
            }

            val hasAnyLog = recentLogs.isNotEmpty() || allRecords.isNotEmpty()
            val hasWaterFountain = allRecords.any { it.totalMl >= 1000 } || todayTotal >= 1000

            val badges = BadgeType.values().map { type ->
                val isUnlocked = when (type) {
                    BadgeType.FIRST_SIP -> hasAnyLog
                    BadgeType.HYDRATION_HERO -> points >= 10000
                    BadgeType.EARLY_BIRD -> hasEarlyBird
                    BadgeType.GOAL_CRUSHER -> successfulDays >= 30
                    BadgeType.SPARK_3 -> longestStreak >= 3
                    BadgeType.WEEK_WARRIOR -> longestStreak >= 7
                    BadgeType.FORTNIGHT_FLOW -> longestStreak >= 14
                    BadgeType.MONTHLY_MASTER -> longestStreak >= 30
                    BadgeType.UNSTOPPABLE -> longestStreak >= 60
                    BadgeType.CENTURY_CLUB -> longestStreak >= 100
                    BadgeType.WATER_FOUNTAIN -> hasWaterFountain
                    BadgeType.OCEAN_DRINKER -> totalAllTime >= 100000
                    BadgeType.CONSISTENT -> longestStreak >= 7
                    BadgeType.NIGHT_OWL -> hasNightOwl
                }
                Badge(type = type, isUnlocked = isUnlocked)
            }

            _uiState.update {
                it.copy(
                    totalPoints = points,
                    currentStreak = streakInfo.currentStreak,
                    longestStreak = longestStreak,
                    badges = badges
                )
            }
        }
    }

    fun dismissNewBadge() {
        _uiState.update { it.copy(newlyUnlockedBadge = null) }
    }
}
