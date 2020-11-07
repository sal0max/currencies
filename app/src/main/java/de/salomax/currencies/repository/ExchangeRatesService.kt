package de.salomax.currencies.repository

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.github.kittinunf.result.Result
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.salomax.currencies.model.ExchangeRates
import de.salomax.currencies.model.Rate
import java.io.IOException
import java.time.LocalDate

object ExchangeRatesService {

    fun getRates(result: (Response, Result<ExchangeRates, FuelError>) -> Unit) {
        val base = "EUR"
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .add(RatesAdapter(base))
            .add(LocalDateAdapter())
            .build()
            .adapter(ExchangeRates::class.java)
        Fuel
//            .get("https://data.fixer.io/api/latest?access_key=027bdb6a24ffc566e6dae37f4a5acb29&base=EUR")
//            .get("https://openexchangerates.org/api/latest.json?app_id=3ffce3987c764a84bdb8fad0aa0bd7c2&base=EUR")
//            .get("https://api.frankfurter.app/latest/latest?base=$base")
            .get("https://api.exchangeratesapi.io/latest?base=$base")
            .responseObject(moshiDeserializerOf(moshi)) { _, r1, r2 ->
                result(r1, r2)
            }
    }


    /*
     * Converts currency object to array of currencies.
     * Also adds the base currency with value "1".
     */
    internal class RatesAdapter(private val base: String) : JsonAdapter<List<Rate>>() {

        @Synchronized
        @FromJson
        @Throws(IOException::class)
        override fun fromJson(reader: JsonReader): List<Rate>? {
            val list = mutableListOf<Rate>()
            reader.beginObject()
            // convert
            while (reader.hasNext()) {
                val name: String = reader.nextName()
                val value: Double = reader.nextDouble()
                list.add(Rate(name, value.toFloat()))
            }
            // add base
            list.add(Rate(base, 1f))
            reader.endObject()
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
