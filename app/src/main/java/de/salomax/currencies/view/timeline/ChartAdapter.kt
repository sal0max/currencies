package de.salomax.currencies.view.timeline

import com.robinhood.spark.SparkAdapter
import de.salomax.currencies.model.Rate
import java.time.LocalDate

class ChartAdapter : SparkAdapter() {

    var entries: List<Map.Entry<LocalDate, Rate>>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getCount(): Int = entries?.size ?: 0

    override fun getItem(index: Int): Map.Entry<LocalDate, Rate>? = entries?.get(index)

    override fun getY(index: Int): Float = getItem(index)?.value?.value ?: 0f

    override fun hasBaseLine() = true

    override fun getBaseLine(): Float = entries?.last()?.value?.value ?: 0f

}
