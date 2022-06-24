package de.salomax.currencies.repository

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.awaitResult
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.salomax.currencies.model.*
import de.salomax.currencies.model.Currency
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

object ExchangeRatesService {

    /**
     * Get all the current exchange rates from the given api provider. Base will be Euro.
     */
    suspend fun getRates(apiProvider: ApiProvider, date: LocalDate? = null): Result<ExchangeRates, FuelError> {
        // Currency conversions are done relatively to each other - so it basically doesn't matter
        // which base is used here. However, Euro is a strong currency, preventing rounding errors.
        val base = Currency.EUR
        val dateString = if (date != null) date.format(DateTimeFormatter.ISO_LOCAL_DATE) else "latest"

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
            }
        ).awaitResult(
            moshiDeserializerOf(
                Moshi.Builder()
                    .addLast(KotlinJsonAdapterFactory())
                    .add(RatesAdapter(base))
                    .add(LocalDateAdapter())
                    .build()
                    .adapter(ExchangeRates::class.java)
            )
        ).map { timeline ->
            timeline.copy(provider = apiProvider)
        }
    }

    /**
     * Get the historic rates of the past year between the given base and symbol.
     * Won't get all the symbols, as it makes a big difference in transferred data size:
     * ~12 KB for one symbol to ~840 KB for all symbols
     */
    suspend fun getTimeline(apiProvider: ApiProvider, base: Currency, symbol: Currency): Result<Timeline, FuelError> {
        val endDate = LocalDate.now()
        val startDate = endDate.minusYears(1)

        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        // can't search for FOK - have to use DKK instead
        val parameterBase = if (base == Currency.FOK) "DKK" else base
        val parameterSymbol = if (symbol == Currency.FOK) "DKK" else symbol
        // call api
        return Fuel.get(
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
            }
        ).awaitResult(
            moshiDeserializerOf(
                Moshi.Builder()
                    .addLast(KotlinJsonAdapterFactory())
                    .add(RatesAdapter(base))
                    .add(LocalDateAdapter())
                    .add(TimelineRatesToRateAdapter(symbol))
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

    /*
     * Converts a timeline rates object to a Map<LocalDate, Rate?>>
     * The API actually returns Map<LocalDate, List<Rate>>>, however, we only want one Rate per day.
     * This converter reduces the list.
     */
    @Suppress("unused", "UNUSED_PARAMETER")
    internal class TimelineRatesToRateAdapter(private val symbol: Currency) {

        @Synchronized
        @FromJson
        fun fromJson(reader: JsonReader): Map<LocalDate, Rate> {
            val map = mutableMapOf<LocalDate, Rate>()
            reader.beginObject()
            // convert
            while (reader.hasNext()) {
                val date: LocalDate = LocalDate.parse(reader.nextName())
                var rate: Rate? = null
                reader.beginObject()
                // sometimes there's no rate yet, but an empty body or more than one rate, so check first
                while (reader.hasNext() && reader.peek() == JsonReader.Token.NAME) {
                    val name = Currency.fromString(reader.nextName())
                    val value = reader.nextDouble().toFloat()
                    rate =
                        // change dkk to fok, when needed
                        if (name == Currency.DKK && symbol == Currency.FOK)
                            Rate(Currency.FOK, value)
                        // make sure that the symbol matches the one we requested
                        else if (name == symbol)
                            Rate(name, value)
                        else
                            null
                }
                if (rate != null)
                    map[date] = rate
                reader.endObject()
            }
            reader.endObject()
            return map
        }

        @Synchronized
        @ToJson
        @Throws(IOException::class)
        fun toJson(writer: JsonWriter, value: Map<LocalDate, Rate>?) {
            writer.nullValue()
        }

    }

    /*
     * Converts currency object to array of currencies.
     * Also removes some unwanted values and adds some wanted ones.
     */
    @Suppress("unused", "UNUSED_PARAMETER")
    internal class RatesAdapter(private val base: Currency) {

        @Synchronized
        @FromJson
        @Suppress("SpellCheckingInspection")
        @Throws(IOException::class)
        fun fromJson(reader: JsonReader): List<Rate> {
            val list = mutableListOf<Rate>()
            reader.beginObject()
            // convert
            while (reader.hasNext()) {
                val name: String = reader.nextName()
                val value: Double = reader.nextDouble()
                // filter these:
                if (name != "BTC" // Bitcoin
                    && name != "CLF" // Unidad de Fomento
                    && name != "XDR" // special drawing rights
                    && name != "XAG" // silver
                    && name != "XAU" // gold
                    && name != "XPD" // palladium
                    && name != "XPT" // platinum
                    && name != "MRO" // Mauritanian ouguiya (pre-2018)
                    && name != "STD" // São Tomé and Príncipe dobra (pre-2018)
                    && name != "VEF" // Venezuelan bolívar fuerte (old)
                    && name != "CNH" // Chinese renminbi (Offshore)
                    && name != "CUP" // Cuban peso (moneda nacional)
                ) {
                    Currency.fromString(name)?.let { list.add(Rate(it, value.toFloat())) }
                }
            }
            reader.endObject()
            // add base - but only if it's missing in the api response!
            if (list.find { rate -> rate.currency == base } == null)
                list.add(Rate(base, 1f))
            // also add Faroese króna (same as Danish krone) if it isn't already there - I simply like it!
            if (list.find { it.currency == Currency.FOK } == null)
                list.find { it.currency == Currency.DKK }?.value?.let { dkk ->
                    list.add(Rate(Currency.FOK, dkk))
                }
            return list
        }

        @Synchronized
        @ToJson
        @Throws(IOException::class)
        fun toJson(writer: JsonWriter, value: List<Rate>?) {
            writer.nullValue()
        }

    }

    @Suppress("unused", "UNUSED_PARAMETER")
    internal class LocalDateAdapter {

        @Synchronized
        @FromJson
        @Throws(IOException::class)
        fun fromJson(reader: JsonReader): LocalDate? {
            return LocalDate.parse(reader.nextString())
        }

        @Synchronized
        @ToJson
        @Throws(IOException::class)
        fun toJson(writer: JsonWriter, value: LocalDate?) {
            writer.value(value?.toString())
        }

    }
}
