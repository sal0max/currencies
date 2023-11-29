package de.salomax.currencies.view.timeline

import android.annotation.SuppressLint
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.core.text.bold
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import com.google.android.material.color.MaterialColors
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.robinhood.spark.SparkView
import de.salomax.currencies.R
import de.salomax.currencies.model.Currency
import de.salomax.currencies.util.dpToPx
import de.salomax.currencies.util.getLocale
import de.salomax.currencies.util.hasAppendedCurrencySymbol
import de.salomax.currencies.util.toHumanReadableNumber
import de.salomax.currencies.view.BaseActivity
import de.salomax.currencies.viewmodel.timeline.TimelineViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.max

class TimelineActivity : BaseActivity() {

    //
    private lateinit var formatter: DateTimeFormatter
    private lateinit var timelineModel: TimelineViewModel

    // views
    private var menuItemToggle: MenuItem? = null

    private lateinit var refreshIndicator: LinearProgressIndicator
    private lateinit var timelineChart: SparkView
    private lateinit var textProvider: TextView
    private lateinit var textRateDifference: TextView
    private lateinit var divider: View

    private lateinit var textPastRateDate: TextView
    private lateinit var textPastRateValue: TextView

    private lateinit var textCurrentRateDate: TextView
    private lateinit var textCurrentRateValue: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        formatter = DateTimeFormatter
            .ofLocalizedDate(FormatStyle.MEDIUM)
            .withLocale(getLocale(this))

        // general layout
        setContentView(R.layout.activity_timeline)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        // what currencies to convert
        val currencyFrom =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                intent.getSerializableExtra("ARG_FROM", Currency::class.java) ?: Currency.EUR
            else
                @Suppress("DEPRECATION")
                intent.getSerializableExtra("ARG_FROM")?.let { it as Currency } ?: Currency.EUR

        val currencyTo =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                intent.getSerializableExtra("ARG_TO", Currency::class.java) ?: Currency.USD
            else
                @Suppress("DEPRECATION")
                intent.getSerializableExtra("ARG_TO")?.let { it as Currency } ?: Currency.USD

        // model
        this.timelineModel = ViewModelProvider(
            this,
            TimelineViewModel.Factory(this.application, currencyFrom, currencyTo)
        )[TimelineViewModel::class.java]

        // views
        findViews()

        // configure timeline view
        initChartView()
        initStatsView()

        // listeners & stuff
        setListeners()

        // heavy lifting
        observe()

