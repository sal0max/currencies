package de.salomax.currencies.model.adapter

import de.salomax.currencies.model.ApiProvider
import de.salomax.currencies.model.Currency
import de.salomax.currencies.model.ExchangeRates
import de.salomax.currencies.model.Rate
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.time.LocalDate
import kotlin.math.pow

class NorgesBankRatesXmlParser {
    private var date: LocalDate? = null
    private val rates = mutableListOf<Rate>()

    fun parse(inputStream: InputStream): ExchangeRates {
        // create parser
        val parser = XmlPullParserFactory.newInstance()
            .apply { isNamespaceAware = false }.newPullParser()
            .apply { setInput(inputStream, null) }

        // parse
        var eventType = parser.eventType
        var base: Currency? = null
        var multiplier = 1
        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagname = parser.name
            if (eventType == XmlPullParser.START_TAG) {

                if (tagname.equals("Series", ignoreCase = true)) {
                    // get foreign currency
                    base = Currency.fromString(parser.getAttributeValue(null, "BASE_CUR"))
                    // get multiplier
                    parser.getAttributeValue(null, "UNIT_MULT")?.toIntOrNull()?.let {
                        multiplier = 10.0.pow(it).toInt()
                    }

                } else if (tagname.equals("Obs", ignoreCase = true)) {
                    // get date
                    val date = LocalDate.parse(parser.getAttributeValue(null, "TIME_PERIOD"))
                    // get value
                    val value = parser.getAttributeValue(null, "OBS_VALUE").toFloatOrNull()
                    // filter out XDR
                    if (base != null && value != null && base != Currency.XDR) {
                        // check if there is already an older value and remove it
                        // (api is sorted ascending, so later currencies are always newer)
                        rates.removeIf { rate -> rate.currency == base }
                        // add rate
                        rates.add(Rate(base, (1f / value) * multiplier))
                    }
                    base = null
                    // found a newer date -> update
                    if (this.date == null || this.date?.isBefore(date) == true) {
                        this.date = date
                    }
                }
            }
            eventType = parser.next()
        }

        if (rates.isNotEmpty()) {
            // finally, add NOK...
            rates.add(Rate(Currency.NOK, 1f))
            // ...and FOK
            if (rates.find { it.currency == Currency.FOK } == null)
                rates.find { it.currency == Currency.DKK }?.value?.let { dkk ->
                    rates.add(Rate(Currency.FOK, dkk))
                }
        }

        return ExchangeRates(
            success = rates.isNotEmpty(),
            error = null,
            base = Currency.NOK,
            date = date,
            rates = rates,
            provider = ApiProvider.NORGES_BANK
        )
    }

}
