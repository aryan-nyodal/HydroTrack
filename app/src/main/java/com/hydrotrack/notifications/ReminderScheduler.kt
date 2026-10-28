package com.hydrotrack.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.hydrotrack.data.datastore.SettingsDataStore
import com.hydrotrack.util.Constants
import com.hydrotrack.util.DateUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsDataStore: SettingsDataStore
) {
    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    suspend fun scheduleNextReminder() {
        val enabled = settingsDataStore.notificationsEnabled.first()
        if (!enabled) {
            cancelReminder()
            return
        }

        val intervalMinutes = settingsDataStore.reminderInterval.first()
        val startHour = settingsDataStore.activeHourStart.first()
        val endHour = settingsDataStore.activeHourEnd.first()

        val nextTriggerMillis = calculateNextReminderTime(intervalMinutes, startHour, endHour)
        scheduleAlarm(nextTriggerMillis, Constants.ALARM_REQUEST_CODE)
    }

    suspend fun scheduleStreakReminder() {
        val enabled = settingsDataStore.streakRemindersEnabled.first()
        if (!enabled) {
            cancelStreakReminder()
            return
        }

        val hour = settingsDataStore.streakReminderHour.first()
        val minute = settingsDataStore.streakReminderMinute.first()

        var reminderTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(hour, minute))
        if (reminderTime.isBefore(LocalDateTime.now())) {
            reminderTime = reminderTime.plusDays(1)
        }

        val triggerMillis = reminderTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        scheduleAlarm(triggerMillis, Constants.STREAK_ALARM_REQUEST_CODE)
    }

    private fun calculateNextReminderTime(intervalMinutes: Int, startHour: Int, endHour: Int): Long {
        val now = LocalDateTime.now()
        val startTimeToday = LocalDateTime.of(now.toLocalDate(), LocalTime.of(startHour, 0))
        val endTimeToday = LocalDateTime.of(now.toLocalDate(), LocalTime.of(endHour, 0))

        val target = if (now.isBefore(startTimeToday)) {
            startTimeToday
        } else if (now.isAfter(endTimeToday) || now.isEqual(endTimeToday)) {
            startTimeToday.plusDays(1)
        } else {
            val candidate = now.plusMinutes(intervalMinutes.toLong())
            if (candidate.isAfter(endTimeToday)) {
                startTimeToday.plusDays(1)
            } else {
                candidate
            }
        }

        return target.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun scheduleAlarm(triggerMillis: Long, requestCode: Int) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = if (requestCode == Constants.ALARM_REQUEST_CODE) {
                "com.hydrotrack.ACTION_TRIGGER_REMINDER"
            } else {
                "com.hydrotrack.ACTION_TRIGGER_STREAK_REMINDER"
            }
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // In case of security exception on exact alarm permission
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        }
    }

    fun cancelReminder() {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.hydrotrack.ACTION_TRIGGER_REMINDER"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            Constants.ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun cancelStreakReminder() {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.hydrotrack.ACTION_TRIGGER_STREAK_REMINDER"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            Constants.STREAK_ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    suspend fun rescheduleAll() {
        scheduleNextReminder()
        scheduleStreakReminder()
    }
}
