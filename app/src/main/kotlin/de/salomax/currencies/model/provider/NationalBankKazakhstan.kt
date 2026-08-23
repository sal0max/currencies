package de.salomax.currencies.model.provider

import android.content.Context
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.core.awaitResult
import com.github.kittinunf.result.Result
import de.salomax.currencies.R
import de.salomax.currencies.model.ApiProvider
import de.salomax.currencies.model.Currency
import de.salomax.currencies.model.ExchangeRates
import de.salomax.currencies.model.Rate
import de.salomax.currencies.model.Timeline
import de.salomax.currencies.model.adapter.NationalBankKazakhstanRatesXmlParser
import de.salomax.currencies.model.adapter.NationalBankKazakhstanTimelineAdapter
import java.io.InputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class NationalBankKazakhstan : ApiProvider.Api() {

    override val name = "National Bank of Kazakhstan"

    override fun descriptionShort(context: Context) =
        context.getText(R.string.api_nationalBankKazakhstan_descriptionShort)

    override fun getDescriptionLong(context: Context) =
        context.getText(R.string.api_nationalBankKazakhstan_descriptionFull)

    override fun descriptionUpdateInterval(context: Context) =
        context.getText(R.string.api_nationalBankKazakhstan_descriptionUpdateInterval)

    override fun descriptionHint(context: Context) =
        null

    override val baseUrl = "https://nationalbank.kz"

    override suspend fun getRates(
        context: Context?,
        date: LocalDate?
    ): Result<ExchangeRates, FuelError> {
        if (date == null) {
            return requestRates("$baseUrl/rss/rates_all.xml")
        }

        // The API returns an empty document on some public holidays. Look back for the latest
        // published rates, as other business-day-only providers in the app do.
        var lastResult: Result<ExchangeRates, FuelError>? = null
        for (daysAgo in 0L..7L) {
            val requestedDate = date.minusDays(daysAgo)
            val result = requestRates(
                "$baseUrl/rss/get_rates.cfm",
                listOf("fdate" to requestedDate.format(DATE_FORMATTER))
            )
            lastResult = result
            val rates = result.component1()
            if (rates?.success == true && rates.rates?.isNotEmpty() == true) {
                return result
            }
            if (result.component2() != null) {
                return result
            }
        }
        return lastResult ?: Result.error(FuelError.wrap(NoSuchElementException()))
    }

    private suspend fun requestRates(
        url: String,
        parameters: List<Pair<String, String>> = emptyList()
    ): Result<ExchangeRates, FuelError> =
        Fuel.get(url, parameters).awaitResult(
            object : ResponseDeserializable<ExchangeRates> {
                override fun deserialize(inputStream: InputStream): ExchangeRates =
                    NationalBankKazakhstanRatesXmlParser().parse(inputStream)
            }
        )

    override suspend fun getTimeline(
        context: Context?,
        base: Currency,
        symbol: Currency,
        startDate: LocalDate,
        endDate: LocalDate
    ): Result<Timeline, FuelError> {
        if (base == symbol) {
            return Result.success(constantTimeline(base, startDate, endDate))
        }

        val parameterBase = if (base == Currency.FOK) Currency.DKK else base
        val parameterSymbol = if (symbol == Currency.FOK) Currency.DKK else symbol
        val requestedCurrencies = listOf(parameterBase, parameterSymbol)
            .filter { it != Currency.KZT }
            .distinct()

        if (requestedCurrencies.any { currencyMetadata[it] == null }) {
            return Result.error(FuelError.wrap(IllegalArgumentException("Currency is not supported by NBK")))
        }

        val parameters = mutableListOf(
            "beginDate" to startDate.format(DATE_FORMATTER),
            "endDate" to endDate.format(DATE_FORMATTER)
        )
        requestedCurrencies.forEach { currency ->
            parameters.add("rates[]" to currencyMetadata.getValue(currency).id.toString())
        }

        return Fuel.get("$baseUrl/en/exchangerates/ezhednevnye-oficialnye-rynochnye-kursy-valyut/graph", parameters)
            .awaitResult(
                object : ResponseDeserializable<Timeline> {
                    override fun deserialize(inputStream: InputStream): Timeline =
                        NationalBankKazakhstanTimelineAdapter(base, symbol).parse(inputStream)
                }
            )
    }

    private fun constantTimeline(
        currency: Currency,
        startDate: LocalDate,
        endDate: LocalDate
    ): Timeline {
        val rates = generateSequence(startDate) { it.plusDays(1) }
            .takeWhile { !it.isAfter(endDate) }
            .associateWith { Rate(currency, 1f) }
        return Timeline(
            success = rates.isNotEmpty(),
            error = null,
            base = currency.iso4217Alpha(),
            startDate = rates.keys.firstOrNull(),
            endDate = rates.keys.lastOrNull(),
            rates = rates,
            provider = ApiProvider.NATIONAL_BANK_KAZAKHSTAN
        )
    }

    data class CurrencyMetadata(val id: Int, val quantity: Float)

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy")

        internal val currencyMetadata = mapOf(
            Currency.AUD to CurrencyMetadata(1, 1f),
            Currency.GBP to CurrencyMetadata(2, 1f),
            Currency.DKK to CurrencyMetadata(3, 1f),
            Currency.AED to CurrencyMetadata(4, 1f),
            Currency.USD to CurrencyMetadata(5, 1f),
            Currency.EUR to CurrencyMetadata(6, 1f),
            Currency.CAD to CurrencyMetadata(7, 1f),
            Currency.CNY to CurrencyMetadata(8, 1f),
            Currency.KWD to CurrencyMetadata(9, 1f),
            Currency.KGS to CurrencyMetadata(10, 1f),
            Currency.MDL to CurrencyMetadata(13, 1f),
            Currency.NOK to CurrencyMetadata(14, 1f),
            Currency.SAR to CurrencyMetadata(15, 1f),
            Currency.RUB to CurrencyMetadata(16, 1f),
            Currency.XDR to CurrencyMetadata(17, 1f),
            Currency.SGD to CurrencyMetadata(18, 1f),
            Currency.UZS to CurrencyMetadata(20, 100f),
            Currency.UAH to CurrencyMetadata(21, 1f),
            Currency.SEK to CurrencyMetadata(22, 1f),
            Currency.CHF to CurrencyMetadata(23, 1f),
            Currency.KRW to CurrencyMetadata(25, 100f),
            Currency.JPY to CurrencyMetadata(26, 1f),
            Currency.BYN to CurrencyMetadata(38, 1f),
            Currency.PLN to CurrencyMetadata(39, 1f),
            Currency.ZAR to CurrencyMetadata(40, 1f),
            Currency.TRY to CurrencyMetadata(41, 1f),
            Currency.HUF to CurrencyMetadata(42, 10f),
            Currency.CZK to CurrencyMetadata(43, 1f),
            Currency.TJS to CurrencyMetadata(44, 1f),
            Currency.HKD to CurrencyMetadata(45, 1f),
            Currency.BRL to CurrencyMetadata(46, 1f),
            Currency.MYR to CurrencyMetadata(47, 1f),
            Currency.AZN to CurrencyMetadata(48, 1f),
            Currency.INR to CurrencyMetadata(49, 1f),
            Currency.THB to CurrencyMetadata(50, 1f),
            Currency.AMD to CurrencyMetadata(51, 10f),
            Currency.GEL to CurrencyMetadata(52, 1f),
            Currency.IRR to CurrencyMetadata(53, 10_000f),
            Currency.MXN to CurrencyMetadata(54, 1f),
            Currency.VND to CurrencyMetadata(55, 1_000f),
            Currency.EGP to CurrencyMetadata(58, 1f),
            Currency.ILS to CurrencyMetadata(61, 1f),
            Currency.IDR to CurrencyMetadata(64, 1_000f),
            Currency.QAR to CurrencyMetadata(67, 1f),
            Currency.MNT to CurrencyMetadata(70, 100f),
            Currency.OMR to CurrencyMetadata(73, 1f),
            Currency.PKR to CurrencyMetadata(76, 1f),
            Currency.RON to CurrencyMetadata(79, 1f)
        )
    }
}
