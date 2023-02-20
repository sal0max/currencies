package de.salomax.currencies.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import de.salomax.currencies.R
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.*

/**
 * Return the *used* Locale, based on the currently active resource folder,
 * not the one set in the System (which one would get with context.resources.configuration.locales[0]).
 * Example: app is localized in en (default) + fr
 * - system=en & app=system-default -> en
 * - system=fr & app=system-default -> fr
 * - system=en & app=en             -> en
 * - system=en & app=fr             -> fr
 * - system=af & app=system-default -> en (as there's no af localization it falls back to en)
 */
fun getLocale(context: Context): Locale {
    return AppCompatDelegate.getApplicationLocales()[0] ?: Locale(
        context.getString(R.string.locale_language),
        context.getString(R.string.locale_country)
    )
}

/**
 * Returns the DecimalFormatSymbols for the localization that is active in the app.
 */
private fun getDecimalSymbols(context: Context): DecimalFormatSymbols {
    val decimalFormatter = NumberFormat.getInstance(getLocale(context)) as DecimalFormat
    return decimalFormatter.decimalFormatSymbols
}

/**
 * Returns the decimal separator character for the localization that is active in the app.
 */
fun getDecimalSeparator(context: Context): String {
    return getDecimalSymbols(context).decimalSeparator.toString()
}

/**
 * Returns the grouping separator character for the localization that is active in the app.
 */
fun getGroupingSeparator(context: Context): String {
    return getDecimalSymbols(context).groupingSeparator.toString()
}

/**
 * True, when the currency symbol should be placed after the value for the current locale.
 * False, when the currency symbol should be placed before the value.
 */
fun hasAppendedCurrencySymbol(context: Context): Boolean {
    val currencyFormatter = NumberFormat.getCurrencyInstance(getLocale(context))
    val formattedCurrency = currencyFormatter.format(1.23)
    return formattedCurrency.last().digitToIntOrNull() == null
}

// *************************************************************************************************

/**
 * Changes "12345678.12" to "12 345 678.12"
 * - adds thousands separators: groups number blocks
 * - uses the decimal separator of the locale
 * - uses the correct numbers (e.g. east arabian) of the locale
 * - adds plus sign and/or a suffix, if wanted
 */
fun Float.toHumanReadableNumber(
    context: Context,
    decimalPlaces: Int? = null,
    showPositiveSign: Boolean = false,
    suffix: String? = null,
    trim: Boolean = false
): String {
    return this
        .toBigDecimal()
        .toPlainString()
        .toHumanReadableNumber(context, decimalPlaces, showPositiveSign, suffix, trim)
}

/**
 * Changes "12345678.12" to "12 345 678.12"
 * - adds thousands separators: groups number blocks
 * - uses the decimal separator of the locale
 * - adds plus sign and/or a suffix, if wanted
 */
fun String.toHumanReadableNumber(
    context: Context,
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
                sb.append(getGroupingSeparator(context))
            sb.append(c)
        }
        return sb.toString().reversed()
            // fix negative values (-.123 -> -123)
            .replace("-${getGroupingSeparator(context)}", "-")
    }


    val sb = StringBuilder()

    // + sign
    if (showPositiveSign
        && DecimalFormat.getInstance().parse(this) != null
        && DecimalFormat.getInstance().parse(this)!!.toFloat() >= 0
    )
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
                split[0].groupNumbers() + getDecimalSeparator(context) + split[1]
            } else {
                it.groupNumbers()
            }
        }
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
 * Parses the given string to a number. Uses the default locale for thousands and decimal separators.
 * - Returns null, if invalid characters are found.
 * - Also returns null, for negative values
 */
fun CharSequence.toNumber(): Number? {
    if (this.isBlank())
        return null
    // allow 0-9 , . whitespace
    if (!this.matches("[0-9,.\\s]+".toRegex()))
        return null
    return NumberFormat.getNumberInstance().parse(
        toString()
            .replace("\\s+".toRegex(), "")
    )
}
