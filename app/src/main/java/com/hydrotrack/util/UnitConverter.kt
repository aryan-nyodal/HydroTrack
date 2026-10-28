package com.hydrotrack.util

object UnitConverter {
    private const val ML_TO_FL_OZ = 0.033814

    fun mlToFlOz(ml: Int): Double = ml * ML_TO_FL_OZ
    fun flOzToMl(flOz: Double): Int = (flOz / ML_TO_FL_OZ).toInt()

    fun formatAmount(ml: Int, useFlOz: Boolean): String {
        return if (useFlOz) {
            "%.1f fl oz".format(mlToFlOz(ml))
        } else {
            "$ml ml"
        }
    }

    fun formatAmountShort(ml: Int, useFlOz: Boolean): String {
        return if (useFlOz) {
            "%.0f".format(mlToFlOz(ml))
        } else {
            "$ml"
        }
    }

    fun unitLabel(useFlOz: Boolean): String = if (useFlOz) "fl oz" else "ml"
}
