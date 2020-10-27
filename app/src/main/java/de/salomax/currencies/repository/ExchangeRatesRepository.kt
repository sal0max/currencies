package de.salomax.currencies.repository

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.kittinunf.fuel.core.isSuccessful
import de.salomax.currencies.model.ExchangeRates

class ExchangeRatesRepository(private val context: Context) {

    companion object {
        private var instance: ExchangeRatesRepository? = null

        fun getInstance(application: Application): ExchangeRatesRepository {
            if (instance == null) {
                synchronized(ExchangeRatesRepository::class) {
                    instance = ExchangeRatesRepository(application)
                }
            }
            return instance!!
        }
    }

    private val liveExchangeRates = Database.getInstance(context).getExchangeRates()
    private var liveError = MutableLiveData<String?>()

    // call api
    fun getExchangeRates(): LiveData<ExchangeRates?> {
        ExchangeRatesService.getRates { response, r ->
            // success: update /store rates to preferences
            if (response.isSuccessful)
                r.component1()?.let {
                    Database.getInstance(context).insertExchangeRates(it)
                }
            // error
            else {
                r.component2()?.let {
                    liveError.postValue(it.message)
                }
            }
        }

        return liveExchangeRates
    }

    fun getError(): LiveData<String?> {
        return liveError
    }

}
