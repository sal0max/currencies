package de.salomax.currencies.model.adapter

import de.salomax.currencies.model.ApiProvider
import de.salomax.currencies.model.Currency
import de.salomax.currencies.model.ExchangeRates
import de.salomax.currencies.model.Rate
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BankRossiiRatesXmlParser {
    private var date: LocalDate? = null
    private val rates = mutableListOf<Rate>()

    fun parse(inputStream: InputStream): ExchangeRates {
        // create parser
        val parser = XmlPullParserFactory.newInstance()
            .apply { isNamespaceAware = false }.newPullParser()
            .apply { setInput(inputStream, null) }

        // storage
        var tagname: String? = null
        var eventType = parser.eventType
        var currency: Currency? = null
        var value: Float? = null

        // parse
        while (eventType != XmlPullParser.END_DOCUMENT) {
            tagname = parser.name ?: tagname
            if (eventType == XmlPullParser.START_TAG) {
                if (tagname == "ValCurs")
                    // date
                    date = LocalDate.parse(
                        parser.getAttributeValue(null, "Date"),
                        DateTimeFormatter.ofPattern("dd.MM.yyyy")
                    )
            } else if (eventType == XmlPullParser.TEXT) {
                when (tagname) {
                    // currency
                    "CharCode" -> currency = Currency.fromString(parser.text)
                    // rate
                    "VunitRate" -> value = 1f / parser.text.replace(',', '.').toFloat()
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                if (tagname == "Valute") {
                    // store rate (filter out XDR)
                    if (currency != null && value != null && currency != Currency.XDR) {
                        rates.add(Rate(currency, value))
                    }
                    // reset
                    currency = null
                    value = null
                }
            }
            eventType = parser.next()
        }

        if (rates.isNotEmpty()) {
            // finally, add RUB...
            rates.add(Rate(Currency.RUB, 1f))
            // ...and FOK
            if (rates.find { it.currency == Currency.FOK } == null)
                rates.find { it.currency == Currency.DKK }?.value?.let { dkk ->
                    rates.add(Rate(Currency.FOK, dkk))
                }
        }

        return ExchangeRates(
            success = rates.isNotEmpty(),
            error = null,
            base = Currency.RUB,
            date = date,
            rates = rates,
            provider = ApiProvider.BANK_ROSSII
        )
    }

}
