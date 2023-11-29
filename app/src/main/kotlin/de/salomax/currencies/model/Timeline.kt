package de.salomax.currencies.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDate

@JsonClass(generateAdapter = true)
data class Timeline(
    @field:Json(name = "success") val success: Boolean?,
    @field:Json(name = "error") val error: String?,

    @field:Json(name = "base") val base: String?,
    @field:Json(name = "start_date") val startDate: LocalDate?,
    @field:Json(name = "end_date") val endDate: LocalDate?,
    @field:Json(name = "rates") val rates: Map<LocalDate, Rate>?,

    @Transient val provider: ApiProvider? = null
)
