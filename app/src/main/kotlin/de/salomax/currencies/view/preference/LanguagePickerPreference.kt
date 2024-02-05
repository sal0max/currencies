package de.salomax.currencies.view.preference

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.preference.ListPreference
import com.google.android.material.radiobutton.MaterialRadioButton
import de.salomax.currencies.R
import de.salomax.currencies.model.Language
import de.salomax.currencies.viewmodel.preference.PreferenceViewModel

@Suppress("unused")
class LanguagePickerPreference: ListPreference {

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet?) :
            super(context, attrs)

    constructor(context: Context) :
            super(context)

    private val viewModel = ViewModelProvider(context as AppCompatActivity)[PreferenceViewModel::class.java]
    private val appLanguage = viewModel.getLanguage()
    private var currentValue : Language? = Language.byIso(appLanguage) ?: Language.SYSTEM

    // e.g. ["en", "de", "pt_BR"]
    override fun getEntryValues(): Array<String> {
        return Language.entries.map { it.iso }.toTypedArray()
    }

    override fun findIndexOfValue(value: String?): Int {
        return Language.byIso(value)?.ordinal ?: -1
    }

    override fun setValue(value: String?) {
        currentValue = Language.byIso(value)
    }

    override fun setValueIndex(index: Int) {
        currentValue = Language.entries[index]
    }

    override fun getValue(): String? {
        return currentValue?.iso
    }

    override fun getSummary(): CharSequence? {
        return currentValue?.localizedName(context)
    }

    // open dialog
    override fun onClick() {
        val adapter = LanguagePickerDialogAdapter(context, currentValue)
        val dialog = AlertDialog.Builder(context)
            .setSingleChoiceItems(adapter, findIndexOfValue(value), null)
            .setTitle(R.string.language_title)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        adapter.onLanguageClicked = { language: Language ->
            callChangeListener(language.iso)
            dialog.dismiss()
        }
        dialog.show()
    }

    /**
     * A simple dialog adapter, that shows all available languages.
     */
    internal class LanguagePickerDialogAdapter(
        val context: Context,
        private val selectedItem: Language?
    ) : BaseAdapter() {

        // listener
        var onLanguageClicked: ((Language) -> Unit)? = null
        private val languages = Language.entries

        override fun getCount() = languages.size

        override fun getItem(position: Int) = languages[position]

        override fun getItemId(position: Int): Long {
            return getItem(position).hashCode().toLong()
        }

        @SuppressLint("InflateParams")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var view = convertView
            val holder: ViewHolder

            if (view == null) {
                view = (context as Activity).layoutInflater.inflate(R.layout.row_language_picker, null)
                holder = ViewHolder().apply {
                    parentView = view
                    radioButton = view.findViewById(R.id.radio)
                    textNative = view.findViewById(R.id.text)
                    textLocale = view.findViewById(R.id.text2)
                }
                view.tag = holder
            } else {
                holder = view.tag as ViewHolder
            }

            holder.run {
                val language = languages[position]
                // register clicks
                parentView?.setOnClickListener { onLanguageClicked?.invoke(language) }
                // check current active language
                radioButton?.isChecked = (language == selectedItem)
                // fill text
                when (language) {
                    Language.SYSTEM -> {
                        textNative?.text = language.localizedName(context)
                        textLocale?.visibility = View.GONE
                    }
                    else -> {
                        textNative?.text = language.nativeName(context)
                        textLocale?.text = language.localizedName(context)
                        textLocale?.visibility = View.VISIBLE
                    }
                }
            }

            return view!!
        }

        internal class ViewHolder {
            var parentView: View? = null
            var radioButton: MaterialRadioButton? = null
            var textNative: TextView? = null
            var textLocale: TextView? = null
        }
    }

}