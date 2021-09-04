package de.salomax.currencies.util

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.lifecycle.LiveData
import de.salomax.currencies.model.Rate
import de.salomax.currencies.model.Timeline
import java.time.LocalDate

class SharedPreferenceTimelineLiveData(private val sharedPrefs: SharedPreferences) : LiveData<Timeline?>() {

    private fun getValueFromPreferences(): Timeline? {
        return if (sharedPrefs.all.isEmpty())
            null
        else
            Timeline(
                true, // success always true, when serving cached data
                null, // error message always null, when serving cached data
                sharedPrefs.getString("_base", null)!!,
                LocalDate.parse(sharedPrefs.getString("_startDate", null))!!,
                LocalDate.parse(sharedPrefs.getString("_endDate", null))!!,
                sharedPrefs.all.entries
                    .filterNot { it.key.startsWith("_") }
                    .sortedBy { it.key }
                    .map { LocalDate.parse(it.key)!! to Rate(sharedPrefs.getString("_target", null)!!, it.value as Float) }
                    .toMap()
            )
    }

    private val preferenceChangeListener = OnSharedPreferenceChangeListener { _: SharedPreferences?, _: String? ->
            postValue(getValueFromPreferences())
    }

    override fun onActive() {
        super.onActive()
        postValue(getValueFromPreferences())
        sharedPrefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onInactive() {
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        super.onInactive()
    }

}
