package de.salomax.currencies.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Rate(
    @field:Json(name = "name") val currency: Currency,
    @field:Json(name = "value") val value: Float
)
