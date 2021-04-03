package de.salomax.currencies.viewmodel.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import de.salomax.currencies.model.ExchangeRates
import de.salomax.currencies.model.Rate
import de.salomax.currencies.repository.Database
import de.salomax.currencies.repository.ExchangeRatesRepository
import java.time.*

class ExchangeRatesViewModel(application: Application) : AndroidViewModel(application) {

    private var repository: ExchangeRatesRepository = ExchangeRatesRepository(application)

    private var dbLiveItems: LiveData<ExchangeRates?>
    private val liveError = repository.getError()

    /*
     * items =======================================================================================
     */

    init {
        // only update if data is old: https://github.com/Formicka/exchangerate.host
        // "Rates are updated around midnight UTC every working day."
        val currentTime = LocalDateTime.now(ZoneId.of("UTC"))
        val cachedDate = Database.getInstance(application).getDate()
        dbLiveItems =
            when {
                // first run: fetch data
                cachedDate == null -> repository.getExchangeRates()
                // also fetch if stored date is before the current date
                cachedDate
                    .plusDays(1)
                    .atStartOfDay()
                    .plusHours(1) // add 1 hour to be sure: "…AROUND midnight…"
                    .isBefore(currentTime) -> repository.getExchangeRates()
                // else just use the cached value
                else -> Database.getInstance(application).getExchangeRates()
            }
    }

    fun getExchangeRate(): LiveData<ExchangeRates?> {
        val liveItems = MediatorLiveData<ExchangeRates?>()
        liveItems.addSource(dbLiveItems) { exchangeRates ->
            exchangeRates?.let {
                liveItems.value = ExchangeRates(
                    exchangeRates.success,
                    exchangeRates.error,
                    exchangeRates.base,
                    exchangeRates.date,
                    exchangeRates.rates?.toMutableList()
                        // add Faroese króna (same as Danish krone) if it isn't already there - I simply like it!
                        .apply {
                            if (it.rates?.find { it.code == "FOK" } == null)
                                it.rates?.find { it.code == "DKK" }?.value?.let { dkk ->
                                    this?.add(Rate("FOK", dkk))
                                }
                        }
                        ?.asSequence()
                        // clean up the list
                        ?.filterNot {
                            it.code == "XDR" // special drawing rights
                                    || it.code == "XAG" // silver
                                    || it.code == "XAU" // gold
                                    || it.code == "XPD" // palladium
                                    || it.code == "XPT" // platinum
                                    || it.code == "MRO" // Mauritanian ouguiya (pre-2018)
                                    || it.code == "STD" // São Tomé and Príncipe dobra (pre-2018)
                                    || it.code == "VEF" // Venezuelan bolívar fuerte (old)
                                    || it.code == "CNH" // Chinese renminbi (Offshore)
                                    || it.code == "CUP" // Cuban peso (moneda nacional)
                        }
                        // sort by code
                        ?.sortedBy { rate -> rate.code }
                        ?.toList()
                )
            }
        }
        return liveItems
    }

    /*
     * error =======================================================================================
     */

    fun getError(): LiveData<String?> {
        return liveError
    }

}
