package de.salomax.currencies.viewmodel.preference

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import de.salomax.currencies.repository.Database

class PreferenceViewModel(application: Application) : AndroidViewModel(application) {

    fun setApiProvider(api: Int) {
        Database.getInstance(getApplication()).setApiProvider(api)
    }

    fun getApiProvider(): LiveData<Int> {
        return Database.getInstance(getApplication()).getApiProviderAsync()
    }

    fun setTheme(theme: Int) {
        Database.getInstance(getApplication()).setTheme(theme)
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
        Database.getInstance(getApplication()).setFee(fee)
    }

    fun getFee(): LiveData<Float> {
        return Database.getInstance(getApplication()).getFee()
    }

    fun setFeeEnabled(enabled: Boolean) {
        Database.getInstance(getApplication()).setFeeEnabled(enabled)
    }

}
