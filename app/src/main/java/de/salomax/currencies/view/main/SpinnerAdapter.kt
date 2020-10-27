package de.salomax.currencies.view.main

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import de.salomax.currencies.R
import de.salomax.currencies.model.Rate

class SpinnerAdapter(context: Context, resource: Int, private val objects: List<Rate>) :
    ArrayAdapter<Rate>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent)
    }

    override fun getItem(position: Int): Rate {
        return objects[position]
    }

    /**
     * @param name e.g. "AUD", "EUR" or "USD"
     * @returns the position of the Rate for the given string.
     */
    fun getPosition(name: String): Int? {
        return objects.indexOf(
            objects.find { it ->
                it.name == name
            }
        )
    }

    private fun getCustomView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView
        val holder: ViewHolder

        if (v == null) {
            v = LayoutInflater.from(context).inflate(R.layout.row_currency, parent, false)

            holder = ViewHolder()
            holder.name = v.findViewById(R.id.text)
            holder.image = v.findViewById(R.id.image)

            v?.tag = holder
        } else {
            holder = v.tag as ViewHolder
        }

        val item = getItem(position)

        holder.name?.text = item.name
        holder.image?.setImageDrawable(getFlag(item.name))

        return v!!
    }

    private fun getFlag(name: String): Drawable? {
        return when (name) {
            "AUD" -> ContextCompat.getDrawable(context, R.drawable.flag_au)
            "BGN" -> ContextCompat.getDrawable(context, R.drawable.flag_bg)
            "BRL" -> ContextCompat.getDrawable(context, R.drawable.flag_br)
            "CAD" -> ContextCompat.getDrawable(context, R.drawable.flag_ca)
            "CHF" -> ContextCompat.getDrawable(context, R.drawable.flag_ch)
            "CNY" -> ContextCompat.getDrawable(context, R.drawable.flag_cn)
            "CZK" -> ContextCompat.getDrawable(context, R.drawable.flag_cz)
            "DKK" -> ContextCompat.getDrawable(context, R.drawable.flag_dk)
            "EUR" -> ContextCompat.getDrawable(context, R.drawable.flag_eu)
            "GBP" -> ContextCompat.getDrawable(context, R.drawable.flag_gb)
            "HKD" -> ContextCompat.getDrawable(context, R.drawable.flag_hk)
            "HRK" -> ContextCompat.getDrawable(context, R.drawable.flag_hr)
            "HUF" -> ContextCompat.getDrawable(context, R.drawable.flag_hu)
            "IDR" -> ContextCompat.getDrawable(context, R.drawable.flag_id)
            "ILS" -> ContextCompat.getDrawable(context, R.drawable.flag_il)
            "INR" -> ContextCompat.getDrawable(context, R.drawable.flag_in)
            "ISK" -> ContextCompat.getDrawable(context, R.drawable.flag_is)
            "JPY" -> ContextCompat.getDrawable(context, R.drawable.flag_jp)
            "KRW" -> ContextCompat.getDrawable(context, R.drawable.flag_kr)
            "MXN" -> ContextCompat.getDrawable(context, R.drawable.flag_mx)
            "MYR" -> ContextCompat.getDrawable(context, R.drawable.flag_my)
            "NOK" -> ContextCompat.getDrawable(context, R.drawable.flag_no)
            "NZD" -> ContextCompat.getDrawable(context, R.drawable.flag_nz)
            "PHP" -> ContextCompat.getDrawable(context, R.drawable.flag_ph)
            "PLN" -> ContextCompat.getDrawable(context, R.drawable.flag_pl)
            "RON" -> ContextCompat.getDrawable(context, R.drawable.flag_ro)
            "RUB" -> ContextCompat.getDrawable(context, R.drawable.flag_ru)
            "SEK" -> ContextCompat.getDrawable(context, R.drawable.flag_se)
            "SGD" -> ContextCompat.getDrawable(context, R.drawable.flag_sg)
            "THB" -> ContextCompat.getDrawable(context, R.drawable.flag_th)
            "TRY" -> ContextCompat.getDrawable(context, R.drawable.flag_tr)
            "USD" -> ContextCompat.getDrawable(context, R.drawable.flag_us)
            "ZAR" -> ContextCompat.getDrawable(context, R.drawable.flag_za)
            else -> ContextCompat.getDrawable(context, R.drawable.flag_unknown)
        }
    }

    internal class ViewHolder {
        var name: TextView? = null
        var image: ImageView? = null
    }

}
