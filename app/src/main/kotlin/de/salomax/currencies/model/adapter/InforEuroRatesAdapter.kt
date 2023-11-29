package de.salomax.currencies.model.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import de.salomax.currencies.model.ApiProvider
import de.salomax.currencies.model.Currency
import de.salomax.currencies.model.ExchangeRates
import de.salomax.currencies.model.Rate
import java.io.IOException
import java.time.LocalDate

@Suppress("unused", "UNUSED_PARAMETER")
internal class InforEuroRatesAdapter(private val date: LocalDate) {

    @Synchronized
    @FromJson
    @Throws(IOException::class)
    fun fromJson(reader: JsonReader): ExchangeRates {
        val rates = mutableListOf<Rate>()

        // received rates
        return if (reader.peek() == JsonReader.Token.BEGIN_ARRAY) {
            reader.beginArray()
            // convert
            while (reader.hasNext()) {
                reader.beginObject() // begin array element
                var name: Currency? = null
                var value: Float? = null
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "isoA3Code" -> name = Currency.fromString(reader.nextString())
                        "value" -> value = reader.nextDouble().toFloat()
                        else -> reader.skipValue()
                    }
                }
                if (name != null && value != null)
                    rates.add(Rate(name, value))
                reader.endObject() // end array element
            }
            reader.endArray()
            ExchangeRates(
                success = rates.isNotEmpty(),
                error = null,
                base = Currency.EUR,
                date = date,
                rates = rates,
                provider = ApiProvider.INFOR_EURO
            )
        }
        // error message
        else {
            reader.beginObject()
            var message: String? = null
            while (reader.hasNext()) {
                if (reader.nextName() == "message")
                    message = reader.nextString()
            }
            reader.endObject()
            ExchangeRates(
                success = false,
                error = message,
                base = Currency.EUR,
                date = date,
                rates = null,
                provider = ApiProvider.INFOR_EURO
            )
        }
    }

    @Synchronized
    @ToJson
    @Throws(IOException::class)
    fun toJson(writer: JsonWriter, value: ExchangeRates) {
        writer.nullValue()
    }

}
