package de.salomax.currencies.util

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class MathUtilsTest {

    @Test
    fun calculateDifferenceTest() {
        assertEquals(10f, calculateDifference(100f, 110f))
        assertEquals(-10f, calculateDifference(100f, 90f))
        assertEquals(null, calculateDifference(null, 1f))
        assertEquals(null, calculateDifference(1f, null))
        assertEquals(null, calculateDifference(null, null))
        assertEquals(null, calculateDifference(Float.POSITIVE_INFINITY, 1f))
        assertEquals(null, calculateDifference(Float.NEGATIVE_INFINITY, 1f))
    }

    @Test
    fun decimalPlacesTest() {
        assertEquals(3, decimalPlaces(1f, 100f)) // 99
        assertEquals(3, decimalPlaces(0.9f, 1f)) // 0.1
        assertEquals(4, decimalPlaces(0.991f, 1f)) // 0.009
        assertEquals(4, decimalPlaces(0.0026f, 0.0073f)) // 0.0047
        assertEquals(4, decimalPlaces(0.998f, 1f)) // 0.002
        assertEquals(5, decimalPlaces(0.9991f, 1f)) // 0.0009
        assertEquals(6, decimalPlaces(0.99991f, 1f)) // 0.00009
        assertEquals(7, decimalPlaces(0.999991f, 1f)) // 0.000009
        assertEquals(7, decimalPlaces(0.9999991f, 1f)) // 0.0000009
    }

}
