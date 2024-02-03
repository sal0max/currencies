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
import de.salomax.currencies.model.ApiProvider
import de.salomax.currencies.model.Currency
import de.salomax.currencies.model.ExchangeRates
import de.salomax.currencies.model.Timeline
import de.salomax.currencies.model.adapter.OpenExchangeratesRatesAdapter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class OpenExchangerates : ApiProvider.Api() {

    override val name = "Open Exchangerates"

    override fun descriptionShort(context: Context) =
        "TODO"

    override fun getDescriptionLong(context: Context) =
        "TODO"

    override fun descriptionUpdateInterval(context: Context) =
        "TODO"

    override fun descriptionHint(context: Context) =
        "TODO"

    override val baseUrl = "https://openexchangerates.org/api"

    override suspend fun getRates(date: LocalDate?): Result<ExchangeRates, FuelError> {
        val appId = "TODO" // TODO: configurable
        val endpoint =
            if (date != null)
                "/historical/" + date.format(DateTimeFormatter.ISO_LOCAL_DATE) + ".json"
            else
                "/latest.json"

        return Fuel.get(
            baseUrl +
                    endpoint +
                    "?app_id=$appId" +
                    "&prettyprint=false" +
                    "&show_alternative=false"
        ).awaitResult(
            moshiDeserializerOf(
                Moshi.Builder()
                    .addLast(KotlinJsonAdapterFactory())
                    .apply {
                        add(OpenExchangeratesRatesAdapter())
                    }
                    .build()
                    .adapter(ExchangeRates::class.java)
            )
        ).map { rates ->
            rates.copy(provider = ApiProvider.OPEN_EXCHANGERATES)
        }
    }

    override suspend fun getTimeline(
        base: Currency,
        symbol: Currency,
        startDate: LocalDate,
        endDate: LocalDate
    ): Result<Timeline, FuelError> {
        return Result.error(FuelError.wrap(Exception("This API doesn't provide timeline data."))) // TODO: localize
    }

}
