package de.salomax.currencies.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.salomax.currencies.R
import de.salomax.currencies.model.ExchangeRates
import de.salomax.currencies.model.Timeline
import kotlinx.coroutines.*

class ExchangeRatesRepository(private val context: Context) {

    private val liveExchangeRates = Database(context).getExchangeRates()
    private val liveTimeline = MutableLiveData<Timeline?>()
    private var liveError = MutableLiveData<String?>()
    private var isUpdating = Database(context).isUpdating()

    /**
     * Gets and returns all latest exchange rates from the API.
     */
    fun getExchangeRates(): LiveData<ExchangeRates?> {
        val start = System.currentTimeMillis()
        Database(context).setUpdating(true)

        // run in background
        CoroutineScope(Dispatchers.IO).launch {
            // call api
            ExchangeRatesService.getRates(
                // use the right api
                when (Database(context).getApiProvider()) {
                    0 -> ExchangeRatesService.ApiProvider.EXCHANGERATE_HOST
                    1 -> ExchangeRatesService.ApiProvider.FRANKFURTER_APP
                    2 -> ExchangeRatesService.ApiProvider.FER_EE
                    else -> throw UnsupportedOperationException()
                }
            ).run  {
                val rates = component1()
                val fuelError = component2()
                // received some json
                if (rates != null && fuelError == null) {
                    // SUCCESS! update /store rates to preferences
                    if (rates.success == null || rates.success == true) {
                        postIsUpdating(start)
                        Database(context).insertExchangeRates(rates)
                    }
                    // ERROR! got response from API, but just an error message
                    else {
                        postError(rates.error)
                    }
                }
                // generic network error
                else {
                    when (fuelError?.response?.statusCode) {
                        -1 -> postError(context.getString(R.string.error_no_data))
                        else -> postError(fuelError?.message)
                    }
                }
            }
        }

        return liveExchangeRates
    }

    /**
     * Gets and returns the timeline of the last year of the given base and target currency
     */
    fun getTimeline(base: String, symbol: String): LiveData<Timeline?> {
        val start = System.currentTimeMillis()
        Database(context).setUpdating(true)

        // run in background
        CoroutineScope(Dispatchers.IO).launch {
            // call api
            ExchangeRatesService.getTimeline(
                // use the right api
                apiProvider = when (Database(context).getApiProvider()) {
                    0 -> ExchangeRatesService.ApiProvider.EXCHANGERATE_HOST
                    1 -> ExchangeRatesService.ApiProvider.FRANKFURTER_APP
                    2 -> ExchangeRatesService.ApiProvider.FER_EE
                    else -> throw UnsupportedOperationException()
                },
                base = base,
                symbol = symbol
            ).run {
                val timeline = component1()
                val fuelError = component2()
                // received some json
                if (timeline != null && fuelError == null) {
                    // SUCCESS! update /store rates to preferences
                    if (timeline.success == null || timeline.success == true) {
                        postIsUpdating(start)
                        CoroutineScope(Dispatchers.Main).launch {
                            liveTimeline.setValue(timeline)
                        }
                    }
                    // ERROR! got response from API, but just an error message
                    else {
                        postError(timeline.error)
                    }
                }
                // generic network error
                else {
                    when (fuelError?.response?.statusCode) {
                        -1 -> postError(context.getString(R.string.error_no_data))
                        else -> postError(fuelError?.message)
                    }
                }
            }
        }

        return liveTimeline
    }

    fun getError(): LiveData<String?> {
        return liveError
    }

    fun isUpdating(): LiveData<Boolean> {
        return isUpdating
    }

    /*
     * "update" for at least 500ms
     */
    private suspend fun postIsUpdating(start: Long) {
        val now = System.currentTimeMillis()
        if (now - start < 500) {
            Database(context).setUpdating(true)

            withContext(Dispatchers.Main) {
                launch {
                    delay(500 - (now - start))
                    Database(context).setUpdating(false)
                }
            }
        } else
            Database(context).setUpdating(false)
    }

    private fun postError(message: String?) {
        Database(context).setUpdating(false)
        liveError.postValue(
            if (message != null)
                context.getString(R.string.error, message)
            else
                context.getString(R.string.error_api_error)
        )
    }

}
