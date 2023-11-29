package de.salomax.currencies.model.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import de.salomax.currencies.model.Currency
import de.salomax.currencies.model.Rate
import java.io.IOException
import java.time.LocalDate

/*
 * Converts a timeline rates object to a Map<LocalDate, Rate?>>
 * The API actually returns Map<LocalDate, List<Rate>>>, however, we only want one Rate per day.
 * This converter reduces the list.
 */
@Suppress("unused", "UNUSED_PARAMETER")
internal class FrankfurterAppTimelineAdapter(private val symbol: Currency) {

    @Synchronized
    @FromJson
    fun fromJson(reader: JsonReader): Map<LocalDate, Rate> {
        val map = mutableMapOf<LocalDate, Rate>()
        reader.beginObject()
        // convert
        while (reader.hasNext()) {
            val date: LocalDate = LocalDate.parse(reader.nextName())
            var rate: Rate? = null
            reader.beginObject()
            // sometimes there's no rate yet, but an empty body or more than one rate, so check first
            while (reader.hasNext() && reader.peek() == JsonReader.Token.NAME) {
                val name = Currency.fromString(reader.nextName())
                val value = reader.nextDouble().toFloat()
                rate =
                        // change dkk to fok, when needed
                    if (name == Currency.DKK && symbol == Currency.FOK)
                        Rate(Currency.FOK, value)
                    // make sure that the symbol matches the one we requested
                    else if (name == symbol)
                        Rate(name, value)
                    else
                        null
            }
            if (rate != null)
                map[date] = rate
            reader.endObject()
        }
        reader.endObject()
        return map
    }

    @Synchronized
    @ToJson
    @Throws(IOException::class)
    fun toJson(writer: JsonWriter, value: Map<LocalDate, Rate>?) {
        writer.nullValue()
    }

}
