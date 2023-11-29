package de.salomax.currencies.model.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import de.salomax.currencies.model.ApiProvider
import de.salomax.currencies.model.Currency
import de.salomax.currencies.model.Rate
import de.salomax.currencies.model.Timeline
import java.io.IOException
import java.time.LocalDate

@Suppress("unused", "UNUSED_PARAMETER")
internal class BankOfCanadaTimelineAdapter(
    private val base: Currency,
    private val symbol: Currency
) {

    @Synchronized
    @FromJson
    @Throws(IOException::class)
    fun fromJson(reader: JsonReader): Timeline? {
        reader.beginObject()
        // convert
        while (reader.hasNext()) {
            when (reader.nextName()) {
                // error
                "message" -> return Timeline(
                    success = false,
                    error = reader.nextString(),
                    base = null,
                    startDate = null,
                    endDate = null,
                    rates = null,
                    provider = ApiProvider.BANK_OF_CANADA
                )
                // get the values
                "observations" -> {
                    reader.beginArray()
                    return convertObservations(reader)
                }
                // not interested in those
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        return null
    }

    private fun convertObservations(reader: JsonReader): Timeline {
        var errorMessage: String? = null
        var rates = mutableMapOf<LocalDate, Rate>()

        if (reader.peek() == JsonReader.Token.END_ARRAY)
            // no data
            errorMessage = "No data found."
        else {
            while (reader.hasNext() && reader.peek() != JsonReader.Token.END_ARRAY) {
                convertObservation(reader)?.let { rates.put(it.first, it.second) }
            }
        }
        rates = rates.toSortedMap()
        return Timeline(
            success = errorMessage == null && rates.isNotEmpty(),
            error = errorMessage,
            base = base.iso4217Alpha(),
            startDate = rates.entries.first().key,
            endDate = rates.entries.last().key,
            rates = rates,
            provider = ApiProvider.BANK_OF_CANADA
        )
    }

    private fun convertObservation(reader: JsonReader): Pair<LocalDate, Rate>? {
        var date: LocalDate? = null

        var baseValue: Float? = null
        var symbolValue: Float? = null

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
                    currency?.let {
                        if (it == base)
                            baseValue = value.toFloat()
                        else if (it == symbol)
                            symbolValue = value.toFloat()
                    }
                }
            }
            if (date != null && baseValue != null && symbolValue != null) {
                reader.endObject()
                return Pair(date, Rate(symbol, baseValue!! / symbolValue!!))
            }
        }
        return null
    }

    @Synchronized
    @ToJson
    @Throws(IOException::class)
    fun toJson(writer: JsonWriter, value: Timeline?) {
        writer.nullValue()
    }

}
