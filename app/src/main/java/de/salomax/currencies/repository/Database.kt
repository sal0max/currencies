package de.salomax.currencies.repository

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import de.salomax.currencies.model.ExchangeRates
import de.salomax.currencies.util.SharedPreferenceBooleanLiveData
import de.salomax.currencies.util.SharedPreferenceExchangeRatesLiveData
import de.salomax.currencies.util.SharedPreferenceFloatLiveData

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
            val editor = edit()
            // clear old values
            editor.clear()
            // apply new ones
            editor.putString("_date", items.date.toString())
            editor.putString("_base", items.base)
            for (rate in items.rates) {
                editor.putFloat(rate.code, rate.value)
            }
            // persist
            editor.apply()
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

    /* theme */

    fun setTheme(theme: Int) {
        prefs.apply {
            edit().putInt("_theme", theme).apply()
        }
    }

    fun getTheme(): Int {
        return prefs.getInt("_theme", 2)
    }

    /* fee */

    fun setFeeEnabled(enabled: Boolean) {
        prefs.apply {
            edit().putBoolean("_feeEnabled", enabled).apply()
        }
    }

    fun isFeeEnabled(): LiveData<Boolean> {
        return SharedPreferenceBooleanLiveData(prefs, "_feeEnabled", false)
    }

    fun setFee(fee: Float) {
        prefs.apply {
            edit().putFloat("_fee", fee).apply()
        }
    }

    fun getFee(): LiveData<Float> {
        return SharedPreferenceFloatLiveData(prefs, "_fee", 2.2f)
    }

}
