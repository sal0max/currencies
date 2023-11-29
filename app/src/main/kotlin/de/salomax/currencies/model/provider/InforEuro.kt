package de.salomax.currencies.model.provider

import android.content.Context
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.awaitResult
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.salomax.currencies.R
import de.salomax.currencies.model.ApiProvider
import de.salomax.currencies.model.Currency
import de.salomax.currencies.model.ExchangeRates
import de.salomax.currencies.model.Rate
import de.salomax.currencies.model.Timeline
import de.salomax.currencies.model.adapter.InforEuroRatesAdapter
import de.salomax.currencies.model.adapter.InforEuroTimelineAdapter
import java.time.LocalDate
import java.time.ZoneOffset

class InforEuro : ApiProvider.Api() {

    override val name = "InforEuro"

    override fun description(context: Context) =
        context.getText(R.string.api_about_inforEuro)

    override fun updateIntervalDescription(context: Context) =
        context.getText(R.string.api_refreshPeriod_inforEuro)

    override val baseUrl = "https://ec.europa.eu/budg/inforeuro/api/public"

    override suspend fun getRates(date: LocalDate?): Result<ExchangeRates, FuelError> {
        return Fuel.get(
            baseUrl +
                    "/monthly-rates" +
                    if (date != null) "?year=${date.year}" + "&month=${date.monthValue}"
                    else ""
        ).awaitResult(
            moshiDeserializerOf(
                Moshi.Builder()
                    .addLast(KotlinJsonAdapterFactory())
                    .apply {
                        add(InforEuroRatesAdapter(date ?: LocalDate.now(ZoneOffset.UTC)))
                    }
                    .build()
                    .adapter(ExchangeRates::class.java)
            )
        ).map { rates ->
            rates.copy(provider = ApiProvider.INFOR_EURO)
        }
    }

    override suspend fun getTimeline(
        base: Currency,
        symbol: Currency,
        startDate: LocalDate,
        endDate: LocalDate
    ): Result<Timeline, FuelError> {
        // can't search for FOK - have to use DKK instead
        val parameterBase = if (base == Currency.FOK) "DKK" else base.iso4217Alpha()
        val parameterSymbol = if (symbol == Currency.FOK) "DKK" else symbol.iso4217Alpha()

        // InforEuro needs 2 calls: the API only provides EUR <-> symbol, without changing the base.
        // So, we make 2 calls: EUR <-> base & EUR <-> symbol

        val deserializer = moshiDeserializerOf(
            Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .add(InforEuroTimelineAdapter(startDate, endDate))
                .build()
                .adapter(Timeline::class.java)
        )
        // EUR <-> base
        val resultBase = Fuel.get(
            "$baseUrl/currencies/$parameterBase"
        ).awaitResult(deserializer)
        // error
        if (resultBase.component2() != null) return resultBase

        // EUR <-> symbol
        val resultSymbol = Fuel.get(
            "$baseUrl/currencies/$parameterSymbol"
        ).awaitResult(deserializer)
        // error
        if (resultSymbol.component2() != null) return resultSymbol

        // success
        val timeline = resultBase.get().copy(
            provider = ApiProvider.INFOR_EURO,
            base = (if (base == Currency.FOK) Currency.FOK else base).iso4217Alpha(),
            rates = resultSymbol.get().rates?.map { symbolEntry ->
                val baseValue = resultBase.get().rates?.get(symbolEntry.key)
                Pair(
                    symbolEntry.key,
                    Rate(symbol, symbolEntry.value.value.div(baseValue?.value ?: 1f))
                )
            }?.toMap()
        )
        return Result.of { timeline }

    }

}
