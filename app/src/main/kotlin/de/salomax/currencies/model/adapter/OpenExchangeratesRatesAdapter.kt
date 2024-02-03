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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Suppress("unused", "UNUSED_PARAMETER")
internal class OpenExchangeratesRatesAdapter {

    @Synchronized
    @FromJson
    @Throws(IOException::class)
    fun fromJson(reader: JsonReader): ExchangeRates? {
        val rates = mutableListOf<Rate>()
        var base: Currency? = null
        var date: LocalDate? = null
        var errorMessage: String? = null

        if (reader.peek() == JsonReader.Token.BEGIN_OBJECT)
            reader.beginObject()
        else
            return null

        // parse
        while (reader.hasNext()) {
            if (reader.peek() == JsonReader.Token.NAME) {
                when (reader.nextName()) {
                    "rates" -> {
                        reader.beginObject()
                        // convert
                        while (reader.hasNext()) {
                            val name: Currency? = Currency.fromString(reader.nextName())
                            val value: Float = reader.nextDouble().toFloat()

                            if (name != null)
                                rates.add(Rate(name, value))
                        }
                        reader.endObject()
                    }
                    "timestamp" -> {
                        date = Instant.ofEpochSecond(reader.nextLong())
                            .atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    "base" -> {
                        base = Currency.fromString(reader.nextString())
                    }
                    "description" -> {
                        errorMessage = reader.nextString()
                    }
                    else -> {
                        reader.skipValue()
                    }
                }
            }
        }

        reader.endObject()

        // also add Faroese króna (same as Danish krone) if it isn't already there - I simply like it!
        if (rates.find { it.currency == Currency.FOK } == null)
            rates.find { it.currency == Currency.DKK }?.value?.let { dkk ->
                rates.add(Rate(Currency.FOK, dkk))
            }

        return if (rates.isNotEmpty())
            ExchangeRates(
                success = true,
                error = null,
                base = base,
                date = date,
                rates = rates,
                provider = ApiProvider.INFOR_EURO
            )
        // error message
        else {
            ExchangeRates(
                success = false,
                error = errorMessage,
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
