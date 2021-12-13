package de.salomax.currencies.view

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.color.MaterialColors
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

        // "transparent" navigation bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            window.navigationBarColor = MaterialColors.getColor(this, R.attr.colorBackground, Color.BLACK)
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = booleanFromAttribute(R.attr.isLightTheme)
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = booleanFromAttribute(R.attr.isLightTheme)
        }

        super.onCreate(savedInstanceState)
    }

    private fun Context.booleanFromAttribute(attribute: Int): Boolean {
        val attributes = obtainStyledAttributes(intArrayOf(attribute))
        val dimension = attributes.getBoolean(0, false)
        attributes.recycle()
        return dimension
    }

}