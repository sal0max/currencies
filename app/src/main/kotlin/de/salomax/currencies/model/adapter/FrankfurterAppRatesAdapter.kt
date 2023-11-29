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
            // filter out these:
            if (name != "BTC"    // Bitcoin
                // metals
                && name != "XAG" // silver
                && name != "XAU" // gold
                && name != "XPD" // palladium
                && name != "XPT" // platinum
                // superseded
                && name != "MRO" // Mauritanian ouguiya         (until 2018/01/01)
                && name != "STD" // São Tomé and Príncipe dobra (until 2018/01/01)
                && name != "VEF" // Venezuelan bolívar fuerte   (2008/01/01 – 2018/08/20)
                && name != "CUC" // Cuban convertible peso      (1994 - 2020/01/01)
                // special
                && name != "XDR" // special drawing rights of the IMF
                && name != "CLF" // Unidad de Fomento (non-circulating Chilean currency)
                && name != "CNH" // Chinese renminbi  (Offshore e.g. Hong Kong)
            ) {
                Currency.fromString(name)?.let { list.add(Rate(it, value.toFloat())) }
            }
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