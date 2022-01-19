package de.salomax.currencies.viewmodel.main

import de.salomax.currencies.util.toHumanReadableNumber
import org.junit.Test
import org.junit.Assert.*

class CalculationsTest {

    @Test
    fun testToHumanReadableNumber() {
        // regular
        assertEquals("123", "123".toHumanReadableNumber())
        assertEquals("12,345,678", "12345678".toHumanReadableNumber())
        // decimal
        assertEquals("1,234.12312", "1234.12312".toHumanReadableNumber())
        // minus sign
        assertEquals("- 111,222", "-111222".toHumanReadableNumber())
        assertEquals("- 11,222", "-11222".toHumanReadableNumber())
        // positive sign
        assertEquals("+ 69", "69".toHumanReadableNumber(showPositiveSign = true))
        // NaN
        assertEquals("0", "NaN".toHumanReadableNumber())
        // rounding
        assertEquals("123,456,789.12", "1.23456789123456789E8".toHumanReadableNumber(decimalPlaces = 2))
        assertEquals("1.11", "1.111".toHumanReadableNumber(decimalPlaces = 2))
        assertEquals("2.22", "2.215".toHumanReadableNumber(decimalPlaces = 2))
        // take care of pending zeros
        assertEquals("6", "6.0".toHumanReadableNumber(trim = true))
        assertEquals("6.1", "6.10".toHumanReadableNumber(trim = true))
        // suffix
        assertEquals("69 %", "69".toHumanReadableNumber(suffix = "%"))
    }

}
