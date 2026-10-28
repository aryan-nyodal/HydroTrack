package com.hydrotrack.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InputValidationTest {

    @Test
    fun testValidAmounts() {
        assertTrue(InputValidator.isValidWaterAmount(1))
        assertTrue(InputValidator.isValidWaterAmount(250))
        assertTrue(InputValidator.isValidWaterAmount(500))
        assertTrue(InputValidator.isValidWaterAmount(5000))

        assertEquals(250, InputValidator.parseAndValidate("250"))
        assertEquals(1, InputValidator.parseAndValidate("1"))
        assertEquals(5000, InputValidator.parseAndValidate("5000"))
        assertEquals(350, InputValidator.parseAndValidate("  350  "))
    }

    @Test
    fun testInvalidAmounts() {
        assertFalse(InputValidator.isValidWaterAmount(0))
        assertFalse(InputValidator.isValidWaterAmount(-1))
        assertFalse(InputValidator.isValidWaterAmount(5001))
        assertFalse(InputValidator.isValidWaterAmount(100000))

        assertNull(InputValidator.parseAndValidate("0"))
        assertNull(InputValidator.parseAndValidate("-250"))
        assertNull(InputValidator.parseAndValidate("5001"))
        assertNull(InputValidator.parseAndValidate(""))
        assertNull(InputValidator.parseAndValidate("   "))
        assertNull(InputValidator.parseAndValidate("abc"))
        assertNull(InputValidator.parseAndValidate("250ml"))
        assertNull(InputValidator.parseAndValidate(null))
    }
}
