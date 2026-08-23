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
internal class AllRatesTodayRatesAdapter(
    private val requestedBase: Currency,
    private val requestedDate: LocalDate?
) {

    @Synchronized
    @FromJson
    @Throws(IOException::class)
    fun fromJson(reader: JsonReader): ExchangeRates {
        val rates = mutableListOf<Rate>()
        var responseDate: LocalDate? = null

        reader.beginArray()
        while (reader.hasNext()) {
            var source: Currency? = null
            var target: Currency? = null
            var value: Float? = null
            var time: String? = null

            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "source" -> source = Currency.fromString(reader.nextString())
                    "target" -> target = Currency.fromString(reader.nextString())
                    "rate" -> value = reader.nextDouble().toFloat()
                    "time" -> time = reader.nextString()
                    else -> reader.skipValue()
                }
            }
            reader.endObject()

            if (responseDate == null)
                responseDate = time?.take(10)?.let(LocalDate::parse)
            if (source == requestedBase && target != null && value != null)
                rates.add(Rate(target, value))
        }
        reader.endArray()

        if (rates.none { it.currency == requestedBase })
            rates.add(Rate(requestedBase, 1f))
        if (rates.none { it.currency == Currency.FOK })
            rates.find { it.currency == Currency.DKK }?.let { rates.add(Rate(Currency.FOK, it.value)) }

        return ExchangeRates(
            success = true,
            error = null,
            base = requestedBase,
            date = responseDate ?: requestedDate ?: LocalDate.now(),
            rates = rates,
            provider = ApiProvider.ALL_RATES_TODAY
        )
    }

    @Synchronized
    @ToJson
    @Throws(IOException::class)
    fun toJson(writer: JsonWriter, value: ExchangeRates) {
        writer.nullValue()
    }
}
