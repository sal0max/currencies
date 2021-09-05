package de.salomax.currencies

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import de.salomax.currencies.repository.Database

@Suppress("unused")
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        // theme
        AppCompatDelegate.setDefaultNightMode(
            when (Database(this)
                .getTheme()) {
                0 -> AppCompatDelegate.MODE_NIGHT_NO
                1 -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }

}
