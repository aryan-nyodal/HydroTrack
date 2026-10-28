package com.hydrotrack.util

object Constants {
    const val DEFAULT_GOAL_ML = 2500
    const val MIN_GOAL_ML = 500
    const val MAX_GOAL_ML = 8000
    const val MIN_CUSTOM_AMOUNT = 1
    const val MAX_CUSTOM_AMOUNT = 5000

    const val DEFAULT_REMINDER_INTERVAL_MINUTES = 60
    const val DEFAULT_ACTIVE_HOUR_START = 8
    const val DEFAULT_ACTIVE_HOUR_END = 22

    const val DEFAULT_STREAK_REMINDER_HOUR = 20
    const val DEFAULT_STREAK_REMINDER_MINUTE = 0

    const val POINTS_PER_10ML = 1
    const val DAILY_GOAL_BONUS = 50
    const val STREAK_BONUS_PER_DAY = 10
    const val MAX_STREAK_BONUS = 100

    const val NOTIFICATION_CHANNEL_REMINDERS = "water_reminders"
    const val NOTIFICATION_CHANNEL_STREAK = "streak_alerts"
    const val NOTIFICATION_CHANNEL_GOAL = "goal_complete"

    const val NOTIFICATION_ID_REMINDER = 1001
    const val NOTIFICATION_ID_STREAK_RISK = 1002
    const val NOTIFICATION_ID_GOAL_COMPLETE = 1003
    const val NOTIFICATION_ID_CONFIRMATION = 1004
    const val NOTIFICATION_ID_MILESTONE = 1005

    const val ACTION_LOG_250 = "com.hydrotrack.ACTION_LOG_250"
    const val ACTION_LOG_500 = "com.hydrotrack.ACTION_LOG_500"
    const val ACTION_LOG_CUSTOM = "com.hydrotrack.ACTION_LOG_CUSTOM"
    const val EXTRA_REMOTE_INPUT_KEY = "remote_input_ml"

    const val ALARM_REQUEST_CODE = 2001
    const val STREAK_ALARM_REQUEST_CODE = 2002

    const val WORK_MIDNIGHT_STREAK = "midnight_streak_check"

    val STREAK_MILESTONES = listOf(3, 7, 14, 30, 60, 100)

    val REMINDER_INTERVALS = listOf(30, 60, 90, 120)
}
