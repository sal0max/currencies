package de.salomax.currencies.repository

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import de.salomax.currencies.model.ExchangeRates
import de.salomax.currencies.util.*

import java.time.LocalDate

class Database(context: Context) {

    /*
     * current exchange rates from api =============================================================
     */
    private val prefsRates: SharedPreferences = context.getSharedPreferences("rates", MODE_PRIVATE)

    private val keyDate = "_date"
    private val keyBaseRate = "_base"

    fun insertExchangeRates(items: ExchangeRates) {
        // don't insert null-values. this would clear the cache
        if (items.date != null)
            prefsRates.apply {
                val editor = edit()
                // clear old values
                editor.clear()
                // apply new ones
                editor.putString(keyDate, items.date.toString())
                editor.putString(keyBaseRate, items.base)
                items.rates?.forEach { rate ->
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
        return prefsRates.getString(keyDate, null)?.let { LocalDate.parse(it) }
    }

    /*
     * last state ==================================================================================
     */
    private val prefsLastState: SharedPreferences = context.getSharedPreferences("last_state", MODE_PRIVATE)

    private val keyLastStateFrom = "_last_from"
    private val keyLastStateTo = "_last_to"
    private val keyIsUpdating = "_isUpdating"

    fun saveLastUsedRates(from: String?, to: String?) {
        prefsLastState.apply {
            edit().putString(keyLastStateFrom, from).apply()
            edit().putString(keyLastStateTo, to).apply()
        }
    }

    fun getLastRateFrom(): String? {
        return prefsLastState.getString(keyLastStateFrom, "USD")
    }

    fun getLastRateTo(): String? {
        return prefsLastState.getString(keyLastStateTo, "EUR")
    }

    fun setUpdating(updating: Boolean) {
        prefsLastState.edit().putBoolean(keyIsUpdating, updating).apply()
    }

    fun isUpdating(): SharedPreferenceBooleanLiveData {
        return SharedPreferenceBooleanLiveData(prefsLastState, keyIsUpdating, false)
    }

    /*
     * starred currencies ==========================================================================
     */
    private val prefsStarredCurrencies: SharedPreferences = context.getSharedPreferences("starred_currencies", MODE_PRIVATE)

    private val keyStars = "_stars"
    private val keyStarredEnabled = "_starredActive"

    fun toggleCurrencyStar(currencyCode: String) {
        prefsStarredCurrencies.apply {
            if (prefsStarredCurrencies.getStringSet(keyStars, HashSet<String>())!!.contains(currencyCode))
                removeCurrencyStar(currencyCode)
            else
                starCurrency(currencyCode)
        }
    }

    fun getStarredCurrencies(): SharedPreferenceLiveData<Set<String>> {
        return SharedPreferenceStringSetLiveData(prefsStarredCurrencies, keyStars, HashSet())
    }

    private fun starCurrency(currencyCode: String) {
        prefsStarredCurrencies.apply {
            edit().putStringSet(keyStars,
                prefsStarredCurrencies.getStringSet(keyStars, HashSet<String>())!!
                    .plus(currencyCode)
            ).apply()
        }
    }

    private fun removeCurrencyStar(currencyCode: String) {
        prefsStarredCurrencies.apply {
            edit().putStringSet(keyStars,
                prefsStarredCurrencies.getStringSet(keyStars, HashSet<String>())!!
                    .minus(currencyCode)
            ).apply()
        }
    }

    fun isFilterStarredEnabled(): SharedPreferenceBooleanLiveData {
        return SharedPreferenceBooleanLiveData(prefsStarredCurrencies, keyStarredEnabled, false)
    }

    fun toggleStarredActive() {
        prefsStarredCurrencies.apply {
            edit().putBoolean(keyStarredEnabled,
                prefsStarredCurrencies.getBoolean(keyStarredEnabled, false).not()
            ).apply()
        }
    }

    /*
     * preferences =================================================================================
     */
    private val prefs: SharedPreferences = context.getSharedPreferences("prefs", MODE_PRIVATE)

    private val keyApi = "_api"
    private val keyTheme = "_theme"
    private val keyPureBlackEnabled = "_pureBlackEnabled"
    private val keyFeeEnabled = "_feeEnabled"
    private val keyFeeValue = "_fee"

    /* api */

    fun setApiProvider(api: Int) {
        prefs.apply {
            edit().putInt(keyApi, api).apply()
        }
    }

    fun getApiProvider(): Int {
        return prefs.getInt(keyApi, 0)
    }

    fun getApiProviderAsync(): LiveData<Int> {
        return SharedPreferenceIntLiveData(prefs, keyApi, 0)
    }

    /* theme */

    fun setTheme(theme: Int) {
        prefs.apply {
            edit().putInt(keyTheme, theme).apply()
        }
    }

    /**
     * 0 = MODE_NIGHT_NO
     * 1 = MODE_NIGHT_YES
     * 2 = MODE_NIGHT_FOLLOW_SYSTEM
     */
    fun getTheme(): Int {
        return prefs.getInt("_theme", 2)
    }

    fun setPureBlackEnabled(enabled: Boolean) {
        prefs.apply {
            edit().putBoolean(keyPureBlackEnabled, enabled).apply()
        }
    }

    fun isPureBlackEnabled(): Boolean {
        return prefs.getBoolean(keyPureBlackEnabled, false)
    }

    /* fee */

    fun setFeeEnabled(enabled: Boolean) {
        prefs.apply {
            edit().putBoolean(keyFeeEnabled, enabled).apply()
        }
    }

    fun isFeeEnabled(): LiveData<Boolean> {
        return SharedPreferenceBooleanLiveData(prefs, keyFeeEnabled, false)
    }

    fun setFee(fee: Float) {
        prefs.apply {
            edit().putFloat(keyFeeValue, fee).apply()
        }
    }

    fun getFee(): LiveData<Float> {
        return SharedPreferenceFloatLiveData(prefs, keyFeeValue, 2.2f)
    }

}
