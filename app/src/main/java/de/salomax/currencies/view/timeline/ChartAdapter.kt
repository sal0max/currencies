package de.salomax.currencies.view.timeline

import com.robinhood.spark.SparkAdapter
import de.salomax.currencies.model.Rate
import java.time.LocalDate

class ChartAdapter : SparkAdapter() {

    var entries: List<Map.Entry<LocalDate, List<Rate>>>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getCount(): Int = entries?.size ?: 0

    override fun getItem(index: Int): Any? = entries?.get(index)

    override fun getY(index: Int): Float = entries?.get(index)?.value?.first()?.value ?: 0f

    override fun hasBaseLine() = true

    override fun getBaseLine(): Float = entries?.last()?.value?.first()?.value ?: 0f

}
