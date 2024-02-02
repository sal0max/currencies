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
internal class CeskaNarodniBankaRatesAdapter {

    @Synchronized
    @FromJson
    @Throws(IOException::class)
    fun fromJson(reader: JsonReader): ExchangeRates {
        val rates = mutableListOf<Rate>()

        // open object
        reader.beginObject()

        // received rates
        return if (reader.nextName() == "rates") {
            reader.beginArray()
            // convert
            var date: LocalDate? = null
            while (reader.hasNext()) {
                reader.beginObject() // begin array element
                var name: Currency? = null
                var value: Float? = null
                var multiplier: Int? = null
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "currencyCode" -> name = Currency.fromString(reader.nextString())
                        "rate" -> value = reader.nextDouble().toFloat()
                        "amount" -> multiplier = reader.nextInt()
                        "validFor" -> date = LocalDate.parse(reader.nextString())
                        else -> reader.skipValue()
                    }
                }
                if (name != null && name != Currency.XDR && value != null && multiplier != null)
                    rates.add(Rate(name, (1 / value) * multiplier))
                reader.endObject() // end array element
            }
            // add base
            if (rates.find { rate -> rate.currency == Currency.CZK } == null)
                rates.add(Rate(Currency.CZK, 1f))
            // also add Faroese króna (same as Danish krone) if it isn't already there
            if (rates.find { it.currency == Currency.FOK } == null)
                rates.find { it.currency == Currency.DKK }?.value?.let { dkk ->
                    rates.add(Rate(Currency.FOK, dkk))
                }
            reader.endArray()
            ExchangeRates(
                success = rates.isNotEmpty(),
                error = null,
                base = Currency.CZK,
                date = date,
                rates = rates,
                provider = ApiProvider.CESKA_NARODNI_BANKA
            )
        }
        // error message
        else {
            reader.beginObject()
            var message: String? = null
            var date: LocalDate? = null
            while (reader.hasNext()) {
                if (reader.nextName() == "description")
                    message = reader.nextString()
                else if (reader.nextName() == "happenedAt")
                    date = try {
                        LocalDate.parse(reader.nextString().substringBefore("T"))
                    } catch (e: Exception) { null }
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
    fun toJson(writer: JsonWriter, value: List<Rate>?) {
        writer.nullValue()
    }

}
