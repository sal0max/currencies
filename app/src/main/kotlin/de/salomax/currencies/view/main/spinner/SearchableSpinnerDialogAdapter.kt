package de.salomax.currencies.view.main.spinner

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import de.salomax.currencies.R
import de.salomax.currencies.model.Currency
import de.salomax.currencies.model.Rate

@SuppressLint("NotifyDataSetChanged")
class SearchableSpinnerDialogAdapter(private val context: Context) :
    RecyclerView.Adapter<SearchableSpinnerDialogAdapter.ViewHolder>() {

    // listeners
    var onRateClicked: ((Rate, Int) -> Unit)? = null
    var onStarClicked: ((Rate) -> Unit)? = null

    private var rates: List<Rate> = listOf()
    private var ratesFiltered: MutableList<Rate> = mutableListOf()
    private var stars: Set<Currency> = setOf()

    private var filterStarred = false
    private var filterText: String? = null

    private val drawableFav = ContextCompat.getDrawable(context, R.drawable.ic_favorite)
    private val drawableFavEmpty = ContextCompat.getDrawable(context, R.drawable.ic_favorite_empty)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_currency_dropdown, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = ratesFiltered[position]
        holder.ivFlag.setImageDrawable(item.currency.flag(context))
        holder.tvCode.text = item.currency.iso4217Alpha()
        holder.tvName.text = item.currency.fullName(context)
        holder.btnStar.setImageDrawable(
            if (stars.contains(item.currency)) drawableFav
            else drawableFavEmpty
        )
    }

    override fun getItemCount(): Int {
        return ratesFiltered.size
    }

    fun setRates(rates: List<Rate>?) {
        if (rates == null)
            this.rates = ArrayList()
        else
            this.rates = rates
        update()
    }

    fun setStars(stars: Set<Currency>?) {
        if (stars == null)
            this.stars = HashSet()
        else
            this.stars = stars
        update()
    }

    fun filterStarred(enabled: Boolean) {
        this.filterStarred = enabled
        update()
    }

    fun filter(constraint: String?) {
        this.filterText = constraint
        update()
    }

    private fun update() {
        ratesFiltered = rates
            // find all rates based on both their code name or their full name
            .filter { rate ->
                if (filterText != null)
                    // full name
                    rate.currency.fullName(context).contains(filterText!!, ignoreCase = true)
                    // code name
                    || rate.currency.iso4217Alpha().contains(filterText!!, ignoreCase = true)
                else
                    true
            }
            // starred
            .filter { rate ->
                if (filterStarred)
                    stars.contains(rate.currency)
                else
                    true
            }.toMutableList()

        notifyDataSetChanged()
    }

    internal fun reset() {
        filterText = null
        ratesFiltered = rates.toMutableList()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val ivFlag: ImageView = itemView.findViewById(R.id.image)
        val tvCode: TextView = itemView.findViewById(R.id.text2)
        val tvName: TextView = itemView.findViewById(R.id.text)
        val btnStar: ImageButton = itemView.findViewById(R.id.btn_fav)

        init {
            itemView.setOnClickListener {
                onRateClicked?.invoke(ratesFiltered[layoutPosition], findOriginalPosition(layoutPosition))
            }
            btnStar.setOnClickListener {
                onStarClicked?.invoke(ratesFiltered[layoutPosition])
            }
        }

        private fun findOriginalPosition(filteredPosition: Int): Int {
            return rates.indexOf(ratesFiltered[filteredPosition])
        }

    }

}
