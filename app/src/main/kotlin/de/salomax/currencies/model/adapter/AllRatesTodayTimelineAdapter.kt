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
internal class AllRatesTodayTimelineAdapter(
    private val base: Currency,
    private val symbol: Currency,
    private val startDate: LocalDate,
    private val endDate: LocalDate
) {

    @Synchronized
    @FromJson
    @Throws(IOException::class)
    fun fromJson(reader: JsonReader): Timeline {
        val rates = mutableMapOf<LocalDate, Rate>()

        reader.beginArray()
        while (reader.hasNext()) {
            var target: Currency? = null
            var value: Float? = null
            var date: LocalDate? = null

            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "target" -> target = Currency.fromString(reader.nextString())
                    "rate" -> value = reader.nextDouble().toFloat()
                    "time" -> date = reader.nextString().take(10).let(LocalDate::parse)
                    else -> reader.skipValue()
                }
            }
            reader.endObject()

            if (target != null && value != null && date != null)
                rates[date] = Rate(symbol, value)
        }
        reader.endArray()

        return Timeline(
            success = true,
            error = null,
            base = base.iso4217Alpha(),
            startDate = startDate,
            endDate = endDate,
            rates = rates.toSortedMap(),
            provider = ApiProvider.ALL_RATES_TODAY
        )
    }

    @Synchronized
    @ToJson
    @Throws(IOException::class)
    fun toJson(writer: JsonWriter, value: Timeline) {
        writer.nullValue()
    }
}
