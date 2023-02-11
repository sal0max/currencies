@file:Suppress("SpellCheckingInspection", "unused")

package de.salomax.helpers.currencies

import org.jsoup.Jsoup
import java.util.*

// !! change this to the target language !!
private const val TARGET_LANGUAGE = "he"

private const val USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:107.0) Gecko/20100101 Firefox/107.0"
private val ISO_NAMES = listOf(
    "AED", "AFN", "ALL", "AMD", "ANG", "AOA", "ARS", "AUD", "AWG", "AZN", "BAM", "BBD", "BDT",
    "BGN", "BHD", "BIF", "BMD", "BND", "BOB", "BRL", "BSD", "BTC", "BTN", "BWP", "BYN", "BZD",
    "CAD", "CDF", "CHF", "CLF", "CLP", "CNH", "CNY", "COP", "CRC", "CUC", "CUP", "CVE", "CZK",
    "DJF", "DKK", "DOP", "DZD", "EGP", "ERN", "ETB", "EUR", "FJD", "FKP", "FOK", "GBP", "GEL",
    "GGP", "GHS", "GIP", "GMD", "GNF", "GTQ", "GYD", "HKD", "HNL", "HRK", "HTG", "HUF", "IDR",
    "ILS", "IMP", "INR", "IQD", "IRR", "ISK", "JEP", "JMD", "JOD", "JPY", "KES", "KGS", "KHR",
    "KMF", "KPW", "KRW", "KWD", "KYD", "KZT", "LAK", "LBP", "LKR", "LRD", "LSL", "LYD", "MAD",
    "MDL", "MGA", "MKD", "MMK", "MNT", "MOP", "MRO", "MRU", "MUR", "MVR", "MWK", "MXN", "MYR",
    "MZN", "NAD", "NGN", "NIO", "NOK", "NPR", "NZD", "OMR", "PAB", "PEN", "PGK", "PHP", "PKR",
    "PLN", "PYG", "QAR", "RON", "RSD", "RUB", "RWF", "SAR", "SBD", "SCR", "SDG", "SEK", "SGD",
    "SHP", "SLE", "SLL", "SOS", "SRD", "SSP", "STD", "STN", "SVC", "SYP", "SZL", "THB", "TJS",
    "TMT", "TND", "TOP", "TRY", "TTD", "TWD", "TZS", "UAH", "UGX", "USD", "UYU", "UZS", "VEF",
    "VES", "VND", "VUV", "WST", "XAF", "XAG", "XAU", "XCD", "XDR", "XOF", "XPD", "XPF", "XPT",
    "YER", "ZAR", "ZMW", "ZWL"
)

fun main() {
    for (isoName in ISO_NAMES) {
        val iso = isoName.lowercase()

        // bad results; don't use!
//        getLocaleStringFromExchangeRatesCom(TARGET_LANGUAGE, iso)

        val s0 = getLocaleStringFromInvestingCom(TARGET_LANGUAGE, iso)
        val s1 = getWiseCom(TARGET_LANGUAGE, iso)
        val s2 = getCoinmillCom(TARGET_LANGUAGE, iso)
        val s3 = getMataffNet(TARGET_LANGUAGE, iso)

        val hits = listOf(s0, s1, s2, s3)
            .filterNot { it.isNullOrEmpty() }
        val s = hits
                .groupingBy { it }
                .eachCount()
                .filter {
                    // good enough: 3/4
                    if (hits.size == 4)
                        it.value >= 3
                    // good enough: 2/3, 2/2
                    else if (hits.size >= 2)
                        it.value >= 2
                    else
                        false
                }
                .keys.let { if (it.isEmpty()) null else it.first() }

        if (s != null)
            println("""<string name="name_$iso">$s</string>""")
        else
            println()

        Thread.sleep(1200)
    }
}

fun getLocaleStringFromInvestingCom(targetLanguage: String, iso4217Alpha: String): String? {
    return try {
        val doc = Jsoup.connect(
            "https://$targetLanguage.investing.com/currencies/" +
                    "eur-${iso4217Alpha.lowercase()}"
        )
            .userAgent(USER_AGENT)
            .get()
        doc.selectFirst("[data-test=instrument-bottom-secondary-value]")
            ?.text()
            ?.uppercase()
    } catch (e: Exception) {
        null
    }
}

fun getLocaleStringFromExchangeRatesCom(targetLanguage: String, iso4217Alpha: String): String? {
    return try {
        val doc = Jsoup.connect(
            "https://www.exchange-rates.com/${targetLanguage.lowercase()}/" +
                    "EUR/${iso4217Alpha.uppercase()}"
        )
            .userAgent(USER_AGENT)
            .get()
        val result = doc.selectFirst("h1")
            ?.text()
            ?.replace("Euro \\w+ ".toRegex(), "")
            ?.replace(" Växelkurs", "")
            ?.replace(" Kurs wymiany", "")
            ?.uppercase()
        if (result == "Exchange Rates and Currency Converter") null else result
    } catch (e: Exception) {
        null

    }
}

fun getWiseCom(targetLanguage: String, iso4217Alpha: String): String? {
    return try {
        val doc = Jsoup.connect(
            "https://wise.com/${targetLanguage.lowercase()}/" +
                    "currency-converter/" +
                    "eur-to-${iso4217Alpha.lowercase()}-rate"
        )
            .userAgent(USER_AGENT)
            .get()
        doc.selectFirst("th[colspan]")
            ?.text()
            ?.substringAfter("/ ")
            ?.uppercase()
    } catch (e: Exception) {
        null
    }
}

fun getCoinmillCom(targetLanguage: String, iso4217Alpha: String): String? {
    return try {
        val doc = Jsoup.connect(
            "https://${targetLanguage.lowercase()}.coinmill.com/" +
                    "${iso4217Alpha.lowercase()}_EUR"
        )
            .userAgent(USER_AGENT)
            .get()
        doc.selectFirst("#currencyBox0 > a")
            ?.text()
            ?.substringBefore(" (")
            ?.uppercase()
    } catch (e: Exception) {
        null
    }
}

fun getMataffNet(targetLanguage: String, iso4217Alpha: String): String? {
    return try {
        val doc = Jsoup.connect(
            "https://www.mataf.net/" +
                    "${targetLanguage.lowercase()}/" +
                    "currency/" +
                    "converter-EUR-${iso4217Alpha.uppercase()}"
        )
            .userAgent(USER_AGENT)
            .get()
        doc.selectFirst(".caption-helper")
            ?.text()
            ?.substringAfter("/ ")
            ?.uppercase()
    } catch (e: Exception) {
        null
    }
}

private fun String?.uppercase() = this?.replaceFirstChar {
    if (it.isLowerCase())
        it.titlecase(Locale.forLanguageTag(TARGET_LANGUAGE))
    else
        it.toString()
}