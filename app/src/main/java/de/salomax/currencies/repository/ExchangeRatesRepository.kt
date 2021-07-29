package de.salomax.currencies.repository

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.kittinunf.fuel.core.isSuccessful
import de.salomax.currencies.R
import de.salomax.currencies.model.ExchangeRates

class ExchangeRatesRepository(private val context: Context) {

    private val liveExchangeRates = Database.getInstance(context).getExchangeRates()
    private var liveError = MutableLiveData<String?>()
    private var isUpdating = MutableLiveData(false)

    // call api
    fun getExchangeRates(): LiveData<ExchangeRates?> {
        val start = System.currentTimeMillis()
        isUpdating.postValue(true)

        ExchangeRatesService.getRates(
            // use the right api
            when (Database.getInstance(context).getApiProvider()) {
                // fer.ee
                2 -> ExchangeRatesService.Endpoint.FER_EE
                // frankfurter.app
                1 -> ExchangeRatesService.Endpoint.FRANKFURTER_APP
                // exchangerate.host (== 0)
                else -> ExchangeRatesService.Endpoint.EXCHANGERATE_HOST
            }
        ) { response, r ->
            // received some json
            if (response.isSuccessful && r.component1() != null) {
                // SUCCESS! update /store rates to preferences
                if (r.component1()!!.success == null || r.component1()!!.success == true) {
                    // "update" for at least 2s
                    val now = System.currentTimeMillis()
                    if (now - start < 2000)
                        Handler(Looper.getMainLooper()).postDelayed({
                            isUpdating.postValue(false)
                        }, 2000 - (now - start))
                    else
                        isUpdating.postValue(false)
                    // update db
                    Database.getInstance(context).insertExchangeRates(r.component1()!!)
                }
                // ERROR! got response from API, but just an error message
                else {
                    isUpdating.postValue(false)
                    val message = r.component1()!!.error
                    liveError.postValue(
                        if (message != null)
                            context.getString(R.string.error, message)
                        else
                            context.getString(R.string.error_api_error)
                    )
                }
            }
            // generic network error
            else {
                isUpdating.postValue(false)

                r.component2()?.let {
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

        return liveExchangeRates
    }

    fun getError(): LiveData<String?> {
        return liveError
    }

    fun isUpdating(): LiveData<Boolean> {
        return isUpdating
    }

}
