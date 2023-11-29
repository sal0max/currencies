package de.salomax.currencies.model.adapter

import de.salomax.currencies.model.ApiProvider
import de.salomax.currencies.model.Currency
import de.salomax.currencies.model.Rate
import de.salomax.currencies.model.Timeline
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BankRossiiTimelineXmlParser(private val ids: Map<String, String>) {
    private val rates = mutableMapOf<LocalDate, Rate>()

    fun parse(inputStream: InputStream): Timeline {
        // create parser
        val parser = XmlPullParserFactory.newInstance()
            .apply { isNamespaceAware = false }.newPullParser()
            .apply { setInput(inputStream, null) }

        // storage
        var tagname: String? = null
        var eventType = parser.eventType
        var date: LocalDate? = null
        var currencyId: String? = null
        var value: Float? = null

        // parse
        while (eventType != XmlPullParser.END_DOCUMENT) {
            tagname = parser.name ?: tagname
            if (eventType == XmlPullParser.START_TAG) {
                if (tagname == "Record") {
                    // date
                    date = LocalDate.parse(
                        parser.getAttributeValue(null, "Date"),
                        DateTimeFormatter.ofPattern("dd.MM.yyyy")
                    )
                    // id
                    currencyId = parser.getAttributeValue(null, "Id")
                }
            } else if (eventType == XmlPullParser.TEXT) {
                if (tagname == "VunitRate")
                    value = 1f / parser.text.replace(',', '.').toFloat()
            } else if (eventType == XmlPullParser.END_TAG) {
                if (tagname == "Record") {
                    if (date != null && value != null && currencyId != null && ids[currencyId] != null) {
                        val currency = Currency.fromString(ids[currencyId]!!)
                        if (currency != null)
                            rates[date] = Rate(currency, value.toFloat())
                    }
                    // reset
                    date = null
                    currencyId = null
                    value = null
                }
            }
            eventType = parser.next()
        }

        return Timeline(
            success = rates.isNotEmpty(),
            error = null,
            base = rates.values.firstOrNull()?.currency?.iso4217Alpha(),
            startDate = rates.entries.first().key,
            endDate = rates.entries.last().key,
            rates = rates,
            provider = ApiProvider.BANK_ROSSII
        )
    }

}
