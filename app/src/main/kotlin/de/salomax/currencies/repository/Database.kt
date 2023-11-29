package de.salomax.currencies.repository

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import de.salomax.currencies.model.ApiProvider
import de.salomax.currencies.model.Currency
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
    private val keyProvider = "_provider"

    fun insertExchangeRates(items: ExchangeRates) {
        // don't insert null-values. this would clear the cache
        if (items.date != null)
            prefsRates.apply {
                val editor = edit()
                // clear old values
                editor.clear()
                // apply new ones
                editor.putString(keyDate, items.date.toString())
                editor.putString(keyBaseRate, items.base?.iso4217Alpha())
                editor.putInt(keyProvider, items.provider?.id ?: -1)
                items.rates?.forEach { rate ->
                    editor.putFloat(rate.currency.iso4217Alpha(), rate.value)
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
    private val keyHistoricalDate = "_historical_date"

    fun saveLastUsedRates(from: Currency?, to: Currency?) {
        prefsLastState.apply {
            from?.let { edit().putString(keyLastStateFrom, it.iso4217Alpha()).apply() }
            to?.let { edit().putString(keyLastStateTo, it.iso4217Alpha()).apply() }
        }
    }

    fun getLastBaseCurrency(): LiveData<Currency?> {
        return SharedPreferenceStringLiveData(prefsLastState, keyLastStateFrom, "USD")
            .map { Currency.fromString(it!!) }
    }

    fun getLastDestinationCurrency(): LiveData<Currency?> {
        return SharedPreferenceStringLiveData(prefsLastState, keyLastStateTo, "EUR")
            .map { Currency.fromString(it!!) }
    }

    fun setUpdating(updating: Boolean) {
        prefsLastState.edit().putBoolean(keyIsUpdating, updating).apply()
    }

    fun isUpdating(): SharedPreferenceBooleanLiveData {
        return SharedPreferenceBooleanLiveData(prefsLastState, keyIsUpdating, false)
    }

    fun setHistoricalDate(date: LocalDate?) {
        prefsLastState.edit().putLong(keyHistoricalDate, date?.toMillis() ?: -1).apply()
    }

    fun getHistoricalLiveDate(): LiveData<LocalDate?> {
        return SharedPreferenceLongLiveData(prefsLastState, keyHistoricalDate, -1).map {
            if (it == -1L) null
            else it.toLocalDate()
        }
    }

    fun getHistoricalDate(): LocalDate? {
        return when (val date = prefsLastState.getLong(keyHistoricalDate, -1)) {
            -1L -> null
            else -> date.toLocalDate()
        }
    }

    /*
     * starred currencies ==========================================================================
     */
    private val prefsStarredCurrencies: SharedPreferences = context.getSharedPreferences("starred_currencies", MODE_PRIVATE)

    private val keyStars = "_stars"
    private val keyStarredEnabled = "_starredActive"

    fun toggleCurrencyStar(currency: Currency) {
        prefsStarredCurrencies.apply {
            if (prefsStarredCurrencies.getStringSet(keyStars, HashSet<String>())!!.contains(currency.iso4217Alpha()))
                removeCurrencyStar(currency)
            else
                starCurrency(currency)
        }
    }

    fun getStarredCurrencies(): LiveData<Set<Currency>> {
        return SharedPreferenceStringSetLiveData(prefsStarredCurrencies, keyStars, HashSet())
            .map { set ->
                set.mapNotNull { code ->
                    Currency.fromString(code)
                }.toSet()
            }
    }

    private fun starCurrency(currency: Currency) {
        prefsStarredCurrencies.apply {
            edit().putStringSet(keyStars,
                prefsStarredCurrencies.getStringSet(keyStars, HashSet<String>())!!
                    .plus(currency.iso4217Alpha())
            ).apply()
        }
    }

    private fun removeCurrencyStar(currency: Currency) {
        prefsStarredCurrencies.apply {
            edit().putStringSet(keyStars,
                prefsStarredCurrencies.getStringSet(keyStars, HashSet<String>())!!
                    .minus(currency.iso4217Alpha())
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
    private val keyPreviewConversionEnabled = "_previewConversionEnabled"
    private val keyExtendedKeypadEnabled = "_extendedKeypadEnabled"

    /* api */

    fun setApiProvider(api: ApiProvider) {
        prefs.apply {
            edit().putInt(keyApi, api.id).apply()
        }
    }

    fun getApiProvider(): ApiProvider {
        return ApiProvider.fromId(prefs.getInt(keyApi, -1))
    }

    fun getApiProviderAsync(): LiveData<ApiProvider> {
        return SharedPreferenceIntLiveData(prefs, keyApi, -1).map {
            ApiProvider.fromId(it)
        }
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

    /* preview conversion */

    fun setPreviewConversionEnabled(enabled: Boolean) {
        prefs.apply {
            edit().putBoolean(keyPreviewConversionEnabled, enabled).apply()
        }
    }

    fun isPreviewConversionEnabled(): LiveData<Boolean> {
        return SharedPreferenceBooleanLiveData(prefs, keyPreviewConversionEnabled, false)
    }

    /* extended keypad */

    fun setExtendedKeypadEnabled(enabled: Boolean) {
        prefs.apply {
            edit().putBoolean(keyExtendedKeypadEnabled, enabled).apply()
        }
    }

    fun isExtendedKeypadEnabled(): LiveData<Boolean> {
        return SharedPreferenceBooleanLiveData(prefs, keyExtendedKeypadEnabled, false)
    }

}
