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
    fun getPosition(name: String): Int {
        return objects.indexOf(
            objects.find {
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
            "AED" -> ContextCompat.getDrawable(context, R.drawable.flag_ae)
            "AUD" -> ContextCompat.getDrawable(context, R.drawable.flag_au)
            "AWG" -> ContextCompat.getDrawable(context, R.drawable.flag_aw)
            "BAM" -> ContextCompat.getDrawable(context, R.drawable.flag_ba)
            "BBD" -> ContextCompat.getDrawable(context, R.drawable.flag_bb)
            "BGN" -> ContextCompat.getDrawable(context, R.drawable.flag_bg)
            "BHD" -> ContextCompat.getDrawable(context, R.drawable.flag_bh)
            "BRL" -> ContextCompat.getDrawable(context, R.drawable.flag_br)
            "BSD" -> ContextCompat.getDrawable(context, R.drawable.flag_bs)
            "BZD" -> ContextCompat.getDrawable(context, R.drawable.flag_bz)
            "CAD" -> ContextCompat.getDrawable(context, R.drawable.flag_ca)
            "CHF" -> ContextCompat.getDrawable(context, R.drawable.flag_ch)
            "CNY" -> ContextCompat.getDrawable(context, R.drawable.flag_cn)
            "CUC" -> ContextCompat.getDrawable(context, R.drawable.flag_cu)
            "CZK" -> ContextCompat.getDrawable(context, R.drawable.flag_cz)
            "DKK" -> ContextCompat.getDrawable(context, R.drawable.flag_dk)
            "EUR" -> ContextCompat.getDrawable(context, R.drawable.flag_eu)
            "FOK" -> ContextCompat.getDrawable(context, R.drawable.flag_fo)
            "GBP" -> ContextCompat.getDrawable(context, R.drawable.flag_gb)
            "HKD" -> ContextCompat.getDrawable(context, R.drawable.flag_hk)
            "HRK" -> ContextCompat.getDrawable(context, R.drawable.flag_hr)
            "HUF" -> ContextCompat.getDrawable(context, R.drawable.flag_hu)
            "IDR" -> ContextCompat.getDrawable(context, R.drawable.flag_id)
            "ILS" -> ContextCompat.getDrawable(context, R.drawable.flag_il)
            "INR" -> ContextCompat.getDrawable(context, R.drawable.flag_in)
            "ISK" -> ContextCompat.getDrawable(context, R.drawable.flag_is)
            "JOD" -> ContextCompat.getDrawable(context, R.drawable.flag_jo)
            "JPY" -> ContextCompat.getDrawable(context, R.drawable.flag_jp)
            "KRW" -> ContextCompat.getDrawable(context, R.drawable.flag_kr)
            "LBP" -> ContextCompat.getDrawable(context, R.drawable.flag_lb)
            "MXN" -> ContextCompat.getDrawable(context, R.drawable.flag_mx)
            "MYR" -> ContextCompat.getDrawable(context, R.drawable.flag_my)
            "NAD" -> ContextCompat.getDrawable(context, R.drawable.flag_na)
            "NOK" -> ContextCompat.getDrawable(context, R.drawable.flag_no)
            "NPR" -> ContextCompat.getDrawable(context, R.drawable.flag_np)
            "NZD" -> ContextCompat.getDrawable(context, R.drawable.flag_nz)
            "OMR" -> ContextCompat.getDrawable(context, R.drawable.flag_om)
            "PHP" -> ContextCompat.getDrawable(context, R.drawable.flag_ph)
            "PLN" -> ContextCompat.getDrawable(context, R.drawable.flag_pl)
            "QAR" -> ContextCompat.getDrawable(context, R.drawable.flag_qa)
            "RON" -> ContextCompat.getDrawable(context, R.drawable.flag_ro)
            "RUB" -> ContextCompat.getDrawable(context, R.drawable.flag_ru)
            "SAR" -> ContextCompat.getDrawable(context, R.drawable.flag_sa)
            "SEK" -> ContextCompat.getDrawable(context, R.drawable.flag_se)
            "SGD" -> ContextCompat.getDrawable(context, R.drawable.flag_sg)
            "THB" -> ContextCompat.getDrawable(context, R.drawable.flag_th)
            "TRY" -> ContextCompat.getDrawable(context, R.drawable.flag_tr)
            "USD" -> ContextCompat.getDrawable(context, R.drawable.flag_us)
            //"XCD" ->
            "ZAR" -> ContextCompat.getDrawable(context, R.drawable.flag_za)
            else -> ContextCompat.getDrawable(context, R.drawable.flag_unknown)
        }
    }

    internal class ViewHolder {
        var name: TextView? = null
        var image: ImageView? = null
    }

}
