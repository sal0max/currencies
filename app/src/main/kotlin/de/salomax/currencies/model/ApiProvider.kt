package de.salomax.currencies.model

import android.content.Context
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.result.Result
import com.squareup.moshi.JsonClass
import de.salomax.currencies.model.provider.BankOfCanada
import de.salomax.currencies.model.provider.BankRossii
import de.salomax.currencies.model.provider.FerEe
import de.salomax.currencies.model.provider.FrankfurterApp
import de.salomax.currencies.model.provider.InforEuro
import de.salomax.currencies.model.provider.NorgesBank
import java.time.LocalDate

@JsonClass(generateAdapter = false) // see https://stackoverflow.com/a/64085370/421140
enum class ApiProvider(
    val id: Int, // safer ordinal; DON'T CHANGE!
    private val implementation: Api,
) {
    // EXCHANGERATE_HOST(0, "https://api.exchangerate.host"), // removed, as API was shut down
    FRANKFURTER_APP(1, FrankfurterApp()), // default
    FER_EE(2, FerEe()),
    INFOR_EURO(3, InforEuro()),
    NORGES_BANK(4, NorgesBank()),
    BANK_ROSSII(5, BankRossii()),
    BANK_OF_CANADA(6, BankOfCanada());

    companion object {
        fun fromId(value: Int): ApiProvider = values().firstOrNull { it.id == value }
            // this is our fallback, e.g. if an API is removed from the app
            ?: BANK_ROSSII
    }

    fun getName(): CharSequence {
        return this.implementation.name
    }

    fun getDescription(context: Context): CharSequence {
        return this.implementation.description(context)
    }

    fun getUpdateIntervalDescription(context: Context): CharSequence {
        return this.implementation.updateIntervalDescription(context)
    }

    suspend fun getRates(date: LocalDate?): Result<ExchangeRates, FuelError> {
        return this.implementation.getRates(date)
    }

    suspend fun getTimeline(
        base: Currency,
        symbol: Currency,
        startDate: LocalDate,
        endDate: LocalDate
    ): Result<Timeline, FuelError> {
        return this.implementation.getTimeline(base, symbol, startDate, endDate)
    }

    abstract class Api {
        abstract val name: String
        abstract fun description(context: Context): CharSequence
        abstract fun updateIntervalDescription(context: Context): CharSequence

        abstract val baseUrl: String
        abstract suspend fun getRates(date: LocalDate?): Result<ExchangeRates, FuelError>
        abstract suspend fun getTimeline(
            base: Currency,
            symbol: Currency,
            startDate: LocalDate,
            endDate: LocalDate
        ): Result<Timeline, FuelError>
    }

}
