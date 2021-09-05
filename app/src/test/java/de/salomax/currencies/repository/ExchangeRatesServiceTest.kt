package de.salomax.currencies.repository

import de.salomax.currencies.model.ExchangeRates
import de.salomax.currencies.model.Timeline
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class ExchangeRatesServiceTest {

    @Test
    fun testExchangerateHost() = runBlocking {
        // latest
        testWebservice(
            ExchangeRatesService.getRates(ExchangeRatesService.ApiProvider.EXCHANGERATE_HOST).get(), 1
        )
        // timeline
        testTimeline(
            ExchangeRatesService.getTimeline(
                ExchangeRatesService.ApiProvider.EXCHANGERATE_HOST,
                "EUR", "ISK"
            ).get()
        )
    }

    @Test
    fun testFrankfurterApp() = runBlocking {
        // latest
        testWebservice(
            ExchangeRatesService.getRates(ExchangeRatesService.ApiProvider.FRANKFURTER_APP).get(), 4
        )
        // timeline
        testTimeline(
            ExchangeRatesService.getTimeline(
                ExchangeRatesService.ApiProvider.FRANKFURTER_APP,
                "EUR", "ISK"
            ).get()
        )
    }

    private fun testWebservice(rates: ExchangeRates?, maxAge: Long) {
        // see there is some valid data
        assertNotNull(rates)

        // see that there is a list of exchange rates
        assertNotNull(rates!!.rates)

        // check for some currencies
        val eur = rates.rates!!.find { rate -> rate.code == "EUR" }
        assertTrue(eur != null)
        assertEquals(1.0f, eur!!.value)
        println(eur)

        val usd = rates.rates!!.find { rate -> rate.code == "USD" }
        assertTrue(usd != null)
        println(usd)

        val jpy = rates.rates!!.find { rate -> rate.code == "JPY" }
        assertTrue(jpy != null)
        println(jpy)

        val krw = rates.rates!!.find { rate -> rate.code == "KRW" }
        assertTrue(krw != null)
        println(krw)

        val chf = rates.rates!!.find { rate -> rate.code == "CHF" }
        assertTrue(chf != null)
        println(chf)

        // check if the date is current
        rates.date?.let {
            assertTrue(it >= LocalDate.now(ZoneId.of("UTC")).minusDays(maxAge))
            println(rates.date)
        }
    }

    private fun testTimeline(data: Timeline) {
        assertTrue(data.rates != null)
        assertTrue(data.rates!!.isNotEmpty())
    }

}
