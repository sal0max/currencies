package de.salomax.currencies.model.provider

import android.content.Context
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.core.awaitResult
import com.github.kittinunf.result.Result
import de.salomax.currencies.R
import de.salomax.currencies.model.ApiProvider
import de.salomax.currencies.model.Currency
import de.salomax.currencies.model.ExchangeRates
import de.salomax.currencies.model.Timeline
import de.salomax.currencies.model.adapter.NorgesBankRatesXmlParser
import de.salomax.currencies.model.adapter.NorgesBankTimelineXmlParser
import java.io.InputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class NorgesBank: ApiProvider.Api() {

    override val name = "Norges Bank"

    override fun description(context: Context) =
        context.getText(R.string.api_about_norgesBank)

    override fun updateIntervalDescription(context: Context) =
        context.getText(R.string.api_refreshPeriod_norgesBank)

    override val baseUrl = "https://data.norges-bank.no/api"

    override suspend fun getRates(date: LocalDate?): Result<ExchangeRates, FuelError> {
        // As this API doesn't return results for nonwork days, get the last seven days.
        // The latest available values will be used.
        val formattedDateStart = date?.minusDays(7)?.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val formattedDateEnd = date?.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val dateString =
            // latest
            if (date == null) "?lastNObservations=1"
            // historical
            else "?StartPeriod=$formattedDateStart&EndPeriod=$formattedDateEnd"

        return Fuel.get(
            baseUrl +
                    "/data" +
                    "/EXR" +
                    "/B..NOK.SP" +
                    dateString +
                    "&format=sdmx-compact-2.1"
        ).awaitResult(
            object : ResponseDeserializable<ExchangeRates> {
                override fun deserialize(inputStream: InputStream): ExchangeRates {
                    return NorgesBankRatesXmlParser().parse(inputStream)
                }
            }
        )
    }

    override suspend fun getTimeline(
        base: Currency,
        symbol: Currency,
        startDate: LocalDate,
        endDate: LocalDate
    ): Result<Timeline, FuelError> {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        // can't search for FOK - have to use DKK instead
        val parameterBase = if (base == Currency.FOK) "DKK" else base.iso4217Alpha()
        val parameterSymbol = if (symbol == Currency.FOK) "DKK" else symbol.iso4217Alpha()

        // if we request NOK->NOK, the response would be empty. Add EUR in that case.
        val eur = if (base == Currency.NOK && symbol == Currency.NOK) "EUR" else ""
        // call API
        return Fuel.get(
            baseUrl +
                    "/data" +
                    "/EXR" +
                    "/B.$parameterBase+$parameterSymbol+$eur.NOK.SP" +
                    "?StartPeriod=${dateFormatter.format(startDate)}" +
                    "&EndPeriod=${dateFormatter.format(endDate)}" +
                    "&format=sdmx-compact-2.1"
        ).awaitResult(
            object : ResponseDeserializable<Timeline> {
                override fun deserialize(inputStream: InputStream): Timeline {
                    return NorgesBankTimelineXmlParser(base, symbol, startDate, endDate).parse(inputStream)
                }
            }
        )
    }

}
