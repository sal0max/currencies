package de.salomax.currencies.util

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.LocaleList
import de.salomax.currencies.R
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class TextUtilsTest {

    @Mock
    private lateinit var mockContext: Context

    @Before
    fun init() {
        // val resources: Resources = Mockito.mock(Resources::class.java)
        // `when`(mockContext.resources).thenReturn(resources)
        // val configuration: Configuration = Mockito.mock(Configuration::class.java)
        // `when`(mockContext.resources.configuration).thenReturn(configuration)
        // val localeList = Mockito.mock(LocaleList::class.java)
        // `when`(mockContext.resources.configuration.locales).thenReturn(localeList)
        // `when`(mockContext.resources.configuration.locales.get(0)).thenReturn(Locale.US)

        `when`(mockContext.getString(R.string.locale_language)).thenReturn("en")
        `when`(mockContext.getString(R.string.locale_country)).thenReturn("")
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
            123.44612f.toHumanReadableNumber(mockContext,  decimalPlaces = 2)
        )
        assertEquals(
            "+ 30.0 cm",
            30f.toHumanReadableNumber(mockContext,  showPositiveSign = true, suffix = "cm")
        )
        assertEquals(
            "12,345,678",
            "12345678".toHumanReadableNumber(mockContext)
        )
        assertEquals(
            "1,234.12312",
            "1234.12312".toHumanReadableNumber(mockContext)
        )
        assertEquals(
            "- 111,222",
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
    }

    @Test
    fun getDecimalSeparatorGetGroupingSeparator() {
        // default
        `when`(mockContext.getString(R.string.locale_language)).thenReturn("en")
        `when`(mockContext.getString(R.string.locale_country)).thenReturn("")
        assertEquals(".", getDecimalSeparator(mockContext))
        assertEquals(",", getGroupingSeparator(mockContext))

        // de
        `when`(mockContext.getString(R.string.locale_language)).thenReturn("de")
        `when`(mockContext.getString(R.string.locale_country)).thenReturn("")
        assertEquals(",", getDecimalSeparator(mockContext))
        assertEquals(".", getGroupingSeparator(mockContext))

        // pt-BR
        `when`(mockContext.getString(R.string.locale_language)).thenReturn("pt")
        `when`(mockContext.getString(R.string.locale_country)).thenReturn("br")
        assertEquals(",", getDecimalSeparator(mockContext))
        assertEquals(".", getGroupingSeparator(mockContext))

        // pt-PT
        `when`(mockContext.getString(R.string.locale_language)).thenReturn("pt")
        `when`(mockContext.getString(R.string.locale_country)).thenReturn("PT")
        assertEquals(",", getDecimalSeparator(mockContext))
        assertEquals(" ", getGroupingSeparator(mockContext))
    }

}
