package de.salomax.currencies.viewmodel.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import de.salomax.currencies.model.ExchangeRates
import de.salomax.currencies.repository.Database
import de.salomax.currencies.repository.ExchangeRatesRepository
import java.time.*

class ExchangeRatesViewModel(application: Application) : AndroidViewModel(application) {

    private var repository: ExchangeRatesRepository = ExchangeRatesRepository.getInstance(application)

    private var dbLiveItems: LiveData<ExchangeRates?>
    private val liveError = repository.getError()

    /*
     * items =======================================================================================
     */

    init {
        // only update if data is old: https://www.ecb.europa.eu/stats/policy_and_exchange_rates/euro_reference_exchange_rates/html/index.en.html
        // "The reference rates are usually updated around 16:00 CET on every working day, except on
        // TARGET closing days. They are based on a regular daily concertation procedure between
        // central banks across Europe, which normally takes place at 14:15 CET."
        val currentTime = LocalDateTime.now(ZoneId.of("CET"))
        val cachedDate = Database.getInstance(application).getDate()
        dbLiveItems =
            when {
                // first run: fetch data
                cachedDate == null -> repository.getExchangeRates()
                // also fetch if stored date is before the current date (only on weekdays)...
                cachedDate
                    .plusDays(1)
                    .atTime(15, 59)
                    .isBefore(currentTime) -> repository.getExchangeRates()
                // else just use the cached value
                else -> Database.getInstance(application).getExchangeRates()
            }
    }

    fun getExchangeRate(): LiveData<ExchangeRates?> {
        return dbLiveItems
    }

    /*
     * error =======================================================================================
     */

    fun getError(): LiveData<String?> {
        return liveError
    }

}
