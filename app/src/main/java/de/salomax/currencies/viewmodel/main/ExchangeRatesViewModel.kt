package de.salomax.currencies.viewmodel.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import de.salomax.currencies.model.ExchangeRates
import de.salomax.currencies.repository.Database
import de.salomax.currencies.repository.ExchangeRatesRepository
import java.time.*

class ExchangeRatesViewModel(application: Application) : AndroidViewModel(application) {

    private var repository: ExchangeRatesRepository = ExchangeRatesRepository(application)

    private var dbLiveItems: LiveData<ExchangeRates?>
    private val liveError = repository.getError()

    private var isUpdating: LiveData<Boolean> = repository.isUpdating()

    /*
     * items =======================================================================================
     */

    init {
        // only update if data is old: https://github.com/Formicka/exchangerate.host
        // "Rates are updated around midnight UTC every working day."
        val currentDate = LocalDate.now(ZoneId.of("UTC"))
        val cachedDate = Database(application).getDate()
        dbLiveItems =
            when {
                // first run: fetch data
                cachedDate == null -> repository.getExchangeRates()
                // also fetch if stored date is before the current date
                cachedDate.isBefore(currentDate) -> repository.getExchangeRates()
                // else just use the cached value
                else -> Database(application).getExchangeRates()
            }
    }

    fun getExchangeRate(): LiveData<ExchangeRates?> {
        val liveItems = MediatorLiveData<ExchangeRates?>()
        liveItems.addSource(dbLiveItems) { exchangeRates ->
            exchangeRates?.let {
                liveItems.value = exchangeRates
            }
        }
        return liveItems
    }

    fun forceUpdateExchangeRate() {
        if (isUpdating.value != true)
            dbLiveItems = repository.getExchangeRates()
    }

    /*
     * error =======================================================================================
     */

    fun getError(): LiveData<String?> = liveError

    fun isUpdating(): LiveData<Boolean> = isUpdating

}
