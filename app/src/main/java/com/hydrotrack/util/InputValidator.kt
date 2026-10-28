package com.hydrotrack.util

object InputValidator {
    fun isValidWaterAmount(amount: Int): Boolean {
        return amount in Constants.MIN_CUSTOM_AMOUNT..Constants.MAX_CUSTOM_AMOUNT
    }

    fun parseAndValidate(input: String?): Int? {
        if (input.isNullOrBlank()) return null
        val parsed = input.trim().toIntOrNull() ?: return null
        return if (isValidWaterAmount(parsed)) parsed else null
    }
}
