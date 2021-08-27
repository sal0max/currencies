package de.salomax.currencies.repository

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.salomax.currencies.R
import de.salomax.currencies.model.ExchangeRates
import de.salomax.currencies.model.Timeline
import kotlinx.coroutines.*
import java.time.LocalDate

class ExchangeRatesRepository(private val context: Context) {

    private val liveExchangeRates = Database.getInstance(context).getExchangeRates()
    private val liveTimeline = MutableLiveData<Timeline?>()
    private var liveError = MutableLiveData<String?>()
    private var isUpdating = MutableLiveData(false)

    // TODO: lots of duplicated code in the following 2 functions - REFACTOR!

    /**
     * Gets and returns all latest exchange rates from the API.
     */
    fun getExchangeRates(): LiveData<ExchangeRates?> {
        val start = System.currentTimeMillis()
        isUpdating.postValue(true)

        // run in background
        CoroutineScope(Dispatchers.IO).launch {
            ExchangeRatesService.getRates(
                // use the right api
                when (Database.getInstance(context).getApiProvider()) {
                    1 -> ExchangeRatesService.Endpoint.FRANKFURTER_APP
                    else -> ExchangeRatesService.Endpoint.EXCHANGERATE_HOST
                }
            ).run  {
                val rates = component1()
                val fuelError = component2()
                // received some json
                if (rates != null && fuelError == null) {
                    // SUCCESS! update /store rates to preferences
                    if (rates.success == null || rates.success == true) {
                        // "update" for at least 2s
                        postIsUpdating(start)
                        // update db
                        Database.getInstance(context).insertExchangeRates(rates)
                    }
                    // ERROR! got response from API, but just an error message
                    else {
                        postError(rates.error)
                    }
                }
                // generic network error
                else {
                    isUpdating.postValue(false)

                    fuelError?.let {
                        liveError.postValue(
                            when (it.response.statusCode) {
                                // no connection
                                -1 -> context.getString(R.string.error_no_data)
                                // everything else
                                else -> context.getString(R.string.error, it.message)
                            }
                        )
                    }
                }
            }
        }

        return liveExchangeRates
    }

    /**
     * Gets and returns the timeline of the last year of the two given currencies
     */
    fun getTimeline(base: String, symbol: String): LiveData<Timeline?> {
        val start = System.currentTimeMillis()
        isUpdating.postValue(true)

        // run in background
        CoroutineScope(Dispatchers.IO).launch {
            ExchangeRatesService.getTimeline(
                // use the right api
                endpoint = when (Database.getInstance(context).getApiProvider()) {
                    1 -> ExchangeRatesService.Endpoint.FRANKFURTER_APP
                    else -> ExchangeRatesService.Endpoint.EXCHANGERATE_HOST
                },
                startDate = LocalDate.now().minusYears(1),
                endDate = LocalDate.now(),
                base = base,
                symbol = symbol
            ).run {
                val timeline = component1()
                val fuelError = component2()
                // received some json
                if (timeline != null && fuelError == null) {
                    // SUCCESS! update /store rates to preferences
                    if (timeline.success == null || timeline.success == true) {
                        // "update" for at least 2s
                        postIsUpdating(start)
                        // remove dates, where no rates (= only base rates) are provided
                        val copy = timeline.copy(rates = timeline.rates
                            ?.filter { entry -> entry.value.size >= 2 }
                        )
                        // TODO cache in db
                        liveTimeline.postValue(copy)
                    }
                    // ERROR! got response from API, but just an error message
                    else {
                        postError(timeline.error)
                    }
                }
                // generic network error
                else {
                    isUpdating.postValue(false)

                    fuelError?.let {
                        liveError.postValue(
                            when (it.response.statusCode) {
                                // no connection
                                -1 -> context.getString(R.string.error_no_data)
                                // everything else
                                else -> context.getString(R.string.error, it.message)
                            }
                        )
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

    private fun postIsUpdating(start: Long) {
        // "update" for at least 2s
        val now = System.currentTimeMillis()
        if (now - start < 2000)
            Handler(Looper.getMainLooper()).postDelayed({
                isUpdating.postValue(false)
            }, 2000 - (now - start))
        else
            isUpdating.postValue(false)
    }

    private fun postError(message: String?) {
        isUpdating.postValue(false)
        liveError.postValue(
            if (message != null)
                context.getString(R.string.error, message)
            else
                context.getString(R.string.error_api_error)
        )
    }

}
