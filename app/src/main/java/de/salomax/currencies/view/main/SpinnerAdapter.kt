package de.salomax.currencies.view.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import de.salomax.currencies.R
import de.salomax.currencies.model.Rate
import java.text.Collator
import java.util.*

class SpinnerAdapter(context: Context, resource: Int, private var objects: List<Rate>) :
    ArrayAdapter<Rate>(context, resource, objects) {

    init {
        // sort by name (also takes care of special characters like umlauts)
        super.sort { o1: Rate, o2: Rate ->
            Collator.getInstance(Locale.getDefault())
                .compare(o1.getName(context), o2.getName(context))
        }
    }

    // selected item
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView
        val holder: ViewHolder

        // view holder
        if (v == null) {
            v = LayoutInflater.from(context).inflate(R.layout.row_currency, parent, false)

            holder = ViewHolder()
            holder.code = v.findViewById(R.id.text)
            holder.image = v.findViewById(R.id.image)

            v?.tag = holder
        } else {
            holder = v.tag as ViewHolder
        }

        // populate
        val item = getItem(position)
        holder.code?.text = item.code
        holder.image?.setImageDrawable(item.getFlag(context))

        return v!!
    }

    // list
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView
        val holder: DropdownViewHolder

        // view holder
        if (v == null) {
            v = LayoutInflater.from(context).inflate(R.layout.row_currency_dropdown, parent, false)

            holder = DropdownViewHolder()
            holder.name = v.findViewById(R.id.text)
            holder.code = v.findViewById(R.id.text2)
            holder.image = v.findViewById(R.id.image)

            v?.tag = holder
        } else {
            holder = v.tag as DropdownViewHolder
        }

        // populate
        val item = getItem(position)
        holder.code?.text = item.code
        holder.name?.text = item.getName(context)
        holder.image?.setImageDrawable(item.getFlag(context))

        return v!!
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
                it.code == name
            }
        )
    }


    internal class ViewHolder {
        var code: TextView? = null
        var image: ImageView? = null
    }

    internal class DropdownViewHolder {
        var name: TextView? = null
        var code: TextView? = null
        var image: ImageView? = null
    }

}
