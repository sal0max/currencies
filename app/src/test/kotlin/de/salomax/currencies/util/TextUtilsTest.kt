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

    @Test
    fun toNumber() {
        assertEquals(
            123,
            "123".toNumber()?.toInt()
        )
        assertEquals(
            123123,
            "123 123".toNumber()?.toInt()
        )
        assertEquals(
            null,
            "123a1".toNumber()
        )
        assertEquals(
            null,
            "-123".toNumber()
        )
        assertEquals(
            null,
            "abcdef".toNumber()
        )
        assertEquals(
            null,
            "".toNumber()
        )
        // working for German locale
        // assertEquals(
        //     123.0001,
        //     "123,0001".toNumber()
        // )
        // // working for German locale
        // assertEquals(
        //     123123.1,
        //     "123.123,1".toNumber()
        // )
        // // working for German locale
        // assertEquals(
        //     11,
        //     "1.1".toNumber()?.toInt()
        // )
    }

}
