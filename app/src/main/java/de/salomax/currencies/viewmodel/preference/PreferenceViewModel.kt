package de.salomax.currencies.viewmodel.preference

import android.app.Application
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.TaskStackBuilder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import de.salomax.currencies.R
import de.salomax.currencies.repository.Database
import de.salomax.currencies.repository.ExchangeRatesRepository
import de.salomax.currencies.view.main.MainActivity
import de.salomax.currencies.view.preference.PreferenceActivity

class PreferenceViewModel(private val app: Application) : AndroidViewModel(app) {

    fun setApiProvider(api: Int) {
        // first put provider to db...
        Database(app).setApiProvider(api)
        // ...after that, fetch the new exchange rates
        ExchangeRatesRepository(app).getExchangeRates()
    }

    fun getApiProvider(): LiveData<Int> {
        return Database(app).getApiProviderAsync()
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
        if (// device is in night mode
            app.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            // app is in night mode
            && Database(app).getTheme() != 0
        ) {
            TaskStackBuilder.create(app)
                // PreferencesActivity is always called from MainActivity
                .addNextIntent(Intent(app, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                })
                .addNextIntent(Intent(app, PreferenceActivity::class.java))
                .startActivities()
        }
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

}
