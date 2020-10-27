package de.salomax.currencies.repository

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import de.salomax.currencies.model.ExchangeRates
import de.salomax.currencies.util.SharedPreferenceExchangeRatesLiveData

import java.time.LocalDate

class Database(context: Context) {

    companion object {
        private var instance: Database? = null

        fun getInstance(context: Context): Database {
            if (instance == null) {
                synchronized(Database::class) {
                    instance = Database(context)
                }
            }
            return instance!!
        }
    }

    /*
     * data from api ===============================================================================
     */
    private val prefsRates: SharedPreferences = context.getSharedPreferences("rates", MODE_PRIVATE)

    fun insertExchangeRates(items: ExchangeRates) {
        prefsRates.apply {
            edit().putString("_date", items.date.toString()).apply()
            edit().putString("_base", items.base).apply()
            for (rate in items.rates) {
                edit().putFloat(rate.name, rate.value).apply()
            }
        }
    }

    fun getExchangeRates(): LiveData<ExchangeRates?> {
        return SharedPreferenceExchangeRatesLiveData(prefsRates)
    }

    fun getDate(): LocalDate? {
        return prefsRates.getString("_date", null)?.let { LocalDate.parse(it) }
    }

    /*
     * last state ==================================================================================
     */
    private val prefsLastState: SharedPreferences = context.getSharedPreferences("last_state", MODE_PRIVATE)

    fun saveLastUsedRates(from: String?, to: String?) {
        prefsLastState.apply {
            edit().putString("_last_from", from).apply()
            edit().putString("_last_to", to).apply()
        }
    }

    fun getLastRateFrom(): String? {
        return prefsLastState.getString("_last_from", "USD")
    }

    fun getLastRateTo(): String? {
        return prefsLastState.getString("_last_to", "EUR")
    }

    /*
     * preferences =================================================================================
     */
    private val prefs: SharedPreferences = context.getSharedPreferences("prefs", MODE_PRIVATE)

    fun setTheme(theme: Int) {
        prefs.apply {
            edit().putInt("_theme", theme).apply()
        }
    }

    fun getTheme(): Int {
        return prefs.getInt("_theme", 2)
    }

}
