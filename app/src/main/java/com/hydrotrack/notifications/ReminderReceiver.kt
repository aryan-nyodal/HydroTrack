package com.hydrotrack.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.hydrotrack.data.repository.SettingsRepository
import com.hydrotrack.data.repository.WaterRepository
import com.hydrotrack.domain.StreakCalculator
import com.hydrotrack.domain.usecase.LogWaterUseCase
import com.hydrotrack.util.Constants
import com.hydrotrack.util.DateUtils
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ReminderReceiverEntryPoint {
        fun logWaterUseCase(): LogWaterUseCase
        fun notificationHelper(): NotificationHelper
        fun reminderScheduler(): ReminderScheduler
        fun waterRepository(): WaterRepository
        fun settingsRepository(): SettingsRepository
        fun streakCalculator(): StreakCalculator
    }

    override fun onReceive(context: Context, intent: Intent) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            ReminderReceiverEntryPoint::class.java
        )

        val logWaterUseCase = entryPoint.logWaterUseCase()
        val notificationHelper = entryPoint.notificationHelper()
        val reminderScheduler = entryPoint.reminderScheduler()
        val waterRepository = entryPoint.waterRepository()
        val settingsRepository = entryPoint.settingsRepository()
        val streakCalculator = entryPoint.streakCalculator()

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    Constants.ACTION_LOG_250 -> {
                        handleLogWater(250, logWaterUseCase, notificationHelper, reminderScheduler)
                    }

                    Constants.ACTION_LOG_500 -> {
                        handleLogWater(500, logWaterUseCase, notificationHelper, reminderScheduler)
                    }

                    Constants.ACTION_LOG_CUSTOM -> {
                        val remoteInput = RemoteInput.getResultsFromIntent(intent)
                        val inputStr = remoteInput?.getCharSequence(Constants.EXTRA_REMOTE_INPUT_KEY)?.toString()?.trim()

                        if (inputStr.isNullOrBlank()) {
                            notificationHelper.showNotification(
                                Constants.NOTIFICATION_ID_CONFIRMATION,
                                notificationHelper.buildErrorNotification("Amount cannot be empty.")
                            )
                        } else {
                            val parsedAmount = inputStr.toIntOrNull()
                            if (parsedAmount == null || parsedAmount < Constants.MIN_CUSTOM_AMOUNT || parsedAmount > Constants.MAX_CUSTOM_AMOUNT) {
                                notificationHelper.showNotification(
                                    Constants.NOTIFICATION_ID_CONFIRMATION,
                                    notificationHelper.buildErrorNotification(
                                        "Please enter a valid amount (${Constants.MIN_CUSTOM_AMOUNT}–${Constants.MAX_CUSTOM_AMOUNT} ml)"
                                    )
                                )
                            } else {
                                handleLogWater(parsedAmount, logWaterUseCase, notificationHelper, reminderScheduler)
                            }
                        }
                    }

                    "com.hydrotrack.ACTION_TRIGGER_REMINDER" -> {
                        val todayGoal = settingsRepository.dailyGoal.first()
                        val todayTotal = waterRepository.getTodayTotalOnce()

                        // Only remind if goal has not been reached yet
                        if (todayTotal < todayGoal) {
                            val startHour = settingsRepository.activeHourStart.first()
                            val endHour = settingsRepository.activeHourEnd.first()

                            if (DateUtils.isWithinActiveHours(startHour, endHour)) {
                                notificationHelper.showNotification(
                                    Constants.NOTIFICATION_ID_REMINDER,
                                    notificationHelper.buildReminderNotification()
                                )
                            }
                        }
                        // Schedule next reminder
                        reminderScheduler.scheduleNextReminder()
                    }

                    "com.hydrotrack.ACTION_TRIGGER_STREAK_REMINDER" -> {
                        val todayGoal = settingsRepository.dailyGoal.first()
                        val todayTotal = waterRepository.getTodayTotalOnce()

                        if (todayTotal < todayGoal) {
                            val records = waterRepository.getAllDailyRecordsOnce()
                            val streakInfo = streakCalculator.calculateStreak(
                                records = records,
                                todayTotal = todayTotal,
                                todayGoal = todayGoal
                            )

                            if (streakInfo.currentStreak > 0) {
                                notificationHelper.showNotification(
                                    Constants.NOTIFICATION_ID_STREAK_RISK,
                                    notificationHelper.buildStreakAtRiskNotification(
                                        streakInfo.currentStreak,
                                        streakInfo.remainingMl
                                    )
                                )
                            }
                        }
                        // Reschedule for tomorrow
                        reminderScheduler.scheduleStreakReminder()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleLogWater(
        amount: Int,
        logWaterUseCase: LogWaterUseCase,
        notificationHelper: NotificationHelper,
        reminderScheduler: ReminderScheduler
    ) {
        synchronized(lock) {
            // Synchronized block to prevent concurrent duplicate logging
        }

        val result = logWaterUseCase(amount)

        // Cancel previous reminder / streak notifications
        notificationHelper.cancelNotification(Constants.NOTIFICATION_ID_REMINDER)
        notificationHelper.cancelNotification(Constants.NOTIFICATION_ID_STREAK_RISK)

        // Show confirmation notification
        notificationHelper.showNotification(
            Constants.NOTIFICATION_ID_CONFIRMATION,
            notificationHelper.buildConfirmationNotification(
                amountMl = amount,
                todayTotal = result.newTotal,
                goalMl = result.goalMl
            )
        )

        // If goal just reached, celebrate!
        if (result.goalJustReached) {
            notificationHelper.showNotification(
                Constants.NOTIFICATION_ID_GOAL_COMPLETE,
                notificationHelper.buildGoalCompleteNotification(result.streakCount)
            )

            // Check milestone
            if (result.streakCount in Constants.STREAK_MILESTONES) {
                notificationHelper.showNotification(
                    Constants.NOTIFICATION_ID_MILESTONE,
                    notificationHelper.buildMilestoneNotification(result.streakCount)
                )
            }
        } else {
            // Re-schedule reminder
            reminderScheduler.scheduleNextReminder()
        }
    }

    companion object {
        private val lock = Any()
    }
}
