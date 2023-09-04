package de.salomax.currencies.util

import kotlin.math.absoluteValue

fun calculateDifference(old: Float?, new: Float?): Float? {
    return if (old == null || new == null)
        null
    else {
        val percentage = (new - old) / old * 100
        if (percentage.isFinite())
            percentage
        else
            null
    }
}

fun decimalPlaces(min: Float, max: Float): Int {
    return if (min == 0f || max == 0f)
        3
    else {
        val diff = (min - max).absoluteValue
        // x < 0.0001
        if (diff < 0.00001)
            7
        // 0.00001 <= x < 0.0001
        else if (diff < 0.0001)
            6
        // 0.0001 <= x < 0.001
        else if (diff < 0.001)
            5
        // 0.001 <= x < 0.01
        else if (diff < 0.01)
            4
        // x > 0.01
        else
            3
    }
}
