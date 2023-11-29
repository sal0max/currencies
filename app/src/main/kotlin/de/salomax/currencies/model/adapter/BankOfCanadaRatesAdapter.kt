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
internal class BankOfCanadaRatesAdapter {

    @Synchronized
    @FromJson
    @Throws(IOException::class)
    fun fromJson(reader: JsonReader): ExchangeRates? {
        reader.beginObject()
        // convert
        while (reader.hasNext()) {
            when (reader.nextName()) {
                // error
                "message" -> return ExchangeRates(
                    success = false,
                    error = reader.nextString(),
                    base = null,
                    date = null,
                    rates = null,
                    provider = ApiProvider.BANK_OF_CANADA
                )
                // get the values
                "observations" -> {
                    reader.beginArray()
                    return convertObservation(reader)
                }
                // not interested in those
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        return null
    }

    private fun convertObservation(reader: JsonReader): ExchangeRates {
        var errorMessage: String? = null
        var date: LocalDate? = null
        val rates = mutableListOf<Rate>()

        if (reader.peek() == JsonReader.Token.END_ARRAY)
            // no data
            errorMessage = "No data found."
        else {
            reader.beginObject()
            while (reader.hasNext()) {
                when (val nextName = reader.nextName()) {
                    // date
                    "d" -> date = LocalDate.parse(reader.nextString())
                    // rate
                    else -> {
                        val currency = Currency.fromString(nextName.substring(2, 5))
                        reader.beginObject()
                        reader.skipName() // always "v"
                        val value = reader.nextDouble()
                        reader.endObject()
                        currency?.let { rates.add(Rate(it, 1f / value.toFloat())) }
                    }
                }
            }
        }

        return ExchangeRates(
            success = errorMessage == null && rates.isNotEmpty() && date != null,
            error = errorMessage,
            base = Currency.CAD,
            date = date,
            rates = rates,
            provider = ApiProvider.BANK_OF_CANADA
        )
    }

    @Synchronized
    @ToJson
    @Throws(IOException::class)
    fun toJson(writer: JsonWriter, value: ExchangeRates?) {
        writer.nullValue()
    }

}
