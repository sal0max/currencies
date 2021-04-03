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

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent, 0)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent, 1)
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

    private fun getCustomView(position: Int, convertView: View?, parent: ViewGroup, viewType: Int): View {
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

        holder.name?.text = when(viewType) {
            0 -> item.code
            1 -> "${item.getName(context)} (${item.code})"
            else -> null
        }
        holder.image?.setImageDrawable(item.getFlag(context))

        return v!!
    }

    internal class ViewHolder {
        var name: TextView? = null
        var image: ImageView? = null
    }

}
