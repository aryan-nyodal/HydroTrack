package com.hydrotrack.domain

import com.hydrotrack.data.local.entity.DailyRecordEntity
import com.hydrotrack.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class StreakCalculatorTest {

    private lateinit var streakCalculator: StreakCalculator
    private val todayStr = "2026-10-28"
    private val today = LocalDate.parse(todayStr)

    @Before
    fun setup() {
        streakCalculator = StreakCalculator()
    }

    @Test
    fun testEmptyRecords_returnsZeroStreak() {
        val result = streakCalculator.calculateStreak(
            records = emptyList(),
            todayDate = todayStr,
            todayTotal = 0,
            todayGoal = 2500
        )

        assertEquals(0, result.currentStreak)
        assertEquals(0, result.longestStreak)
        assertEquals(0, result.totalSuccessfulDays)
        assertFalse(result.isAtRisk)
        assertFalse(result.todayGoalMet)
        assertEquals(2500, result.remainingMl)
    }

    @Test
    fun testSingleSuccessfulDay_streakIsOne() {
        val records = listOf(
            DailyRecordEntity(
                date = todayStr,
                goalMl = 2500,
                totalMl = 2500,
                goalMet = true
            )
        )

        val result = streakCalculator.calculateStreak(
            records = records,
            todayDate = todayStr,
            todayTotal = 2500,
            todayGoal = 2500
        )

        assertEquals(1, result.currentStreak)
        assertEquals(1, result.longestStreak)
        assertEquals(1, result.totalSuccessfulDays)
        assertTrue(result.todayGoalMet)
        assertFalse(result.isAtRisk)
    }

    @Test
    fun testConsecutiveDays_streakIncrements() {
        val records = (0..4).map { daysAgo ->
            val d = DateUtils.formatDate(today.minusDays(daysAgo.toLong()))
            DailyRecordEntity(
                date = d,
                goalMl = 2500,
                totalMl = 2500,
                goalMet = true
            )
        }

        val result = streakCalculator.calculateStreak(
            records = records,
            todayDate = todayStr,
            todayTotal = 2500,
            todayGoal = 2500
        )

        assertEquals(5, result.currentStreak)
        assertEquals(5, result.longestStreak)
        assertEquals(5, result.totalSuccessfulDays)
    }

    @Test
    fun testMissedDay_streakResets() {
        // Day 0 (today): met
        // Day 1 (yesterday): met
        // Day 2: missed
        // Day 3, 4, 5, 6: met (4 days streak historically)
        val records = listOf(
            DailyRecordEntity(date = DateUtils.formatDate(today), goalMl = 2500, totalMl = 2500, goalMet = true),
            DailyRecordEntity(date = DateUtils.formatDate(today.minusDays(1)), goalMl = 2500, totalMl = 2500, goalMet = true),
            DailyRecordEntity(date = DateUtils.formatDate(today.minusDays(2)), goalMl = 2500, totalMl = 1000, goalMet = false),
            DailyRecordEntity(date = DateUtils.formatDate(today.minusDays(3)), goalMl = 2500, totalMl = 2500, goalMet = true),
            DailyRecordEntity(date = DateUtils.formatDate(today.minusDays(4)), goalMl = 2500, totalMl = 2500, goalMet = true),
            DailyRecordEntity(date = DateUtils.formatDate(today.minusDays(5)), goalMl = 2500, totalMl = 2500, goalMet = true),
            DailyRecordEntity(date = DateUtils.formatDate(today.minusDays(6)), goalMl = 2500, totalMl = 2500, goalMet = true)
        )

        val result = streakCalculator.calculateStreak(
            records = records,
            todayDate = todayStr,
            todayTotal = 2500,
            todayGoal = 2500
        )

        assertEquals(2, result.currentStreak) // today + yesterday
        assertEquals(4, result.longestStreak) // Days 3..6
        assertEquals(6, result.totalSuccessfulDays)
    }

    @Test
    fun testTodayNotMetYet_streakAtRisk() {
        // Yesterday met, today 500/2500 ml -> streak continues from yesterday, marked at risk
        val records = listOf(
            DailyRecordEntity(date = DateUtils.formatDate(today.minusDays(1)), goalMl = 2500, totalMl = 2500, goalMet = true),
            DailyRecordEntity(date = DateUtils.formatDate(today.minusDays(2)), goalMl = 2500, totalMl = 2500, goalMet = true)
        )

        val result = streakCalculator.calculateStreak(
            records = records,
            todayDate = todayStr,
            todayTotal = 500,
            todayGoal = 2500
        )

        assertEquals(2, result.currentStreak)
        assertTrue(result.isAtRisk)
        assertFalse(result.todayGoalMet)
        assertEquals(2000, result.remainingMl)
    }

    @Test
    fun testGoalChangeDoesNotRewritePast() {
        // Yesterday's record has snapshot goal of 2000 ml and 2000 ml drank
        val yesterdayDate = DateUtils.formatDate(today.minusDays(1))
        val records = listOf(
            DailyRecordEntity(date = yesterdayDate, goalMl = 2000, totalMl = 2000, goalMet = true)
        )

        // Today user changed goal to 3500 ml
        val result = streakCalculator.calculateStreak(
            records = records,
            todayDate = todayStr,
            todayTotal = 0,
            todayGoal = 3500
        )

        // Yesterday remains successful despite today's goal being higher
        assertEquals(1, result.currentStreak)
        assertTrue(result.isAtRisk)
        assertEquals(3500, result.remainingMl)
    }

    @Test
    fun testAppNotOpenedForDays_streakResets() {
        // Last opened 5 days ago
        val records = listOf(
            DailyRecordEntity(date = DateUtils.formatDate(today.minusDays(5)), goalMl = 2500, totalMl = 2500, goalMet = true),
            DailyRecordEntity(date = DateUtils.formatDate(today.minusDays(6)), goalMl = 2500, totalMl = 2500, goalMet = true)
        )

        val result = streakCalculator.calculateStreak(
            records = records,
            todayDate = todayStr,
            todayTotal = 0,
            todayGoal = 2500
        )

        assertEquals(0, result.currentStreak)
        assertFalse(result.isAtRisk)
        assertEquals(2, result.longestStreak)
    }
}
