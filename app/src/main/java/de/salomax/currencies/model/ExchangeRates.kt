package de.salomax.currencies.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDate

@JsonClass(generateAdapter = true)
data class ExchangeRates(
    @field:Json(name = "base") val base: String,
    @field:Json(name = "date") val date: LocalDate,
    @field:Json(name = "rates") val rates: List<Rate>
)
