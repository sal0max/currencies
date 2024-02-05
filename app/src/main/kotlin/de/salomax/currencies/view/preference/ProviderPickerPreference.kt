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
import androidx.core.text.HtmlCompat
import androidx.preference.ListPreference
import com.google.android.material.radiobutton.MaterialRadioButton
import de.salomax.currencies.R
import de.salomax.currencies.model.ApiProvider

@Suppress("unused")
class ProviderPickerPreference: ListPreference {

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet?) :
            super(context, attrs)

    constructor(context: Context) :
            super(context)

    // open dialog
    override fun onClick() {
        val adapter = ProviderPickerDialogAdapter(context, ApiProvider.fromId(value.toInt()))
        val dialog = AlertDialog.Builder(context)
            .setSingleChoiceItems(adapter, findIndexOfValue(value), null)
            .setTitle(R.string.api_title)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        adapter.onProviderClicked = { provider: ApiProvider ->
            callChangeListener(provider.id)
            value = provider.id.toString()
            dialog.dismiss()
        }
        dialog.show()
    }

    /**
     * A simple dialog adapter, that shows all available API providers.
     */
    internal class ProviderPickerDialogAdapter(
        val context: Context,
        private val selectedItem: ApiProvider?
    ) : BaseAdapter() {

        // listener
        var onProviderClicked: ((ApiProvider) -> Unit)? = null
        private val providers = ApiProvider.entries

        override fun getCount() = providers.size

        override fun getItem(position: Int) = providers[position]

        override fun getItemId(position: Int): Long {
            return getItem(position).id.toLong()
        }

        @SuppressLint("InflateParams")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var view = convertView
            val holder: ViewHolder

            if (view == null) {
                view = (context as Activity).layoutInflater.inflate(R.layout.row_provider_picker, null)
                holder = ViewHolder().apply {
                    parentView = view
                    radioButton = view.findViewById(R.id.radio)
                    textProviderName = view.findViewById(R.id.text)
                    textDesc = view.findViewById(R.id.text2)
                    textHint = view.findViewById(R.id.text3)
                }
                view.tag = holder
            } else {
                holder = view.tag as ViewHolder
            }

            holder.run {
                val provider = providers[position]
                // register clicks
                parentView?.setOnClickListener {
                    // TODO

                    onProviderClicked?.invoke(provider)
                }
                // check current active api provider
                radioButton?.isChecked = (provider == selectedItem)
                // fill text
                textProviderName?.text = provider.getName()
                textDesc?.text = context.getString(
                    R.string.api_descriptionShort,
                    provider.getCurrencyCount(),
                    provider.getSource(context)
                )
                textHint?.visibility = provider.getHint(context)?.let {
                    textHint?.text = it
                    View.VISIBLE
                } ?: View.GONE
            }

            return view!!
        }

        internal class ViewHolder {
            var parentView: View? = null
            var radioButton: MaterialRadioButton? = null
            var textProviderName: TextView? = null
            var textDesc: TextView? = null
            var textHint: TextView? = null
        }
    }

}