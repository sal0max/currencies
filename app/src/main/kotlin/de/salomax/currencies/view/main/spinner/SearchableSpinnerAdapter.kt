package de.salomax.currencies.view.main.spinner

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import de.salomax.currencies.R
import de.salomax.currencies.model.Rate
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class SearchableSpinnerAdapter(context: Context, resource: Int) :
    ArrayAdapter<Rate>(context, resource) {

    private var rates: List<Rate> = ArrayList()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView
        val holder: ViewHolder

        // view holder
        if (v == null) {
            v = LayoutInflater.from(context).inflate(R.layout.row_currency, parent, false)

            holder = ViewHolder()
            holder.flag = v.findViewById(R.id.image)
            holder.code = v.findViewById(R.id.text)

            v?.tag = holder
        } else {
            holder = v.tag as ViewHolder
        }

        // populate
        val item = getItem(position)
        holder.flag?.setImageDrawable(item.getFlag(context))
        holder.code?.text = item.code

        return v!!
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getView(position, convertView, parent)
    }

    override fun getCount(): Int {
        return rates.size
    }

    override fun getItem(position: Int): Rate {
        return rates[position]
    }

    override fun getPosition(item: Rate?): Int {
        return rates.indexOf(item)
    }

    /**
     * @param name e.g. "AUD", "EUR" or "USD"
     * @returns the position of the Rate for the given string.
     */
    fun getPosition(name: String): Int {
        return rates.indexOf(
            rates.find {
                it.code == name
            }
        )
    }

    fun setRates(rates: List<Rate>?) {
        if (rates == null)
            this.rates = ArrayList()
        else
            this.rates = rates
        notifyDataSetChanged()
    }

    internal class ViewHolder {
        var flag: ImageView? = null
        var code: TextView? = null
    }

}
