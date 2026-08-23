package de.salomax.currencies.model.adapter

import com.squareup.moshi.Moshi
import de.salomax.currencies.model.ApiProvider
import de.salomax.currencies.model.Currency
import de.salomax.currencies.model.ExchangeRates
import de.salomax.currencies.model.Timeline
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.LocalDate

class AllRatesTodayAdaptersTest {

    @Test
    fun currentAndHistoricalRatesTest() {
        val requestedDate = LocalDate.parse("2026-08-01")
        val adapter = Moshi.Builder()
            .add(AllRatesTodayRatesAdapter(Currency.EUR, requestedDate))
            .build()
            .adapter(ExchangeRates::class.java)

        val rates = adapter.fromJson(
            """[
                {"rate":1.1723,"source":"EUR","target":"USD","time":"2026-08-01T23:00:00Z"},
                {"rate":7.4631,"source":"EUR","target":"DKK","time":"2026-08-01T23:00:00Z"},
                {"rate":999.0,"source":"USD","target":"EUR","time":"2026-08-01T23:00:00Z"}
            ]""".trimIndent()
        )

        assertNotNull(rates)
        assertEquals(ApiProvider.ALL_RATES_TODAY, rates?.provider)
        assertEquals(Currency.EUR, rates?.base)
        assertEquals(requestedDate, rates?.date)
        assertEquals(1f, rates?.rates?.single { it.currency == Currency.EUR }?.value)
        assertEquals(1.1723f, rates?.rates?.single { it.currency == Currency.USD }?.value)
        assertEquals(7.4631f, rates?.rates?.single { it.currency == Currency.FOK }?.value)
        assertEquals(4, rates?.rates?.size)
    }

    @Test
    fun timelineTest() {
        val startDate = LocalDate.parse("2026-08-01")
        val endDate = LocalDate.parse("2026-08-03")
        val adapter = Moshi.Builder()
            .add(
                AllRatesTodayTimelineAdapter(
                    Currency.USD,
                    Currency.EUR,
                    startDate,
                    endDate
                )
            )
            .build()
            .adapter(Timeline::class.java)

        val timeline = adapter.fromJson(
            """[
                {"rate":0.8534,"source":"USD","target":"EUR","time":"2026-08-02T00:00:00Z"},
                {"rate":0.8521,"source":"USD","target":"EUR","time":"2026-08-01T00:00:00Z"}
            ]""".trimIndent()
        )

        assertNotNull(timeline)
        assertEquals(ApiProvider.ALL_RATES_TODAY, timeline?.provider)
        assertEquals("USD", timeline?.base)
        assertEquals(startDate, timeline?.startDate)
        assertEquals(endDate, timeline?.endDate)
        assertEquals(0.8521f, timeline?.rates?.get(startDate)?.value)
        assertEquals(Currency.EUR, timeline?.rates?.get(startDate)?.currency)
        assertEquals(2, timeline?.rates?.size)
        assertEquals(listOf(startDate, startDate.plusDays(1)), timeline?.rates?.keys?.toList())
    }

    @Test
    fun fokTimelineUsesRequestedCurrencyTest() {
        val date = LocalDate.parse("2026-08-01")
        val adapter = Moshi.Builder()
            .add(AllRatesTodayTimelineAdapter(Currency.EUR, Currency.FOK, date, date))
            .build()
            .adapter(Timeline::class.java)

        val timeline = adapter.fromJson(
            """[{"rate":7.4631,"source":"EUR","target":"DKK","time":"2026-08-01T00:00:00Z"}]"""
        )

        assertEquals(Currency.FOK, timeline?.rates?.get(date)?.currency)
        assertEquals(7.4631f, timeline?.rates?.get(date)?.value)
    }
}