        // foldable devices
        prepareFoldableLayoutChanges()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.timeline, menu)
        menuItemToggle = menu.findItem(R.id.toggle)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.toggle -> {
                timelineModel.toggleCurrencies()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun findViews() {
        this.refreshIndicator = findViewById(R.id.refreshIndicator)
        this.timelineChart = findViewById(R.id.timeline_chart)
        this.textProvider = findViewById(R.id.textProvider)
        this.textRateDifference = findViewById(R.id.text_rate_difference_percent)
        this.divider = findViewById(R.id.divider)

        this.textPastRateDate = findViewById(R.id.text_date_past)
        this.textPastRateValue = findViewById(R.id.text_rate_past)

        this.textCurrentRateDate = findViewById(R.id.text_date_current)
        this.textCurrentRateValue = findViewById(R.id.text_rate_current)
    }

    private fun initChartView() {
        timelineChart.apply {
            // dashed baseline
            baseLinePaint = baseLinePaint.apply {
                strokeWidth = 1f.dpToPx()
                style = Paint.Style.STROKE
                pathEffect = DashPathEffect(floatArrayOf(1f.dpToPx(), 4f.dpToPx()), 0f)
            }
            // scrub (tooltip)
            scrubListener = SparkView.OnScrubListener { data ->
                data as Map.Entry<*, *>?
                timelineModel.setPastDate(data?.key as LocalDate?)
            }
            // adapter
            adapter = ChartAdapter()
        }
    }

    private fun initStatsView() {
        val view1 = findViewById<View>(R.id.stats_row_1).findViewById<TextView>(R.id.text)
        val view2 = findViewById<View>(R.id.stats_row_2).findViewById<TextView>(R.id.text)
        val view3 = findViewById<View>(R.id.stats_row_3).findViewById<TextView>(R.id.text)

        // set the title of "avg", "min", "max
        val string1 = getString(R.string.rate_average)
        val string2 = getString(R.string.rate_min)
        val string3 = getString(R.string.rate_max)
        view1.text = string1
        view2.text = string2
        view3.text = string3

        // set the width of "avg", "min", "max" to the same value
        val width1 = view1.paint.measureText(string1)
        val width2 = view2.paint.measureText(string2)
        val width3 = view3.paint.measureText(string3)
        val maxWidth = (max(width1, max(width2, width3)) * 1.25).toInt()
        view1.width = maxWidth
        view2.width = maxWidth
        view3.width = maxWidth
    }

    private fun setListeners() {
        findViewById<com.google.android.material.button.MaterialButtonToggleGroup>(R.id.toggleButton)
            .addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked)
                    when (checkedId) {
                        R.id.button_week -> timelineModel.setTimePeriod(TimelineViewModel.Period.WEEK)
                        R.id.button_month -> timelineModel.setTimePeriod(TimelineViewModel.Period.MONTH)
                        R.id.button_year -> timelineModel.setTimePeriod(TimelineViewModel.Period.YEAR)
                    }
            }
    }

    @SuppressLint("SetTextI18n")
    private fun observe() {
        // title
        timelineModel.getTitle().observe(this) {
            title = it
        }

        // error
        timelineModel.getError().observe(this) {
            findViewById<TextView>(R.id.error).apply {
                visibility = View.VISIBLE
                text = HtmlCompat.fromHtml(it ?: "", HtmlCompat.FROM_HTML_MODE_LEGACY)
            }
            // disable toggle button, when there was an error
            menuItemToggle?.isEnabled = it == null
        }

        // progress bar
        timelineModel.isUpdating().observe(this) { isRefreshing ->
            refreshIndicator.visibility = if (isRefreshing) View.VISIBLE else View.GONE
            // disable toggle button, when data is updating
            menuItemToggle?.isEnabled = isRefreshing.not()
        }

        // populate the chart
        timelineModel.getRates().observe(this) {
            (timelineChart.adapter as ChartAdapter).entries = it?.entries?.toList()
        }

        // provider info
        timelineModel.getProvider().observe(this) {
            textProvider.text = if (it != null)
                HtmlCompat.fromHtml(
                    getString(R.string.data_provider, it),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            else
                null
        }

        // difference in percent
        timelineModel.getRatesDifferencePercent().observe(this) {
            textRateDifference.text = it?.toHumanReadableNumber(this, 2, true, "%")
            if (it != null) {
                textRateDifference.setTextColor(
                    if (it < 0)
                        MaterialColors.getColor(this, R.attr.colorError, null) // red
                    else
                        getColor(R.color.dollarBill) // green
                )
            }
        }

        // past rate
        timelineModel.getRatePast().observe(this) {
            val rate = it.first?.value
            if (rate != null) {
                textPastRateDate.text = it.first?.key?.format(formatter)
                textPastRateValue.text = combineValueAndSymbol(rate.value, rate.currency.symbol(), it.second)
                // only show the divider if this row is populated
                // highest chance of it populated is with this "past rate" data
                divider.visibility = View.VISIBLE
            } else {
                divider.visibility = View.GONE
            }
        }

        // current rate
        timelineModel.getRateCurrent().observe(this) {
            val rate = it.first?.value
            if (rate != null) {
                textCurrentRateDate.text = it.first?.key?.format(formatter)
                textCurrentRateValue.text = combineValueAndSymbol(rate.value, rate.currency.symbol(), it.second)
            }
        }

        // average rate
        timelineModel.getRatesAverage().observe(this) {
            populateStat(
                findViewById(R.id.stats_row_1),
                it.first?.currency?.symbol(),
                it.first?.value,
                null,
                it.second
            )
        }

        // min rate
        timelineModel.getRatesMin().observe(this) {
            val rate = it.first
            populateStat(
                findViewById(R.id.stats_row_2),
                rate?.currency?.symbol(),
                rate?.value,
                it.second,
                it.third
            )
        }

        // max rate
        timelineModel.getRatesMax().observe(this) {
            val rate = it.first
            populateStat(
                findViewById(R.id.stats_row_3),
                rate?.currency?.symbol(),
                rate?.value,
                it.second,
                it.third
            )
        }

    }

    private fun populateStat(parent: View, symbol: String?, value: Float?, date: LocalDate?, places: Int = 3) {
        // hide entire row when there's no data
        parent.visibility = if (symbol == null) View.GONE else View.VISIBLE
        // hide dotted line when there's no date
        parent.findViewById<View>(R.id.dotted_line).visibility = if (date == null) View.GONE else View.VISIBLE
        if (value != null)
            parent.findViewById<TextView>(R.id.text2).text = combineValueAndSymbol(value, symbol, places)
        parent.findViewById<TextView>(R.id.text3).text = date?.format(formatter)
    }

    private fun combineValueAndSymbol(
        value: Float,
        symbol: String?,
        decimalPlaces: Int
    ): SpannableStringBuilder {
        return if (hasAppendedCurrencySymbol(this))
            SpannableStringBuilder()
                .bold {
                    append(
                        value.toHumanReadableNumber(
                            this@TimelineActivity,
                            decimalPlaces = decimalPlaces
                        )
                    )
                }
                .append(" " + (symbol ?: ""))
        else
            SpannableStringBuilder()
                .append((symbol ?: "") + " ")
                .bold {
                    append(
                        value.toHumanReadableNumber(
                            this@TimelineActivity,
                            decimalPlaces = decimalPlaces
                        )
                    )
                }
    }

    private fun prepareFoldableLayoutChanges() {
        lifecycleScope.launch(Dispatchers.Main) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                WindowInfoTracker.getOrCreate(this@TimelineActivity)
                    .windowLayoutInfo(this@TimelineActivity)
                    .collect { newLayoutInfo ->
                        newLayoutInfo.displayFeatures.filterIsInstance(FoldingFeature::class.java)
                            .firstOrNull ()?.let { foldingFeature ->
                                // portrait
                                if (foldingFeature.orientation == FoldingFeature.Orientation.VERTICAL) {
                                    if (foldingFeature.state == FoldingFeature.State.HALF_OPENED)
                                        findViewById<LinearLayout>(R.id.timeline_root).orientation = LinearLayout.HORIZONTAL
                                    else
                                        findViewById<LinearLayout>(R.id.timeline_root).orientation = LinearLayout.VERTICAL
                                }
                                // landscape
                                else {
                                    if (foldingFeature.state == FoldingFeature.State.FLAT || foldingFeature.state == FoldingFeature.State.HALF_OPENED)
                                        findViewById<LinearLayout>(R.id.timeline_root).orientation = LinearLayout.VERTICAL
                                    else
                                        findViewById<LinearLayout>(R.id.timeline_root).orientation = LinearLayout.HORIZONTAL
                                }
                            }
                    }
            }
        }
    }

}
