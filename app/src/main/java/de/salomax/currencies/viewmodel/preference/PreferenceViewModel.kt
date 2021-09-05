package de.salomax.currencies.viewmodel.preference

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import de.salomax.currencies.repository.Database
import de.salomax.currencies.repository.ExchangeRatesRepository

class PreferenceViewModel(application: Application) : AndroidViewModel(application) {

    fun setApiProvider(api: Int) {
        // first put provider to db...
        Database(getApplication()).setApiProvider(api)
        // ...after that, fetch the new exchange rates
        ExchangeRatesRepository(getApplication()).getExchangeRates()
    }

    fun getApiProvider(): LiveData<Int> {
        return Database(getApplication()).getApiProviderAsync()
    }

    fun setTheme(theme: Int) {
        Database(getApplication()).setTheme(theme)
        // switch theme
        AppCompatDelegate.setDefaultNightMode(
            when (theme) {
                0 -> AppCompatDelegate.MODE_NIGHT_NO
                1 -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }

    fun setFee(fee: Float) {
        Database(getApplication()).setFee(fee)
    }

    fun getFee(): LiveData<Float> {
        return Database(getApplication()).getFee()
    }

    fun setFeeEnabled(enabled: Boolean) {
        Database(getApplication()).setFeeEnabled(enabled)
    }

}
