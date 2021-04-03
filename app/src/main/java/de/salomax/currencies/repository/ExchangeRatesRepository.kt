package de.salomax.currencies.repository

import android.content.Context
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
        isUpdating.postValue(true)

        ExchangeRatesService.getRates { response, r ->
            // received some json
            if (response.isSuccessful && r.component1() != null ) {
                isUpdating.postValue(false)

                // SUCCESS! update /store rates to preferences
                if (r.component1()!!.success == null || r.component1()!!.success == true) {
                    Database.getInstance(context).insertExchangeRates(r.component1()!!)
                }
                // ERROR! got response from API, but just an error message
                else {
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
