package de.salomax.currencies.util

import android.content.Context
import de.salomax.currencies.R
import java.lang.StringBuilder


fun Float.humanReadableFee(context: Context): String {
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
