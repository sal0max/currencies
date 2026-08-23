package de.salomax.currencies.model.provider

import android.content.Context
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.awaitResult
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.github.kittinunf.result.Result
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.salomax.currencies.R
import de.salomax.currencies.model.ApiProvider
import de.salomax.currencies.model.Currency
import de.salomax.currencies.model.ExchangeRates
import de.salomax.currencies.model.Timeline
import de.salomax.currencies.model.adapter.AllRatesTodayRatesAdapter
import de.salomax.currencies.model.adapter.AllRatesTodayTimelineAdapter
import de.salomax.currencies.repository.Database
import java.time.LocalDate

class AllRatesToday : ApiProvider.Api() {

    override val name = "AllRatesToday"

    override fun descriptionShort(context: Context) =
        context.getText(R.string.api_allRatesToday_descriptionShort)

    override fun getDescriptionLong(context: Context) =
        context.getText(R.string.api_allRatesToday_descriptionFull)

    override fun descriptionUpdateInterval(context: Context) =
        context.getText(R.string.api_allRatesToday_descriptionUpdateInterval)

    override fun descriptionHint(context: Context) =
        context.getText(R.string.api_allRatesToday_hint)

    override val baseUrl = "https://allratestoday.com/api/v1"

    override suspend fun getRates(
        context: Context?,
        date: LocalDate?
    ): Result<ExchangeRates, FuelError> {
        val apiKey = getApiKey(context) ?: return missingApiKey(context)
        val base = Currency.EUR
        val parameters = mutableListOf("source" to base.iso4217Alpha())
        date?.let { parameters.add("time" to "${it}T23:59:59Z") }

        val result = Fuel.get("$baseUrl/rates", parameters)
            .header("Authorization", "Bearer $apiKey")
            .awaitResult(
                moshiDeserializerOf(
                    Moshi.Builder()
                        .add(AllRatesTodayRatesAdapter(base, date))
                        .addLast(KotlinJsonAdapterFactory())
                        .build()
                        .adapter(ExchangeRates::class.java)
                )
            )

        return handleAuthenticationError(context, result)
    }

    override suspend fun getTimeline(
        context: Context?,
        base: Currency,
        symbol: Currency,
        startDate: LocalDate,
        endDate: LocalDate
    ): Result<Timeline, FuelError> {
        val apiKey = getApiKey(context) ?: return missingApiKey(context)
        val parameterBase = if (base == Currency.FOK) Currency.DKK else base
        val parameterSymbol = if (symbol == Currency.FOK) Currency.DKK else symbol

        val result = Fuel.get(
            "$baseUrl/rates",
            listOf(
                "source" to parameterBase.iso4217Alpha(),
                "target" to parameterSymbol.iso4217Alpha(),
                "from" to startDate.toString(),
                "to" to endDate.plusDays(1).toString(),
                "group" to "day"
            )
        )
            .header("Authorization", "Bearer $apiKey")
            .awaitResult(
                moshiDeserializerOf(
                    Moshi.Builder()
                        .add(AllRatesTodayTimelineAdapter(base, symbol, startDate, endDate))
                        .addLast(KotlinJsonAdapterFactory())
                        .build()
                        .adapter(Timeline::class.java)
                )
            )

        return handleAuthenticationError(context, result)
    }

    private fun getApiKey(context: Context?): String? =
        context?.let { Database(it).getAllRatesTodayApiKey() }?.takeUnless(String::isBlank)

    private fun <T> missingApiKey(context: Context?): Result<T, FuelError> =
        Result.error(FuelError.wrap(Exception(context?.getString(R.string.error_no_api_key))))

    private fun <T> handleAuthenticationError(
        context: Context?,
        result: Result<T, FuelError>
    ): Result<T, FuelError> {
        return if (result.component2()?.response?.statusCode == 401) {
            Result.error(
                FuelError.wrap(Exception(context?.getString(R.string.error_invalid_api_key)))
            )
        } else {
            result
        }
    }
}
