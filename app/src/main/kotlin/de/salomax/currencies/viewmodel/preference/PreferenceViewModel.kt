package de.salomax.currencies.viewmodel.preference

import android.app.Application
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.TaskStackBuilder
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import de.salomax.currencies.R
import de.salomax.currencies.model.ApiProvider
import de.salomax.currencies.repository.Database
import de.salomax.currencies.repository.ExchangeRatesRepository
import de.salomax.currencies.view.main.MainActivity
import de.salomax.currencies.view.preference.PreferenceActivity

class PreferenceViewModel(private val app: Application) : AndroidViewModel(app) {

    private var apiProvider: LiveData<ApiProvider> = Database(app).getApiProviderAsync()
    private var isPreviewConversionEnabled: LiveData<Boolean> = Database(app).isPreviewConversionEnabled()

    fun setApiProvider(api: ApiProvider) {
        // first put provider to db...
        Database(app).setApiProvider(api)
        // ...after that, fetch the new exchange rates
        ExchangeRatesRepository(app).getExchangeRates()
    }

    fun getApiProvider(): LiveData<ApiProvider> {
        return apiProvider
    }

    fun setTheme(theme: Int) {
        Database(app).setTheme(theme)
        // switch theme
        AppCompatDelegate.setDefaultNightMode(
            when (theme) {
                0 -> AppCompatDelegate.MODE_NIGHT_NO
                1 -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }

    fun setLanguage(language: String) {
        val appLocale: LocaleListCompat =
            if (language == "system")
                LocaleListCompat.getEmptyLocaleList()
            else
                LocaleListCompat.forLanguageTags(
                    // pt_BR -> pt-BR
                    language.replace('_', '-')
                )
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    /**
     * returns the currently selected language in the following format:
     * "de_DE" or "de", if no country is set
     */
    fun getLanguage(): String? {
        val appLocale = AppCompatDelegate.getApplicationLocales()[0]
        return if (appLocale == null || (appLocale.language.isEmpty() && appLocale.country.isEmpty()))
            null
        else if (appLocale.country.isEmpty())
            appLocale.language
        else if (appLocale.language.isEmpty())
            appLocale.country
        else
            "${appLocale.language}_${appLocale.country}"
    }

    fun setPureBlackEnabled(enabled: Boolean) {
        Database(app).setPureBlackEnabled(enabled)
        // switch theme
        app.setTheme(
            if (enabled)
                R.style.AppTheme_PureBlack
            else
                R.style.AppTheme
        )

        // re-create all open activities, when we're in night mode
        if (isDarkThemeActive()) {
            TaskStackBuilder.create(app)
                // PreferencesActivity is always called from MainActivity
                .addNextIntent(Intent(app, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                })
                .addNextIntent(Intent(app, PreferenceActivity::class.java))
                .startActivities()
        }
    }

    private fun isDarkThemeActive(): Boolean {
        // app theme is dark
        val x = Database(app).getTheme() == 1
        // app theme is system default && current state is dark
        val y = Database(app).getTheme() == 2 && (app.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES)

        return x || y
    }

    fun setFee(fee: Float) {
        Database(app).setFee(fee)
    }

    fun getFee(): LiveData<Float> {
        return Database(app).getFee()
    }

    fun setFeeEnabled(enabled: Boolean) {
        Database(app).setFeeEnabled(enabled)
    }


    fun isPreviewConversionEnabled(): LiveData<Boolean> {
        return isPreviewConversionEnabled
    }

    fun setPreviewConversionEnabled(enabled: Boolean) {
        Database(app).setPreviewConversionEnabled(enabled)
    }

    fun setExtendedKeypadEnabled(enabled: Boolean) {
        Database(app).setExtendedKeypadEnabled(enabled)
    }

}
