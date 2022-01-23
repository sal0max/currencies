package de.salomax.currencies.model

import android.content.Context
import de.salomax.currencies.R

enum class ApiProvider(
    val number: Int, // safer ordinal; DON'T CHANGE!
    val baseUrl: String,
) {
    EXCHANGERATE_HOST(0, "https://api.exchangerate.host"),
    FRANKFURTER_APP(1, "https://api.frankfurter.app"),
    FER_EE(2, "https://api.fer.ee");

    companion object {
        fun fromNumber(value: Int): ApiProvider? = values().firstOrNull { it.number == value }
    }

    fun getName(context: Context): CharSequence? {
        return context.resources.getTextArray(R.array.api_names)[
                when (this) {
                    EXCHANGERATE_HOST -> 0
                    FRANKFURTER_APP -> 1
                    FER_EE -> 2
                }
        ]
    }

    fun getDescription(context: Context): CharSequence? {
        return context.resources.getTextArray(R.array.api_about_summary)[
                when (this) {
                    EXCHANGERATE_HOST -> 0
                    FRANKFURTER_APP -> 1
                    FER_EE -> 2
                }
        ]
    }

    fun getUpdateIntervalDescription(context: Context): CharSequence? {
        return context.resources.getTextArray(R.array.api_refreshPeriod_summary)[
                when (this) {
                    EXCHANGERATE_HOST -> 0
                    FRANKFURTER_APP -> 1
                    FER_EE -> 2
                }
        ]
    }

}
