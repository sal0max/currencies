package de.salomax.currencies.model

import android.content.Context
import com.squareup.moshi.JsonClass
import de.salomax.currencies.R

@JsonClass(generateAdapter = false) // see https://stackoverflow.com/a/64085370/421140
enum class ApiProvider(
    val number: Int, // safer ordinal; DON'T CHANGE!
    val baseUrl: String,
) {
    EXCHANGERATE_HOST(0, "https://api.exchangerate.host"),
    FRANKFURTER_APP(1, "https://api.frankfurter.app"),
    FER_EE(2, "https://api.fer.ee"),
    INFOR_EURO(3, "https://ec.europa.eu/budg/inforeuro/api/public");

    companion object {
        fun fromNumber(value: Int): ApiProvider? = values().firstOrNull { it.number == value }
    }

    fun getName(context: Context): CharSequence? {
        return context.resources.getTextArray(R.array.api_names)[
                when (this) {
                    EXCHANGERATE_HOST -> 0
                    FRANKFURTER_APP -> 1
                    FER_EE -> 2
                    INFOR_EURO -> 3
                }
        ]
    }

    fun getDescription(context: Context): CharSequence? {
        return context.resources.getTextArray(R.array.api_about_summary)[
                when (this) {
                    EXCHANGERATE_HOST -> 0
                    FRANKFURTER_APP -> 1
                    FER_EE -> 2
                    INFOR_EURO -> 3
                }
        ]
    }

    fun getUpdateIntervalDescription(context: Context): CharSequence? {
        return context.resources.getTextArray(R.array.api_refreshPeriod_summary)[
                when (this) {
                    EXCHANGERATE_HOST -> 0
                    FRANKFURTER_APP -> 1
                    FER_EE -> 2
                    INFOR_EURO -> 3
                }
        ]
    }

}
