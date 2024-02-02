package de.salomax.currencies.model.provider

import android.content.Context
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.awaitResult
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.github.kittinunf.result.Result
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.salomax.currencies.model.ApiProvider
import de.salomax.currencies.model.Currency
import de.salomax.currencies.model.ExchangeRates
import de.salomax.currencies.model.Timeline
import de.salomax.currencies.model.adapter.CeskaNarodniBankaRatesAdapter
import de.salomax.currencies.model.adapter.LocalDateAdapter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CeskaNarodniBanka : ApiProvider.Api() {

    @Suppress("SpellCheckingInspection")
    override val name = "Česká Národní Banka"

    override fun descriptionShort(context: Context) =
        "TODO"

    override fun getDescriptionLong(context: Context) =
        "TODO"

    override fun descriptionUpdateInterval(context: Context) =
        "TODO"

    override fun descriptionHint(context: Context): CharSequence? =
        null


    override val baseUrl = "https://api.cnb.cz/cnbapi/exrates/daily"

    override suspend fun getRates(date: LocalDate?): Result<ExchangeRates, FuelError> {
        val dateString = date?.let { "&date=${it.format(DateTimeFormatter.ISO_LOCAL_DATE)}" } ?: ""
        return Fuel.get(
            baseUrl +
                    "?lang=EN" +
                    dateString
        ).awaitResult(
            moshiDeserializerOf(
                Moshi.Builder()
                    .addLast(KotlinJsonAdapterFactory())
                    .apply {
                        add(CeskaNarodniBankaRatesAdapter())
                        add(LocalDateAdapter())
                    }
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
        TODO()
//        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
//        // can't search for FOK - have to use DKK instead
//        val parameterBase = if (base == Currency.FOK) "DKK" else base.iso4217Alpha()
//        val parameterSymbol = if (symbol == Currency.FOK) "DKK" else symbol.iso4217Alpha()
//
//        return Fuel.get(
//            "$baseUrl/" +
//                startDate.format(dateFormatter) +
//                ".." +
//                endDate.format(dateFormatter) +
//                "?base=$parameterBase" +
//                "&symbols=$parameterSymbol"
//        ).awaitResult(
//            moshiDeserializerOf(
//                Moshi.Builder()
//                    .addLast(KotlinJsonAdapterFactory())
//                    .apply {
//                        add(FrankfurterAppRatesAdapter(base))
//                        add(LocalDateAdapter())
//                        add(FrankfurterAppTimelineAdapter(symbol))
//                    }
//                    .build()
//                    .adapter(Timeline::class.java)
//            )
//        ).map { timeline ->
//            when (base) {
//                // change dkk base back to fok, if needed
//                Currency.FOK -> timeline.copy(base = base.iso4217Alpha())
//                else -> timeline
//            }
//        }
    }

}
