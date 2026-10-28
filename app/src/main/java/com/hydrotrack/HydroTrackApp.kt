package com.hydrotrack

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.hydrotrack.notifications.NotificationHelper
import com.hydrotrack.notifications.ReminderScheduler
import com.hydrotrack.work.MidnightStreakWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class HydroTrackApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // Create notification channels
        notificationHelper.createNotificationChannels()

        // Schedule midnight streak check work
        MidnightStreakWorker.enqueue(this)

        // Reschedule reminders on app startup
        CoroutineScope(Dispatchers.IO).launch {
            try {
                reminderScheduler.rescheduleAll()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
