package de.salomax.currencies.view.preference

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import de.salomax.currencies.BuildConfig
import de.salomax.currencies.R
import de.salomax.currencies.util.humanReadableFee
import de.salomax.currencies.viewmodel.preference.PreferenceViewModel
import de.salomax.currencies.widget.EditTextSwitchPreference
import de.salomax.currencies.widget.LongSummaryPreference
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
            true
        }
        // change text according to selected api
        viewModel.getApiProvider().observe(this, {
            findPreference<LongSummaryPreference>(getString(R.string.prefKey_dataSource))!!.summary =
                resources.getTextArray(R.array.prefSummary_dataSource)[it]
            findPreference<LongSummaryPreference>(getString(R.string.prefKey_dataUpdate))!!.summary =
                resources.getTextArray(R.array.prefSummary_dataUpdate)[it]
        })

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

        // donate
        findPreference<Preference>(getString(R.string.prefKey_donate))?.apply {
            // hide for Play Store - Google is a cunt
            isVisible = when (BuildConfig.FLAVOR) {
                "play" -> false
                "fdroid" -> true
                else -> true
            }
            // go to PayPal, when clicked
            setOnPreferenceClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.paypal.com/donate?hosted_button_id=2JCY7E99V9DGC")
                    )
                )
                true
            }
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
