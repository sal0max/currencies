package de.salomax.currencies.util

import android.icu.text.DecimalFormat
import java.lang.NumberFormatException
import java.lang.StringBuilder
import java.math.RoundingMode

fun getDecimalSeparator(): Char {
    return DecimalFormat.getInstance()?.let { it as DecimalFormat }
        // fallback to english
        ?.decimalFormatSymbols?.monetaryDecimalSeparator ?: '.'
}

fun getThousandsSeparator(): Char {
    return DecimalFormat.getInstance()?.let { it as DecimalFormat }
        // fallback to english
        ?.decimalFormatSymbols?.monetaryGroupingSeparator ?: ','
}

// *************************************************************************************************

/**
 * Changes "12345678.12" to "12 345 678.12"
 * - adds thousands separators: groups number blocks
 * - uses the decimal separator of the locale
 * - uses the correct numbers (e.g. east arabian) of the locale
 * - adds plus sign and/or a suffix, if wanted
 */
fun Number.toHumanReadableNumber(
    decimalPlaces: Int? = null,
    showPositiveSign: Boolean = false,
    suffix: String? = null,
    trim: Boolean = false
): String {
    return this.toDouble()
        .toBigDecimal()
        .toPlainString()
        .toHumanReadableNumber(decimalPlaces, showPositiveSign, suffix, trim)
}

/**
 * Changes "12345678.12" to "12 345 678.12"
 * - adds thousands separators: groups number blocks
 * - uses the decimal separator of the locale
 * - uses the correct numbers (e.g. east arabian) of the locale
 * - adds plus sign and/or a suffix, if wanted
 */
fun String.toHumanReadableNumber(
    decimalPlaces: Int? = null,
    showPositiveSign: Boolean = false,
    suffix: String? = null,
    trim: Boolean = false
): String {
    fun String.groupNumbers(): String {
        val sb = StringBuilder(this.length * 2)
        // group thousands
        for ((i, c) in this.reversed().withIndex()) {
            if (i % 3 == 0 && i != 0)
                sb.append(getThousandsSeparator())
            sb.append(c)
        }
        return sb.toString().reversed()
            // fix negative values (-.123 -> -123)
            .replace("-${getThousandsSeparator()}", "-")
    }

    val sb = StringBuilder()

    // + sign
    if (showPositiveSign && DecimalFormat.getInstance().parse(this).toFloat() >= 0)
        sb.append("+ ")

    // format number
    sb.append(this
        // round, if wished; also converts scientific to natural (123456789.123456789 instead of 1.23456789123456789E8)
        .let {
            if (decimalPlaces != null)
                it.toBigDecimal()
                    // round with bankers' rounding
                    .setScale(decimalPlaces, RoundingMode.HALF_EVEN)
                    // convert back to string
                    .toPlainString()
            else
                it
        }
        // remove trailing .0000
        .let {
            if (trim)
                it.replace("(?!^)\\.?0+$".toRegex(), "")
            else
                it
        }
        // group number blocks
        .let {
            if (it.contains('.')) {
                val split = it.split('.')
                split[0].groupNumbers() + getDecimalSeparator() + split[1]
            } else {
                it.groupNumbers()
            }
        }
        // use local numerals
        .toLocalizedNumeral()
        // add space to negative sign
        .replace("-", "- ")
    )

    // suffix
    if (suffix != null)
        sb.append(" $suffix")

    return sb.toString()
}

// *************************************************************************************************

/**
 * Can be used to convert e.g. "٣" to "3" on a device with arabic locale.
 */
fun Char.toWesternNumeral(): String {
    return try {
        Integer.parseInt(this.toString()).toString()
    } catch (e: NumberFormatException) {
        this.toString()
    }
}

/**
 * Can be used to convert e.g. "٣" to "3" on a device with arabic locale.
 */
fun String.toWesternNumeral(): String {
    val sb = StringBuilder(this.length)
    for (c in this) {
        sb.append(c.toWesternNumeral())
    }
    return sb.toString()
}

/**
 * Can be used to convert e.g. "3" to "٣" on a device with arabic locale.
 */
fun Char.toLocalizedNumeral(): String {
    return try {
        String.format("%d", this.toString().toInt())
    } catch (e: NumberFormatException) {
        this.toString()
    }
}

/**
 * Can be used to convert e.g. "312" to "٣١٢" on a device with arabic locale.
 */
fun String.toLocalizedNumeral(): String {
    val sb = StringBuilder(this.length)
    for (c in this) {
        sb.append(c.toLocalizedNumeral())
    }
    return sb.toString()
}
