package de.salomax.currencies.model.adapter

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream

class BankRossiiCurrencyCodesXmlParser {
    private val items = mutableMapOf<String, String>()

    fun parse(inputStream: InputStream): MutableMap<String, String> {
        // create parser
        val parser = XmlPullParserFactory.newInstance()
            .apply { isNamespaceAware = false }.newPullParser()
            .apply { setInput(inputStream, null) }

        // storage
        var tagname: String? = null
        var eventType = parser.eventType
        var id: String? = null
        var iso4217Alpha: String? = null

        // parse
        while (eventType != XmlPullParser.END_DOCUMENT) {
            tagname = parser.name ?: tagname
            if (eventType == XmlPullParser.START_TAG) {
                if (tagname == "Item")
                    id = parser.getAttributeValue(null, "ID")
            } else if (eventType == XmlPullParser.TEXT) {
                if (tagname == "ISO_Char_Code")
                    iso4217Alpha = parser.text
            } else if (eventType == XmlPullParser.END_TAG) {
                if (tagname == "Item") {
                    if (id != null && iso4217Alpha != null)
                        items[id] = iso4217Alpha
                    // reset
                    id = null
                    iso4217Alpha = null
                }
            }
            eventType = parser.next()
        }

        return items
    }

}
