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
import de.salomax.currencies.util.prettyPrintPercent
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

        // theme
        findPreference<ListPreference>(getString(R.string.theme_key))?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                viewModel.setTheme(newValue.toString().toInt())
                true
            }
        }

        // transaction fee
        val feePreference = findPreference<EditTextSwitchPreference>(getString(R.string.fee_key))
        feePreference?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue is String)
                viewModel.setFee(newValue.toString().toFloat())
            else if (newValue is Boolean)
                viewModel.setFeeEnabled(newValue.toString().toBoolean())
            true
        }
        viewModel.getFee().observe(this, {
            feePreference?.summary = it.prettyPrintPercent(requireContext())
            feePreference?.text = it.toString()
        })

        // api provider
        findPreference<ListPreference>(getString(R.string.api_key))?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                viewModel.setApiProvider(newValue.toString().toInt())
                true
            }
        }
        // change text according to selected api
        viewModel.getApiProvider().observe(this, {
            findPreference<LongSummaryPreference>(getString(R.string.key_apiProvider))?.summary =
                resources.getTextArray(R.array.api_about_summary)[it]
            findPreference<LongSummaryPreference>(getString(R.string.key_refreshPeriod))?.summary =
                resources.getTextArray(R.array.api_refreshPeriod_summary)[it]
        })

        // donate
        findPreference<Preference>(getString(R.string.donate_key))?.apply {
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
        findPreference<Preference>(getString(R.string.about_key))?.apply {
            title = getString(R.string.aboutVersion, BuildConfig.VERSION_NAME)
            summary = getString(R.string.about_summary, Calendar.getInstance().get(Calendar.YEAR))
            setOnPreferenceClickListener {
                ChangelogDialog().show(childFragmentManager, null)
                true
            }
        }
    }

}
