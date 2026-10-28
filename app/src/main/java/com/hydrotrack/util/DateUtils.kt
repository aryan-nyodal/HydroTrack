package com.hydrotrack.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateUtils {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE // yyyy-MM-dd

    fun today(): String = LocalDate.now().format(dateFormatter)

    fun yesterday(): String = LocalDate.now().minusDays(1).format(dateFormatter)

    fun formatDate(date: LocalDate): String = date.format(dateFormatter)

    fun parseDate(dateStr: String): LocalDate = LocalDate.parse(dateStr, dateFormatter)

    fun daysAgo(days: Long): String = LocalDate.now().minusDays(days).format(dateFormatter)

    fun isToday(dateStr: String): Boolean = dateStr == today()

    fun isYesterday(dateStr: String): Boolean = dateStr == yesterday()

    fun getDateRange(startDaysAgo: Long, endDaysAgo: Long = 0): Pair<String, String> {
        val start = LocalDate.now().minusDays(startDaysAgo).format(dateFormatter)
        val end = LocalDate.now().minusDays(endDaysAgo).format(dateFormatter)
        return start to end
    }

    fun getWeekDates(): List<String> {
        val today = LocalDate.now()
        return (6 downTo 0).map { today.minusDays(it.toLong()).format(dateFormatter) }
    }

    fun getMonthDates(yearMonth: java.time.YearMonth): List<LocalDate> {
        return (1..yearMonth.lengthOfMonth()).map { yearMonth.atDay(it) }
    }

    fun currentTimeMillis(): Long = System.currentTimeMillis()

    fun isWithinActiveHours(startHour: Int, endHour: Int): Boolean {
        val now = LocalTime.now()
        val start = LocalTime.of(startHour, 0)
        val end = LocalTime.of(endHour, 0)
        return !now.isBefore(start) && now.isBefore(end)
    }
}
