package com.hydrotrack.domain

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PointsCalculatorTest {

    private lateinit var pointsCalculator: PointsCalculator

    @Before
    fun setup() {
        pointsCalculator = PointsCalculator()
    }

    @Test
    fun testBasePoints_calculation() {
        assertEquals(25L, pointsCalculator.calculateBasePoints(250))
        assertEquals(50L, pointsCalculator.calculateBasePoints(500))
        assertEquals(0L, pointsCalculator.calculateBasePoints(9))
        assertEquals(1L, pointsCalculator.calculateBasePoints(10))
        assertEquals(100L, pointsCalculator.calculateBasePoints(1000))
    }

    @Test
    fun testDailyBonus_isFifty() {
        assertEquals(50L, pointsCalculator.calculateDailyBonus())
    }

    @Test
    fun testStreakBonus_scalesAndCapsAtHundred() {
        assertEquals(10L, pointsCalculator.calculateStreakBonus(1))
        assertEquals(50L, pointsCalculator.calculateStreakBonus(5))
        assertEquals(100L, pointsCalculator.calculateStreakBonus(10))
        assertEquals(100L, pointsCalculator.calculateStreakBonus(15)) // capped at 100
        assertEquals(100L, pointsCalculator.calculateStreakBonus(100))
    }

    @Test
    fun testGoalReachedBonus_sumOfDailyAndStreak() {
        // Day 1 streak: 50 daily + 10 streak = 60
        assertEquals(60L, pointsCalculator.calculateGoalReachedBonus(1))

        // Day 5 streak: 50 daily + 50 streak = 100
        assertEquals(100L, pointsCalculator.calculateGoalReachedBonus(5))

        // Day 10+ streak: 50 daily + 100 max streak = 150
        assertEquals(150L, pointsCalculator.calculateGoalReachedBonus(10))
        assertEquals(150L, pointsCalculator.calculateGoalReachedBonus(25))
    }

    @Test
    fun testPointsToRevoke_matchesGoalReachedBonus() {
        val bonus = pointsCalculator.calculateGoalReachedBonus(4)
        val revoked = pointsCalculator.calculatePointsToRevoke(4)
        assertEquals(bonus, revoked)
    }
}
