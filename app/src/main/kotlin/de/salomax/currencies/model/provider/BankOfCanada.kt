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
import de.salomax.currencies.model.adapter.BankOfCanadaRatesAdapter
import de.salomax.currencies.model.adapter.BankOfCanadaTimelineAdapter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class BankOfCanada: ApiProvider.Api() {

    override val name = "Bank of Canada"

    override fun description(context: Context) =
        context.getText(R.string.api_about_bankOfCanada)

    override fun updateIntervalDescription(context: Context) =
        context.getText(R.string.api_refreshPeriod_bankOfCanada)

    override val baseUrl = "https://www.bankofcanada.ca/valet"

    override suspend fun getRates(date: LocalDate?): Result<ExchangeRates, FuelError> {

        // As this API doesn't return results for nonwork days, get the last seven days.
        // The latest available values will be used.
        val formattedDateStart = date?.minusDays(7)?.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val formattedDateEnd = date?.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val dateString =
            // latest
            if (date == null) "recent=1"
            // historical
            else "start_date=$formattedDateStart&end_date=$formattedDateEnd"

        return Fuel.get(
            baseUrl +
                    "/observations/group/FX_RATES_DAILY_CURRENT/json" +
                    "?$dateString" +
                    "&order_dir=desc"
        ).awaitResult(
            moshiDeserializerOf(
                Moshi.Builder()
                    .addLast(KotlinJsonAdapterFactory())
                    .add(BankOfCanadaRatesAdapter())
                    .build()
                    .adapter(ExchangeRates::class.java)

            )
        )
    }

    override suspend fun getTimeline(
        base: Currency,
        symbol: Currency,
        startDate: LocalDate,
        endDate: LocalDate
    ): Result<Timeline, FuelError> {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        // can't search for FOK - have to use DKK instead
        val parameterBase = if (base == Currency.FOK) "DKK" else base.iso4217Alpha()
        val parameterSymbol = if (symbol == Currency.FOK) "DKK" else symbol.iso4217Alpha()

        return Fuel.get(
            baseUrl +
                    "/observations" +
                    "/FX${parameterBase}CAD,FX${parameterSymbol}CAD" +
                    "/json" +
                    // "?recent_years=1" +
                    "?start_date=${startDate.format(dateFormatter)}" +
                    "&end_date=${endDate.format(dateFormatter)}" +
                    "&order_dir=asc"
        ).awaitResult(
            moshiDeserializerOf(
                Moshi.Builder()
                    .addLast(KotlinJsonAdapterFactory())
                    .add(BankOfCanadaTimelineAdapter(base, symbol))
                    .build()
                    .adapter(Timeline::class.java)
            )
        )
    }

}
