package de.salomax.currencies.util

import android.content.Context
import de.salomax.currencies.R
import java.lang.StringBuilder

/**
 * returns e.g. "+ 0.1231 %" or "- 0.41512 %"
 */
fun Float.prettyPrintPercent(context: Context): String {
    val sb = StringBuilder()
    // sign
    if (this >= 0) sb.append("+ ")
    sb.append(
        this.toString()
            // decimal separator
            .replace(".", context.getString(R.string.decimal_separator))
            // sign
            .replace("-", "- ")
    )
    // percent
    sb.append(" %")
    return sb.toString()
}

/**
 * returns e.g. "+ 0.12 %" or "- 41.32 %"
 */
fun Float.prettyPrintPercent(context: Context, decimalPlaces: Int): String {
    return String
        // round (with right padding)
        .format("%.${decimalPlaces}f", this)
        .toFloat()
        .prettyPrintPercent(context)
}

/**
 * returns e.g. "- 0.1231" or "0.21311"
 */
fun Float.prettyPrint(context: Context): String {
    return this.toString()
        // decimal separator
        .replace(".", context.getString(R.string.decimal_separator))
        // sign
        .replace("-", "- ")
}

/**
 * returns e.g. "- 0.12" or "0.54"
 */
fun Float.prettyPrint(context: Context, decimalPlaces: Int): String {
    return String
        // round (with right padding)
        .format("%.${decimalPlaces}f", this)
        // decimal separator
        .replace(".", context.getString(R.string.decimal_separator))
        // sign
        .replace("-", "- ")
}
