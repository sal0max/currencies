package de.salomax.currencies.repository

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.awaitResult
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.salomax.currencies.model.ApiProvider
import de.salomax.currencies.model.Currency
import de.salomax.currencies.model.ExchangeRates
import de.salomax.currencies.model.Rate
import de.salomax.currencies.model.Timeline
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

object ExchangeRatesService {

    /**
     * Get all the current exchange rates from the given api provider. Base will be Euro.
     */
    suspend fun getRates(
        apiProvider: ApiProvider,
        date: LocalDate? = null
    ): Result<ExchangeRates, FuelError> {
        // Currency conversions are done relatively to each other - so it basically doesn't matter
        // which base is used here. However, Euro is a strong currency, preventing rounding errors.
        val base = Currency.EUR
        val dateString =
            if (date != null) date.format(DateTimeFormatter.ISO_LOCAL_DATE) else "latest"

        return Fuel.get(
            when (apiProvider) {
                ApiProvider.EXCHANGERATE_HOST -> apiProvider.baseUrl +
                        "/$dateString" +
                        "?base=$base" +
                        "&v=${UUID.randomUUID()}"

                ApiProvider.FRANKFURTER_APP -> apiProvider.baseUrl +
                        "/$dateString" +
                        "?base=$base"

                ApiProvider.FER_EE -> apiProvider.baseUrl +
                        "/$dateString" +
                        "?base=$base"

                ApiProvider.INFOR_EURO -> apiProvider.baseUrl +
                        "/monthly-rates" +
                        if (date != null) "?year=${date.year}" + "&month=${date.monthValue}"
                        else ""
            }
        ).awaitResult(
            moshiDeserializerOf(
                Moshi.Builder()
                    .addLast(KotlinJsonAdapterFactory())
                    .apply {
                        if (apiProvider == ApiProvider.INFOR_EURO) {
                            add(InforEuroAdapter(date ?: LocalDate.now(ZoneOffset.UTC)))
                        } else {
                            add(RatesAdapter(base))
                            add(LocalDateAdapter())
                        }
                    }
                    .build()
                    .adapter(ExchangeRates::class.java)
            )
        ).map { rates ->
            rates.copy(provider = apiProvider)
        }
    }

    /**
     * Get the historic rates of the past year between the given base and symbol.
     * Won't get all the symbols, as it makes a big difference in transferred data size:
     * ~12 KB for one symbol to ~840 KB for all symbols
     */
    suspend fun getTimeline(
        apiProvider: ApiProvider,
        base: Currency,
        symbol: Currency
    ): Result<Timeline, FuelError> {
        val endDate = LocalDate.now()
        val startDate = endDate.minusYears(1)

        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        // can't search for FOK - have to use DKK instead
        val parameterBase = if (base == Currency.FOK) "DKK" else base.iso4217Alpha()
        val parameterSymbol = if (symbol == Currency.FOK) "DKK" else symbol.iso4217Alpha()

        // CALL API

        // InforEuro needs 2 calls: the API only provides EUR <-> symbol, without changing the base.
        // So, we make 2 calls: EUR <-> base & EUR <-> symbol
        return if (apiProvider == ApiProvider.INFOR_EURO) {
            val deserializer = moshiDeserializerOf(
                Moshi.Builder()
                    .addLast(KotlinJsonAdapterFactory())
                    .add(InforEuroTimelineAdapter(startDate, endDate))
                    .build()
                    .adapter(Timeline::class.java)
            )
            // EUR <-> base
            val resultBase = Fuel.get(
                "${apiProvider.baseUrl}/currencies/$parameterBase"
            ).awaitResult(deserializer)
            // error
            if (resultBase.component2() != null) return resultBase

            // EUR <-> symbol
            val resultSymbol = Fuel.get(
                "${apiProvider.baseUrl}/currencies/$parameterSymbol"
            ).awaitResult(deserializer)
            // error
            if (resultSymbol.component2() != null) return resultSymbol

            // success
            val timeline = resultBase.get().copy(
                provider = apiProvider,
                base = (if (base == Currency.FOK) Currency.FOK else base).iso4217Alpha(),
                rates = resultSymbol.get().rates?.map { symbolEntry ->
                    val baseValue = resultBase.get().rates?.get(symbolEntry.key)
                    Pair(
                        symbolEntry.key,
                        Rate(symbol, symbolEntry.value.value.div(baseValue?.value ?: 1f))
                    )
                }?.toMap()
            )
            Result.of { timeline }
        }

        // take care of the "normal" APIs
        else Fuel.get(
            @Suppress("KotlinConstantConditions")
            when (apiProvider) {
                ApiProvider.EXCHANGERATE_HOST -> "${apiProvider.baseUrl}/timeseries" +
                        "?base=$parameterBase" +
                        "&v=${UUID.randomUUID()}" +
                        "&start_date=${startDate.format(dateFormatter)}" +
                        "&end_date=${endDate.format(dateFormatter)}" +
                        "&symbols=$parameterSymbol"

                ApiProvider.FRANKFURTER_APP -> "${apiProvider.baseUrl}/" +
                        startDate.format(dateFormatter) +
                        ".." +
                        endDate.format(dateFormatter) +
                        "?base=$parameterBase" +
                        "&symbols=$parameterSymbol"

                ApiProvider.FER_EE -> "${apiProvider.baseUrl}/" +
                        startDate.format(dateFormatter) +
                        ".." +
                        endDate.format(dateFormatter) +
                        "?base=$parameterBase" +
                        "&symbols=$parameterSymbol"

                ApiProvider.INFOR_EURO -> TODO() // we can ignore this: never reachable
            }
        ).awaitResult(
            moshiDeserializerOf(
                Moshi.Builder()
                    .addLast(KotlinJsonAdapterFactory())
                    .apply {
                        add(RatesAdapter(base))
                        add(LocalDateAdapter())
                        add(TimelineRatesAdapter(symbol))
                    }
                    .build()
                    .adapter(Timeline::class.java)
            )
        ).map { timeline ->
            when (base) {
                // change dkk base back to fok, if needed
                Currency.FOK -> timeline.copy(base = base.iso4217Alpha())
                else -> timeline
            }
        }.map { timeline ->
            timeline.copy(provider = apiProvider)
        }
    }

}
