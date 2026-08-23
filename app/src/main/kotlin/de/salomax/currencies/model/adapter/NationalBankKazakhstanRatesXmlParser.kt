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

class NationalBankKazakhstanRatesXmlParser {

    fun parse(inputStream: InputStream): ExchangeRates {
        val parser = XmlPullParserFactory.newInstance()
            .apply { isNamespaceAware = false }.newPullParser()
            .apply { setInput(inputStream, null) }

        var eventType = parser.eventType
        var currentTag: String? = null
        var date: LocalDate? = null
        var currency: Currency? = null
        var quantity: Float? = null
        var value: Float? = null
        val rates = mutableListOf<Rate>()

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> currentTag = parser.name
                XmlPullParser.TEXT -> when (currentTag) {
                    "date", "pubDate" -> if (date == null) {
                        date = parser.text.trim().takeIf { it.isNotEmpty() }?.let {
                            LocalDate.parse(it, DATE_FORMATTER)
                        }
                    }
                    "title" -> currency = Currency.fromString(parser.text.trim())
                    "quant" -> quantity = parser.text.trim().toFloatOrNull()
                    "description" -> value = parser.text.trim().replace(',', '.').toFloatOrNull()
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "item") {
                        if (currency != null && quantity != null && value != null) {
                            NationalBankKazakhstanRateConverter.normalize(quantity, value)?.let {
                                rates.add(Rate(currency, it))
                            }
                        }
                        currency = null
                        quantity = null
                        value = null
                    }
                    currentTag = null
                }
            }
            eventType = parser.next()
        }

        if (rates.isNotEmpty()) {
            rates.add(Rate(Currency.KZT, 1f))
            rates.find { it.currency == Currency.DKK }?.value?.let { dkk ->
                rates.add(Rate(Currency.FOK, dkk))
            }
        }

        return ExchangeRates(
            success = rates.isNotEmpty(),
            error = if (rates.isEmpty()) "No data found." else null,
            base = Currency.KZT,
            date = date,
            rates = rates,
            provider = ApiProvider.NATIONAL_BANK_KAZAKHSTAN
        )
    }

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    }
}
