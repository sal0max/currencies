package de.salomax.currencies.repository

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.ResponseResultOf
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.github.kittinunf.result.Result
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.salomax.currencies.model.ExchangeRates
import de.salomax.currencies.model.Rate
import java.io.IOException
import java.time.LocalDate
import java.util.*

object ExchangeRatesService {

    private const val base = "EUR"

    enum class Endpoint(val url: String) {
        EXCHANGERATE_HOST("https://api.exchangerate.host/latest?base=$base&v=${UUID.randomUUID()}"),
        FRANKFURTER_APP("https://api.frankfurter.app/latest?base=$base"),
        FER_EE("https://api.fer.ee/latest?base=$base"),
    }

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .add(RatesAdapter(base))
        .add(LocalDateAdapter())
        .build()
        .adapter(ExchangeRates::class.java)

    fun getRates(endpoint: Endpoint, result: (Response, Result<ExchangeRates, FuelError>) -> Unit) {
        Fuel
            .get(endpoint.url)
            .responseObject(moshiDeserializerOf(moshi)) { _, r1, r2 ->
                result(r1, r2)
            }
    }

    fun getRatesBlocking(endpoint: Endpoint): ResponseResultOf<ExchangeRates> {
        return Fuel
            .get(endpoint.url)
            .responseObject(moshiDeserializerOf(moshi))
    }

    /*
     * Converts currency object to array of currencies.
     * Also removes some unwanted values and adds some wanted ones.
     */
    internal class RatesAdapter(private val base: String) : JsonAdapter<List<Rate>>() {

        @Synchronized
        @FromJson
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
