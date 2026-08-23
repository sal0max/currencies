package de.salomax.currencies.model.adapter

import de.salomax.currencies.model.ApiProvider
import de.salomax.currencies.model.Currency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.time.LocalDate

class NationalBankKazakhstanTimelineAdapterTest {

    @Test
    fun crossRatesTest() {
        val timeline = NationalBankKazakhstanTimelineAdapter(Currency.RUB, Currency.USD)
            .parse(graphHtml(datasets = """
                {"label":"RUB","data":[5.5,5.6]},
                {"label":"USD","data":[500.0,510.0]}
            """.trimIndent()))

        assertTrue(timeline.success == true)
        assertEquals(ApiProvider.NATIONAL_BANK_KAZAKHSTAN, timeline.provider)
        assertEquals(Currency.RUB.iso4217Alpha(), timeline.base)
        assertEquals(LocalDate.parse("2026-08-01"), timeline.startDate)
        assertEquals(LocalDate.parse("2026-08-02"), timeline.endDate)
        assertEquals(5.5f / 500f, timeline.rates?.get(LocalDate.parse("2026-08-01"))?.value ?: 0f, 0.0000001f)
        assertEquals(5.6f / 510f, timeline.rates?.get(LocalDate.parse("2026-08-02"))?.value ?: 0f, 0.0000001f)
    }

    @Test
    fun quantitiesAndKztBaseTest() {
        val timeline = NationalBankKazakhstanTimelineAdapter(Currency.KZT, Currency.KRW)
            .parse(graphHtml(datasets = """{"label":"KRW","data":[40.0,50.0]}"""))

        assertEquals(2.5f, timeline.rates?.get(LocalDate.parse("2026-08-01"))?.value ?: 0f, 0.0000001f)
        assertEquals(2f, timeline.rates?.get(LocalDate.parse("2026-08-02"))?.value ?: 0f, 0.0000001f)
    }

    @Test
    fun fokUsesDkkRatesTest() {
        val timeline = NationalBankKazakhstanTimelineAdapter(Currency.KZT, Currency.FOK)
            .parse(graphHtml(datasets = """{"label":"DKK","data":[80.0,100.0]}"""))

        assertEquals(1f / 80f, timeline.rates?.get(LocalDate.parse("2026-08-01"))?.value ?: 0f, 0.0000001f)
        assertEquals(1f / 100f, timeline.rates?.get(LocalDate.parse("2026-08-02"))?.value ?: 0f, 0.0000001f)
    }

    @Test
    fun missingGraphDataTest() {
        val timeline = NationalBankKazakhstanTimelineAdapter(Currency.RUB, Currency.USD)
            .parse(ByteArrayInputStream("<html></html>".toByteArray()))

        assertFalse(timeline.success == true)
        assertEquals("No data found.", timeline.error)
        assertTrue(timeline.rates?.isEmpty() == true)
        assertNull(timeline.startDate)
        assertNull(timeline.endDate)
    }

    @Test
    fun normalizationRejectsZeroQuoteTest() {
        assertNull(NationalBankKazakhstanRateConverter.normalize(1f, 0f))
        assertNull(NationalBankKazakhstanRateConverter.cross(0f, 1f))
    }

    private fun graphHtml(datasets: String): ByteArrayInputStream {
        val html = """
            <html><script>window["sharedData"]={
              "rates": {
                "labels": ["2026-08-01", "2026-08-02"],
                "datasets": [$datasets]
              }
            };window["sharedDataNamespace"]="sharedData";</script></html>
        """.trimIndent()
        return ByteArrayInputStream(html.toByteArray())
    }
}
