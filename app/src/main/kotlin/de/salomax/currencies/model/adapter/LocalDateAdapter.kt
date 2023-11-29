package de.salomax.currencies.model.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import java.io.IOException
import java.time.LocalDate

@Suppress("unused")
internal class LocalDateAdapter {

    @Synchronized
    @FromJson
    @Throws(IOException::class)
    fun fromJson(reader: JsonReader): LocalDate? {
        return LocalDate.parse(reader.nextString())
    }

    @Synchronized
    @ToJson
    @Throws(IOException::class)
    fun toJson(writer: JsonWriter, value: LocalDate?) {
        writer.value(value?.toString())
    }

}
