package de.salomax.currencies.model.adapter

import de.salomax.currencies.model.ApiProvider
import de.salomax.currencies.model.Currency
import de.salomax.currencies.model.Rate
import de.salomax.currencies.model.Timeline
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.time.LocalDate
import kotlin.math.pow

class NorgesBankTimelineXmlParser(
    val base: Currency,
    val symbol: Currency,
    val startDate: LocalDate,
    val endDate: LocalDate
) {
    private val ratesList = mutableListOf<Map<LocalDate, Rate>>()

    fun parse(inputStream: InputStream): Timeline {
        // create parser
        val parser = XmlPullParserFactory.newInstance()
            .apply { isNamespaceAware = false }.newPullParser()
            .apply { setInput(inputStream, null) }

        // parse
        var eventType = parser.eventType
        var base: Currency? = null
        var multiplier = 1
        val currentTimeline = mutableMapOf<LocalDate, Rate>()
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
                        // add rate
                        currentTimeline[date] = Rate(base, (1f / value) * multiplier)
                    }
                }
            }

            // persist current timeline
            else if (eventType == XmlPullParser.END_TAG && tagname.equals("Series", ignoreCase = true)) {
                ratesList.add(currentTimeline.toMap())
                currentTimeline.clear()
            }

            eventType = parser.next()
        }

        // Always manually add a NOK (constant 1.0) series.
        // Neded for timelines with NOK, as the API doesn't return NOK, even if requested.
        ratesList.add(object: LinkedHashMap<LocalDate, Rate>() {
            var currentDate = startDate
            init {
                while (currentDate.isBefore(endDate) || currentDate.isEqual(endDate)) {
                    this[currentDate] = Rate(Currency.NOK, 1f)
                    currentDate = currentDate.plusDays(1)
                }
            }
        })

        // merge all series
        val rates = mutableMapOf<LocalDate, Rate>()
        val baseList = ratesList.find { it.entries.first().value.currency == this.base }
        val symbolList = ratesList.find { it.entries.first().value.currency == this.symbol }
        if (baseList != null && symbolList != null) {
            for (entry in baseList) {
                val baseValue = entry.value.value
                val symbolValue = symbolList[entry.key]?.value
                if (symbolValue != null)
                    rates[entry.key] = Rate(entry.value.currency, symbolValue.div(baseValue))
            }
        }

        return Timeline(
            success = rates.isNotEmpty(),
            error = null,
            base = base?.iso4217Alpha(),
            startDate = rates.entries.first().key,
            endDate = rates.entries.last().key,
            rates = rates.toSortedMap(),
            provider = ApiProvider.NORGES_BANK
        )
    }

}
