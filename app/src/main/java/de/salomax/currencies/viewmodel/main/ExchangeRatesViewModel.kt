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
    private var starredLiveItems: LiveData<Set<String>>
    private var onlyShowStarred: LiveData<Boolean>
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
        starredLiveItems = Database(getApplication()).getStarredCurrencies()
        onlyShowStarred = Database(getApplication()).isFilterStarredEnabled()
    }

    internal val exchangeRates: LiveData<ExchangeRates?> = ExchangeRatesLiveDate()

    internal inner class ExchangeRatesLiveDate: MediatorLiveData<ExchangeRates?>() {
        init {
            addSource(dbLiveItems) { calc() }
            addSource(starredLiveItems) { calc() }
            addSource(onlyShowStarred) { calc() }
        }

        private fun calc() {
            dbLiveItems.value?.let { rates ->
                this.value = rates
                    // usa a copy with ...
                    .copy(
                        rates = rates.rates
                            // ... the correct sort order of the rates
                            ?.sortedWith(
                                @Suppress("MoveLambdaOutsideParentheses")
                                compareBy(
                                    // { rate -> starredLiveItems.value?.contains(rate.code) == false}, // starred
                                    { rate -> rate.getName(getApplication()) } // name
                                )
                            )
                    )
            }
        }
    }

    fun forceUpdateExchangeRate() {
        if (isUpdating.value != true)
            dbLiveItems = repository.getExchangeRates()
    }

    fun getStarredCurrencies(): LiveData<Set<String>> {
        return starredLiveItems
    }

    fun isFilterStarredEnabled(): LiveData<Boolean> {
        return onlyShowStarred
    }

    fun toggleStarredActive() {
        Database(getApplication()).toggleStarredActive()
    }

    fun toggleCurrencyStar(currencyCode: String) {
        Database(getApplication()).toggleCurrencyStar(currencyCode)
    }

    /*
     * error =======================================================================================
     */

    fun getError(): LiveData<String?> = liveError

    fun isUpdating(): LiveData<Boolean> = isUpdating

}
