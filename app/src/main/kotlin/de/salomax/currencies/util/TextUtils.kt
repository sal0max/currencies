package de.salomax.currencies.util

import android.content.Context
import de.salomax.currencies.R
import java.lang.StringBuilder

/**
 * returns e.g. "+ 0.1231 %" or "- 0.41512 %"
 */
@Suppress("unused")
fun Float.prettyPrintPercent(context: Context): String {
    return this
        .toString()
        .prettyPrintPercent(context)
}

/**
 * returns e.g. "+ 0.12 %" or "- 41.32 %"
 */
@Suppress("unused")
fun Float.prettyPrintPercent(context: Context, decimalPlaces: Int): String {
    return String
        // round (with right padding)
        .format("%.${decimalPlaces}f", this)
        .prettyPrintPercent(context)
}

private fun String.prettyPrintPercent(context: Context): String {
    val sb = StringBuilder()
    // sign
    if (!this.contains("-")) sb.append("+ ")
    sb.append(
        this
            // decimal separator
            .replace(".", context.getString(R.string.decimal_separator))
            // sign
            .replace("-", "- ")
    )
    // percent
    sb.append(" %")
    return sb.toString()
}

// *************************************************************************************************

/**
 * returns e.g. "- 0.1231" or "0.21311"
 */
@Suppress("unused")
fun Float.prettyPrint(context: Context): String {
    return this
        .toString()
        .prettyPrint(context)
}

/**
 * returns e.g. "- 0.12" or "0.54"
 */
@Suppress("unused")
fun Float.prettyPrint(context: Context, decimalPlaces: Int): String {
    return String
        // round (with right padding)
        .format("%.${decimalPlaces}f", this)
        .prettyPrint(context)
}

private fun String.prettyPrint(context: Context): String {
    return this
        // decimal separator
        .replace(".", context.getString(R.string.decimal_separator))
        // sign
        .replace("-", "- ")
}
