package com.hydrotrack.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.hydrotrack.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val reminderChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_REMINDERS,
                "Water Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Periodic reminders to drink water"
                enableVibration(true)
            }

            val streakChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_STREAK,
                "Streak Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when your hydration streak is at risk"
                enableVibration(true)
            }

            val goalChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_GOAL,
                "Goal Celebrations",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Congratulations when you reach your daily goal"
            }

            notificationManager.createNotificationChannels(
                listOf(reminderChannel, streakChannel, goalChannel)
            )
        }
    }

    fun buildReminderNotification(): NotificationCompat.Builder {
        val message = ReminderMessages.getRandomMessage()
        return NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_menu_myplaces)
            .setContentTitle("💧 HydroTrack")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(buildLog250Action())
            .addAction(buildLog500Action())
            .addAction(buildCustomInputAction())
    }

    fun buildGoalCompleteNotification(streakCount: Int): NotificationCompat.Builder {
        val message = ReminderMessages.getRandomGoalCompleteMessage()
        val streakText = if (streakCount > 0) "\n🔥 Current streak: $streakCount day${if (streakCount > 1) "s" else ""}!" else ""
        val fullMessage = "$message$streakText"

        return NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_GOAL)
            .setSmallIcon(android.R.drawable.ic_menu_myplaces)
            .setContentTitle("🎉 Goal Complete!")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(fullMessage))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
    }

    fun buildStreakAtRiskNotification(streakCount: Int, remainingMl: Int): NotificationCompat.Builder {
        val message = ReminderMessages.getRandomStreakAtRiskMessage()
        val details = "$message\n\n🔥 $streakCount-day streak at risk!\n💧 Only $remainingMl ml left to reach your goal."

        return NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_STREAK)
            .setSmallIcon(android.R.drawable.ic_menu_myplaces)
            .setContentTitle("⚠️ Streak at Risk!")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(details))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(buildLog250Action())
            .addAction(buildLog500Action())
            .addAction(buildCustomInputAction())
    }

    fun buildConfirmationNotification(amountMl: Int, todayTotal: Int, goalMl: Int): NotificationCompat.Builder {
        val progress = ((todayTotal.toFloat() / goalMl) * 100).toInt().coerceAtMost(100)
        val remaining = maxOf(0, goalMl - todayTotal)
        val statusText = if (remaining <= 0) {
            "✅ +${amountMl} ml logged! Goal reached! 🎉"
        } else {
            "✅ +${amountMl} ml logged! ${remaining} ml remaining ($progress% done)"
        }

        return NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_menu_myplaces)
            .setContentTitle("Water Logged")
            .setContentText(statusText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(statusText))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setTimeoutAfter(5000)
    }

    fun buildMilestoneNotification(streakCount: Int): NotificationCompat.Builder {
        val message = ReminderMessages.getMilestoneMessage(streakCount)

        return NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_GOAL)
            .setSmallIcon(android.R.drawable.ic_menu_myplaces)
            .setContentTitle("🏆 Streak Milestone!")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
    }

    fun buildErrorNotification(message: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_menu_myplaces)
            .setContentTitle("❌ Invalid Input")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setTimeoutAfter(5000)
    }

    fun showNotification(notificationId: Int, builder: NotificationCompat.Builder) {
        notificationManager.notify(notificationId, builder.build())
    }

    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    private fun buildLog250Action(): NotificationCompat.Action {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = Constants.ACTION_LOG_250
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            250,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_myplaces,
            "250 ml",
            pendingIntent
        ).build()
    }

    private fun buildLog500Action(): NotificationCompat.Action {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = Constants.ACTION_LOG_500
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            500,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_myplaces,
            "500 ml",
            pendingIntent
        ).build()
    }

    private fun buildCustomInputAction(): NotificationCompat.Action {
        val remoteInput = RemoteInput.Builder(Constants.EXTRA_REMOTE_INPUT_KEY)
            .setLabel("Enter ml")
            .build()

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = Constants.ACTION_LOG_CUSTOM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        return NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_myplaces,
            "Custom",
            pendingIntent
        )
            .addRemoteInput(remoteInput)
            .build()
    }
}
