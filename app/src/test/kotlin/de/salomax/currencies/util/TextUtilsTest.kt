package de.salomax.currencies.util

import android.content.Context
import de.salomax.currencies.R
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class TextUtilsTest {

    @Mock
    private lateinit var mockContext: Context

    @Before
    fun init() {
        `when`(mockContext.getString(R.string.decimal_separator)).thenReturn(".")
        `when`(mockContext.getString(R.string.thousands_separator)).thenReturn(" ")
    }

    @Test
    fun humanReadable() {
        // regular
        assertEquals(
            "123.0",
            123f.toHumanReadableNumber(mockContext, trim = false)
        )
        assertEquals(
            "123",
            123f.toHumanReadableNumber(mockContext, trim = true)
        )
        assertEquals(
            "123.45",
            123.446123f.toHumanReadableNumber(mockContext,  decimalPlaces = 2)
        )
        assertEquals(
            "+ 30.0 cm",
            30f.toHumanReadableNumber(mockContext,  showPositiveSign = true, suffix = "cm")
        )
        assertEquals(
            "12 345 678",
            "12345678".toHumanReadableNumber(mockContext)
        )
        assertEquals(
            "1 234.12312",
            "1234.12312".toHumanReadableNumber(mockContext)
        )
        assertEquals(
            "- 111 222",
            "-111222".toHumanReadableNumber(mockContext)
        )
    }

}
