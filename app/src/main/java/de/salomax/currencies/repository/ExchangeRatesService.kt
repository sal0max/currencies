package de.salomax.currencies.repository

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.github.kittinunf.result.Result
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.salomax.currencies.model.ExchangeRates
import de.salomax.currencies.model.Timeline
import de.salomax.currencies.model.Rate
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

object ExchangeRatesService {

    enum class Endpoint(val baseUrl: String) {
        EXCHANGERATE_HOST("https://api.exchangerate.host"),
        FRANKFURTER_APP("https://api.frankfurter.app")
    }

    /**
     *
     */
    suspend fun getRates(endpoint: Endpoint): Result<ExchangeRates, FuelError> {
        // Currency conversions are done relatively to each other - so it basically doesn't matter
        // which base is used here. However, Euro is a strong currency, preventing rounding errors.
        val base = "EUR"

        return Fuel.get(
            when (endpoint) {
                Endpoint.EXCHANGERATE_HOST -> "${endpoint.baseUrl}/latest" +
                        "?base=$base" +
                        "&v=${UUID.randomUUID()}"
                Endpoint.FRANKFURTER_APP -> "${endpoint.baseUrl}/latest" +
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
        )
    }

    /**
     *
     */
    suspend fun getTimeline(endpoint: Endpoint, startDate: LocalDate, endDate: LocalDate,
                            base: String, symbol: String): Result<Timeline, FuelError> {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return Fuel.get(
            when (endpoint) {
                Endpoint.EXCHANGERATE_HOST -> "${endpoint.baseUrl}/timeseries" +
                        "?base=$base" +
                        "&v=${UUID.randomUUID()}" +
                        "&start_date=${startDate.format(dateFormatter)}" +
                        "&end_date=${endDate.format(dateFormatter)}" +
                        "&symbols=$symbol"
                Endpoint.FRANKFURTER_APP -> "${endpoint.baseUrl}/" +
                        startDate.format(dateFormatter) +
                        ".." +
                        endDate.format(dateFormatter) +
                        "?base=$base" +
                        "&symbols=$symbol"
            }
        ).awaitResult(
            moshiDeserializerOf(
                Moshi.Builder()
                    .addLast(KotlinJsonAdapterFactory())
                    .add(RatesAdapter(base))
                    .add(LocalDateAdapter())
                    .build()
                    .adapter(Timeline::class.java)
            )
        )
    }

    /*
     * Converts currency object to array of currencies.
     * Also removes some unwanted values and adds some wanted ones.
     */
    internal class RatesAdapter(private val base: String) : JsonAdapter<List<Rate>>() {

        @Synchronized
        @FromJson
        @Suppress("SpellCheckingInspection")
        @Throws(IOException::class)
        override fun fromJson(reader: JsonReader): List<Rate> {
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
                    list.add(Rate(name, value.toFloat()))
                }
            }
            reader.endObject()
            // add base - but only if it's missing in the api response!
            if (list.find { rate -> rate.code == base } == null)
                list.add(Rate(base, 1f))
            // also add Faroese króna (same as Danish krone) if it isn't already there - I simply like it!
            if (list.find { it.code == "FOK" } == null)
                list.find { it.code == "DKK" }?.value?.let { dkk ->
                    list.add(Rate("FOK", dkk))
                }
            return list
        }

        @Synchronized
        @ToJson
        @Throws(IOException::class)
        override fun toJson(writer: JsonWriter, value: List<Rate>?) {
            writer.nullValue()
        }

    }

    internal class LocalDateAdapter : JsonAdapter<LocalDate>() {

        @Synchronized
        @FromJson
        @Throws(IOException::class)
        override fun fromJson(reader: JsonReader): LocalDate? {
            return LocalDate.parse(reader.nextString())
        }

        @Synchronized
        @ToJson
        @Throws(IOException::class)
        override fun toJson(writer: JsonWriter, value: LocalDate?) {
            writer.value(value?.toString())
        }

    }
}
