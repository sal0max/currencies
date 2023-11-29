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
import java.time.format.DateTimeFormatter

@Suppress("unused", "UNUSED_PARAMETER")
internal class InforEuroTimelineAdapter(
    private val startDate: LocalDate,
    private val endDate: LocalDate
) {

    @Synchronized
    @FromJson
    @Throws(IOException::class)
    fun fromJson(reader: JsonReader): Timeline {
        val rates = mutableMapOf<LocalDate, Rate>()
        val datePattern = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        // received rates
        return if (reader.peek() == JsonReader.Token.BEGIN_ARRAY) {
            reader.beginArray()
            // convert
            while (reader.hasNext()) {
                reader.beginObject() // begin array element
                var currencyIso: Currency? = null
                var value: Float? = null
                var dateStart: LocalDate? = null
                var dateEnd: LocalDate? = null
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "currencyIso" -> currencyIso = Currency.fromString(reader.nextString())
                        "amount" -> value = reader.nextDouble().toFloat()
                        "dateStart" -> dateStart = LocalDate.parse(reader.nextString(), datePattern)
                        "dateEnd" -> dateEnd = LocalDate.parse(reader.nextString(), datePattern)
                        else -> reader.skipValue()
                    }
                }
                // only add data to result if it matches our criteria
                if (
                    currencyIso != null && value != null
                    && dateStart != null && dateEnd != null
                    && (startDate.withDayOfMonth(1).isBefore(dateStart) // TODO: check if not isAfter()
                            || startDate.withDayOfMonth(1).isEqual(dateStart))
                ) {
                    var date: LocalDate = dateEnd
                    while (date.isAfter(dateStart) || date.isEqual(dateStart)) {
                        rates[date] = Rate(currencyIso, value)
                        date = date.minusDays(1)
                    }
                }
                reader.endObject() // end array element
            }
            reader.endArray()
            Timeline(
                success = rates.isNotEmpty(),
                error = null,
                base = Currency.EUR.iso4217Alpha(),
                startDate = startDate,
                endDate = endDate,
                rates = rates.toSortedMap(compareBy { it }), // sort ascending
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
            Timeline(
                success = false,
                error = message,
                base = Currency.EUR.iso4217Alpha(),
                startDate = null,
                endDate = null,
                rates = null,
                provider = ApiProvider.INFOR_EURO
            )
        }
    }

    @Synchronized
    @ToJson
    @Throws(IOException::class)
    fun toJson(writer: JsonWriter, value: Timeline) {
        writer.nullValue()
    }

}
