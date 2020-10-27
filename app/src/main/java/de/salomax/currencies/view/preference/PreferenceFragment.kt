package de.salomax.currencies.view.preference

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import de.salomax.currencies.BuildConfig
import de.salomax.currencies.R
import de.salomax.currencies.viewmodel.preference.PreferenceViewModel
import java.util.*

@Suppress("unused")
class PreferenceFragment: PreferenceFragmentCompat() {

    private lateinit var viewModel: PreferenceViewModel

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.prefs, rootKey)
        viewModel = ViewModelProvider(this).get(PreferenceViewModel::class.java)

        // theme
        val themePreference = findPreference<ListPreference>(getString(R.string.prefKey_theme))!!
        themePreference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                viewModel.setTheme(newValue.toString().toInt())
                true
            }

        // about
        val aboutPreference = findPreference<Preference>(getString(R.string.prefKey_about))!!
        aboutPreference.title = getString(R.string.prefTitle_about, BuildConfig.VERSION_NAME)
        aboutPreference.summary = getString(R.string.prefSummary_about, Calendar.getInstance().get(Calendar.YEAR))
        aboutPreference.setOnPreferenceClickListener {
            ChangelogDialog().show(childFragmentManager, null)
            true
        }
    }

}
