package de.salomax.currencies.view.preference

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import de.salomax.currencies.BuildConfig
import de.salomax.currencies.R
import de.salomax.currencies.repository.ExchangeRatesRepository
import de.salomax.currencies.util.humanReadableFee
import de.salomax.currencies.viewmodel.preference.PreferenceViewModel
import de.salomax.currencies.widget.EditTextSwitchPreference
import java.util.*

@Suppress("unused")
class PreferenceFragment: PreferenceFragmentCompat() {

    private lateinit var viewModel: PreferenceViewModel

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.prefs, rootKey)
        viewModel = ViewModelProvider(this).get(PreferenceViewModel::class.java)

        // api provider
        val apiPreference = findPreference<ListPreference>(getString(R.string.prefKey_api))!!
        apiPreference.setOnPreferenceChangeListener { _, newValue ->
            viewModel.setApiProvider(newValue.toString().toInt())
            // fetch the new data
            ExchangeRatesRepository(requireContext()).getExchangeRates()
            true
        }

        // theme
        val themePreference = findPreference<ListPreference>(getString(R.string.prefKey_theme))!!
        themePreference.setOnPreferenceChangeListener { _, newValue ->
            viewModel.setTheme(newValue.toString().toInt())
            true
        }

        // fee
        val feePreference = findPreference<EditTextSwitchPreference>(getString(R.string.prefKey_fee))!!
        feePreference.setOnPreferenceChangeListener { _, newValue ->
            if (newValue is String)
                viewModel.setFee(newValue.toString().toFloat())
            else if (newValue is Boolean)
                viewModel.setFeeEnabled(newValue.toString().toBoolean())
            true
        }
        viewModel.getFee().observe(this, {
            feePreference.summary = it.humanReadableFee(requireContext())
            feePreference.text = it.toString()
        })

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
