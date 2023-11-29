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
import de.salomax.currencies.model.adapter.BankRossiiCurrencyCodesXmlParser
import de.salomax.currencies.model.adapter.BankRossiiRatesXmlParser
import de.salomax.currencies.model.adapter.BankRossiiTimelineXmlParser
import java.io.InputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BankRossii : ApiProvider.Api() {

    override val name = "Bank Rossii"

    override fun description(context: Context) =
        context.getText(R.string.api_about_bankRossii)

    override fun updateIntervalDescription(context: Context) =
        context.getText(R.string.api_refreshPeriod_bankRossii)

    override val baseUrl = "https://www.cbr.ru/scripts"

    override suspend fun getRates(date: LocalDate?): Result<ExchangeRates, FuelError> {
        val dateString =
            // latest
            if (date == null) ""
            // historical
            else "?date_req=${date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"

        return Fuel.get(
            baseUrl +
                    "/XML_daily.asp" +
                    dateString
        ).awaitResult(
            object : ResponseDeserializable<ExchangeRates> {
                override fun deserialize(inputStream: InputStream): ExchangeRates {
                    return BankRossiiRatesXmlParser().parse(inputStream)
                }
            }
        )
    }

    override suspend fun getTimeline(
        base: Currency,
        symbol: Currency,
        startDate: LocalDate,
        endDate: LocalDate
    ): Result<Timeline, FuelError> {
        // Create a constant (1.0) timeline series for RUB.
        // Needed for timelines with RUB, as the API doesn't return RUB, even if requested.
        var currentDate = startDate
        val rubMap = object : LinkedHashMap<LocalDate, Rate>() {
            init {
                while (currentDate.isBefore(endDate) || currentDate.isEqual(endDate)) {
                    this[currentDate] = Rate(Currency.RUB, 1f)
                    currentDate = currentDate.plusDays(1)
                }
            }
        }
        val timelineRub = Timeline(
            success = true,
            error = null,
            base = Currency.RUB.iso4217Alpha(),
            startDate = startDate,
            endDate = endDate,
            rates = rubMap.toSortedMap(),
            provider = ApiProvider.BANK_ROSSII
        )

        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        // can't search for FOK - have to use DKK instead
        val parameterBase = if (base == Currency.FOK) "DKK" else base.iso4217Alpha()
        val parameterSymbol = if (symbol == Currency.FOK) "DKK" else symbol.iso4217Alpha()

        // need three calls:
        // first: get internal currency IDs
        val ids = Fuel.get(
            baseUrl +
                    "/XML_valFull.asp"
        ).awaitResult(
            object : ResponseDeserializable<Map<String, String>> {
                override fun deserialize(inputStream: InputStream): Map<String, String> {
                    return BankRossiiCurrencyCodesXmlParser().parse(inputStream)
                }
            }
        ).get()

        // second call: RUB -> base
        val baseTimeline = if (parameterBase == Currency.RUB.iso4217Alpha()) {
            Result.success(timelineRub)
        } else {
            val idBase = ids.entries.find { it.value == parameterBase }?.key
                ?: return Result.error(FuelError.wrap(Throwable()))
            Fuel.get(
                baseUrl +
                        "/XML_dynamic.asp" +
                        "?date_req1=${startDate.format(dateFormatter)}" +
                        "&date_req2=${endDate.format(dateFormatter)}" +
                        "&VAL_NM_RQ=$idBase"
            ).awaitResult(
                object : ResponseDeserializable<Timeline> {
                    override fun deserialize(inputStream: InputStream): Timeline {
                        return BankRossiiTimelineXmlParser(ids).parse(inputStream)
                    }
                }
            )
        }

        // third call: RUB -> symbol
        val symbolTimeline = if (parameterSymbol == Currency.RUB.iso4217Alpha()) {
            Result.success(timelineRub)
        } else {
            val idSymbol = ids.entries.find { it.value == parameterSymbol }?.key
                ?: return Result.error(FuelError.wrap(Throwable()))
            Fuel.get(
                baseUrl +
                        "/XML_dynamic.asp" +
                        "?date_req1=${startDate.format(dateFormatter)}" +
                        "&date_req2=${endDate.format(dateFormatter)}" +
                        "&VAL_NM_RQ=$idSymbol"
            ).awaitResult(
                object : ResponseDeserializable<Timeline> {
                    override fun deserialize(inputStream: InputStream): Timeline {
                        return BankRossiiTimelineXmlParser(ids).parse(inputStream)
                    }
                }
            )
        }

        // finally, combine the responses and return the result
        val baseRates: Map<LocalDate, Rate>? = baseTimeline.component1()?.rates
        val symbolRates: Map<LocalDate, Rate>? = symbolTimeline.component1()?.rates

        return if (baseRates == null || symbolRates == null)
            Result.error(FuelError.wrap(Throwable()))
        else
            Result.of {
                // merge base & symbol
                symbolTimeline.get().copy(
                    rates = symbolRates
                        .filter { symbol -> baseRates[symbol.key] != null }
                        .mapValues { symbol ->
                            val symbolValue = symbol.value
                            val baseValue = baseRates[symbol.key]!!
                            symbol.value.copy(
                                currency = symbolValue.currency,
                                value = symbolValue.value / baseValue.value
                            )
                        }
                )
            }
    }

}
