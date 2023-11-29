package de.salomax.currencies.view.preference

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import de.salomax.currencies.BuildConfig
import de.salomax.currencies.R
import de.salomax.currencies.model.ApiProvider
import de.salomax.currencies.util.toHumanReadableNumber
import de.salomax.currencies.viewmodel.preference.PreferenceViewModel
import de.salomax.currencies.widget.EditTextSwitchPreference
import de.salomax.currencies.widget.LongSummaryPreference
import java.util.*

@Suppress("unused")
class PreferenceFragment: PreferenceFragmentCompat() {

    private lateinit var viewModel: PreferenceViewModel

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.prefs, rootKey)
        viewModel = ViewModelProvider(this)[PreferenceViewModel::class.java]

        // theme
        findPreference<ListPreference>(getString(R.string.theme_key))?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                viewModel.setTheme(newValue.toString().toInt())
                true
            }
        }

        // language
        findPreference<LanguagePickerPreference>(getString(R.string.language_key))?.apply {
            // listen for changes
            setOnPreferenceChangeListener { _, newValue ->
                viewModel.setLanguage(newValue.toString())
                true
            }
        }

        // conversion preview
        findPreference<SwitchPreferenceCompat>(getString(R.string.previewConversion_key))?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                viewModel.setPreviewConversionEnabled(newValue.toString().toBoolean())
                true
            }
        }

        // extended keypad
        findPreference<SwitchPreferenceCompat>(getString(R.string.extendedKeypad_key))?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                viewModel.setExtendedKeypadEnabled(newValue.toString().toBoolean())
                true
            }
        }

        // pure black
        findPreference<SwitchPreferenceCompat>(getString(R.string.pure_black_key))?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                viewModel.setPureBlackEnabled(newValue.toString().toBoolean())
                true
            }
        }

        // transaction fee
        val feePreference = findPreference<EditTextSwitchPreference>(getString(R.string.fee_key))
        feePreference?.setOnPreferenceChangeListener { _, newValue ->
            // fee amount changed
            if (newValue is String)
                try {
                    viewModel.setFee(
                        if (newValue.isEmpty()) 0f
                        else newValue.toFloat()
                    )
                } catch (e: NumberFormatException) {
                    viewModel.setFee(0f)
                }
            // fee enabled/disabled
            else if (newValue is Boolean)
                viewModel.setFeeEnabled(newValue)
            true
        }
        viewModel.getFee().observe(this) {
            feePreference?.summary = it.toHumanReadableNumber(requireContext(), showPositiveSign = true, suffix = "%")
            feePreference?.text = it.toString()
        }

        // api provider
        findPreference<ListPreference>(getString(R.string.api_key))?.apply {
            // initialize values
            val providers = ApiProvider.entries
            entries = providers.map { it.getName() }.toTypedArray()         // names
            entryValues = providers.map { it.id.toString() }.toTypedArray() // ids
            // listen for changes
            setOnPreferenceChangeListener { _, newValue ->
                viewModel.setApiProvider(
                    ApiProvider.fromId(newValue.toString().toInt())
                )
                true
            }
            // set default, if empty (empty means, there was no mapping for the stored value)
            if (entry == null) {
                val defaultProvider = ApiProvider.fromId(-1)
                viewModel.setApiProvider(defaultProvider)
                value = defaultProvider.id.toString()
            }
        }
        // change text according to selected api
        viewModel.getApiProvider().observe(this) {
            findPreference<LongSummaryPreference>(getString(R.string.key_apiProvider))?.apply {
                title =
                    resources.getString(R.string.api_about_title, it.getName())
                summary =
                    it.getDescription(context)
            }
            findPreference<LongSummaryPreference>(getString(R.string.key_refreshPeriod))?.summary =
                it.getUpdateIntervalDescription(requireContext())
        }

        // open source code repo
        findPreference<Preference>(getString(R.string.sourcecode_key))?.apply {
            setOnPreferenceClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/sal0max/currencies")
                    )
                )
                true
            }
        }

        // donate
        findPreference<Preference>(getString(R.string.donate_key))?.apply {
            // hide for Play Store - Google is a cunt
            @Suppress("KotlinConstantConditions")
            isVisible = when (BuildConfig.FLAVOR) {
                "play" -> false
                "fdroid" -> true
                else -> true
            }
            // go to PayPal, when clicked
            setOnPreferenceClickListener {
                startActivity(createIntent("https://www.paypal.com/donate?hosted_button_id=2JCY7E99V9DGC"))
                true
            }
        }

        // changelog
        findPreference<Preference>(getString(R.string.changelog_key))?.apply {
            setOnPreferenceClickListener {
                ChangelogDialog().show(childFragmentManager, null)
                true
            }
        }

        // about
        findPreference<Preference>(getString(R.string.version_key))?.apply {
            title = BuildConfig.VERSION_NAME
            summary = getString(R.string.version_summary, Calendar.getInstance().get(Calendar.YEAR).toString())
        }

        // rate
        findPreference<Preference>(getString(R.string.rate_key))?.apply {
            // hide for F-Droid - no rating mechanism there
            @Suppress("KotlinConstantConditions")
            isVisible = when (BuildConfig.FLAVOR) {
                "play" -> true
                else -> false
            }
            // open play store
            setOnPreferenceClickListener {
                // play store
                try {
                    startActivity(createIntent("market://details?id=de.salomax.currencies"))
                }
                // browser
                catch (e: ActivityNotFoundException) {
                    startActivity(createIntent("https://play.google.com/store/apps/details?id=de.salomax.currencies"))
                }
                true
            }
        }
    }

    private fun createIntent(url: String): Intent {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY
                    or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                    or Intent.FLAG_ACTIVITY_NEW_DOCUMENT
        )
        return intent
    }

}
