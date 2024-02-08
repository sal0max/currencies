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
import de.salomax.currencies.model.Timeline
import de.salomax.currencies.model.adapter.OpenExchangeratesRatesAdapter
import de.salomax.currencies.repository.Database
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class OpenExchangerates : ApiProvider.Api() {

    override val name = "Open Exchangerates"

    override fun descriptionShort(context: Context) =
        context.getText(R.string.api_openExchangeRates_descriptionShort)

    override fun getDescriptionLong(context: Context) =
        context.getText(R.string.api_openExchangeRates_descriptionFull)

    override fun descriptionUpdateInterval(context: Context) =
        context.getText(R.string.api_openExchangeRates_descriptionUpdateInterval)

    override fun descriptionHint(context: Context) =
        context.getText(R.string.api_openExchangeRates_hint)

    override val baseUrl = "https://openexchangerates.org/api"

    override suspend fun getRates(context: Context?, date: LocalDate?): Result<ExchangeRates, FuelError> {
        val apiKey = context?.let { Database(it).getOpenExchangeRatesApiKey() }
        if (apiKey.isNullOrBlank())
            return Result.error(FuelError.wrap(Exception(context?.getString(R.string.error_no_api_key))))

        val endpoint =
            if (date != null)
                "/historical/" + date.format(DateTimeFormatter.ISO_LOCAL_DATE) + ".json"
            else
                "/latest.json"

        val result = Fuel.get(
            baseUrl +
                    endpoint +
                    "?app_id=$apiKey" +
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

        if (result.component2()?.response?.statusCode == 401) {
            return Result.error(
                FuelError.wrap(
                    Exception(context.getString(R.string.error_invalid_api_key))
                )
            )
        }
        return result
    }

    override suspend fun getTimeline(
        context: Context?,
        base: Currency,
        symbol: Currency,
        startDate: LocalDate,
        endDate: LocalDate
    ): Result<Timeline, FuelError> {
        return Result.error(FuelError.wrap(Exception(context?.getString(R.string.error_unsupported_timeline))))
    }

}
