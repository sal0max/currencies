package de.salomax.currencies.model.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import de.salomax.currencies.model.Currency
import de.salomax.currencies.model.Rate
import java.io.IOException

/*
 * Converts currency object to array of currencies.
 * Also removes some unwanted values and adds some wanted ones.
 */
@Suppress("unused", "UNUSED_PARAMETER")
internal class FrankfurterAppRatesAdapter(private val base: Currency) {

    @Synchronized
    @FromJson
    @Throws(IOException::class)
    fun fromJson(reader: JsonReader): List<Rate> {
        val list = mutableListOf<Rate>()
        reader.beginObject()
        // convert
        while (reader.hasNext()) {
            val name: String = reader.nextName()
            val value: Double = reader.nextDouble()
            Currency.fromString(name)?.let { list.add(Rate(it, value.toFloat())) }
        }
        reader.endObject()
        // add base - but only if it's missing in the api response!
        if (list.find { rate -> rate.currency == base } == null)
            list.add(Rate(base, 1f))
        // also add Faroese króna (same as Danish krone) if it isn't already there - I simply like it!
        if (list.find { it.currency == Currency.FOK } == null)
            list.find { it.currency == Currency.DKK }?.value?.let { dkk ->
                list.add(Rate(Currency.FOK, dkk))
            }
        return list
    }

    @Synchronized
    @ToJson
    @Throws(IOException::class)
    fun toJson(writer: JsonWriter, value: List<Rate>?) {
        writer.nullValue()
    }

}