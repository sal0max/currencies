package de.salomax.currencies.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Converts a Unix timestamp to a LocalDate
 */
fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this)
    .atZone(ZoneOffset.UTC)
    .toLocalDate()

/**
 * Converts a LocalDate to a Unix timestamp
 */
fun LocalDate.toMillis() = this
    .atStartOfDay(ZoneOffset.UTC)
    .toInstant()
    .toEpochMilli()
