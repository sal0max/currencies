package de.salomax.currencies.util

import android.content.Context
import de.salomax.currencies.R
import java.lang.StringBuilder
import kotlin.math.round

// TODO nice new names for all those functions

/**
 * returns e.g. + 0.1231 %
 */
fun Float.humanReadablePercentage(context: Context): String {
    val sb = StringBuilder()
    if (this >= 0)
        sb.append("+ ")
    sb.append(this.toString()
        .replace(".", context.getString(R.string.decimal_separator))
        .replace("-", "- ")
    )
    sb.append(" %")
    return sb.toString()
}

/**
 * returns e.g. + 0.12 %
 */
fun Float.humanReadablePercentage(context: Context, decimalPlaces: Int): String {
    return this.round(decimalPlaces).humanReadablePercentage(context)
}

/**
 * returns e.g. - 0.1231
 */
fun Float.humanReadable(context: Context): String {
    val sb = StringBuilder()
    sb.append(this.toString()
        .replace(".", context.getString(R.string.decimal_separator))
        .replace("-", "- ")
    )
    return sb.toString()
}

/**
 * returns e.g. - 0.12
 */
fun Float.humanReadable(context: Context, decimalPlaces: Int): String {
    return this.round(decimalPlaces).humanReadable(context)
}

private fun Float.round(decimals: Int): Float {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return (round(this * multiplier) / multiplier).toFloat()
}
