package com.hydrotrack.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.hydrotrack.data.repository.SettingsRepository
import com.hydrotrack.data.repository.WaterRepository
import com.hydrotrack.domain.StreakCalculator
import com.hydrotrack.domain.usecase.UpdateDailyRecordUseCase
import com.hydrotrack.util.Constants
import com.hydrotrack.util.DateUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

@HiltWorker
class MidnightStreakWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val waterRepository: WaterRepository,
    private val settingsRepository: SettingsRepository,
    private val streakCalculator: StreakCalculator,
    private val updateDailyRecordUseCase: UpdateDailyRecordUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val yesterday = DateUtils.yesterday()
            val today = DateUtils.today()

            // Ensure yesterday's record is finalized
            val yesterdayRecord = waterRepository.getDailyRecord(yesterday)
            if (yesterdayRecord != null) {
                val yesterdayTotal = waterRepository.getTotalForDate(yesterday)
                val wasGoalMet = yesterdayTotal >= yesterdayRecord.goalMl
                if (yesterdayRecord.goalMet != wasGoalMet || yesterdayRecord.totalMl != yesterdayTotal) {
                    waterRepository.updateDailyRecord(
                        yesterdayRecord.copy(
                            totalMl = yesterdayTotal,
                            goalMet = wasGoalMet
                        )
                    )
                }
            }

            // Ensure today's record is initialized
            updateDailyRecordUseCase(today)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        fun enqueue(context: Context) {
            val now = LocalDateTime.now()
            val midnight = LocalDateTime.of(now.toLocalDate().plusDays(1), LocalTime.MIDNIGHT)
            val initialDelayMinutes = Duration.between(now, midnight).toMinutes()

            val workRequest = PeriodicWorkRequestBuilder<MidnightStreakWorker>(
                repeatInterval = 24,
                repeatIntervalTimeUnit = TimeUnit.HOURS,
                flexTimeInterval = 15,
                flexTimeIntervalUnit = TimeUnit.MINUTES
            )
                .setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                Constants.WORK_MIDNIGHT_STREAK,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }
    }
}
