package de.salomax.currencies.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import de.salomax.currencies.R
import de.salomax.currencies.repository.Database

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // pure black
        setTheme(
            if (Database(this).isPureBlackEnabled())
                R.style.AppTheme_PureBlack
            else
                R.style.AppTheme
        )
        // theme
        AppCompatDelegate.setDefaultNightMode(
            when (Database(this)
                .getTheme()) {
                0 -> AppCompatDelegate.MODE_NIGHT_NO
                1 -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )

        super.onCreate(savedInstanceState)
    }

}