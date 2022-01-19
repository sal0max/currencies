package de.salomax.currencies.view.timeline

import android.annotation.SuppressLint
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.robinhood.spark.SparkView
import de.salomax.currencies.R
import de.salomax.currencies.model.Currency
import de.salomax.currencies.util.dpToPx
import de.salomax.currencies.util.toHumanReadableNumber
import de.salomax.currencies.view.BaseActivity
import de.salomax.currencies.viewmodel.timeline.TimelineViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DecimalStyle
import java.time.format.FormatStyle

class TimelineActivity : BaseActivity() {

    // extras
    private lateinit var currencyFrom: Currency
    private lateinit var currencyTo: Currency

    //
    private val formatter = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.MEDIUM)
        .withDecimalStyle(DecimalStyle.ofDefaultLocale())

    private lateinit var timelineModel: TimelineViewModel

    // views
    private lateinit var refreshIndicator: LinearProgressIndicator
    private lateinit var timelineChart: SparkView
    private lateinit var textRateDifference: TextView
    private lateinit var divider: View

    private lateinit var textPastRateDate: TextView
    private lateinit var textPastRateSymbol: TextView
    private lateinit var textPastRateValue: TextView

    private lateinit var textCurrentRateDate: TextView
    private lateinit var textCurrentRateSymbol: TextView
    private lateinit var textCurrentRateValue: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // general layout
        setContentView(R.layout.activity_timeline)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        // what currencies to convert
        this.currencyFrom = intent.getSerializableExtra("ARG_FROM")?.let { it as Currency } ?: Currency.EUR
        this.currencyTo = intent.getSerializableExtra("ARG_TO")?.let { it as Currency } ?: Currency.USD
        title = HtmlCompat.fromHtml(
            getString(R.string.activity_timeline_title, currencyFrom, currencyTo),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        // model
        this.timelineModel = ViewModelProvider(
            this,
            TimelineViewModel.Factory(this.application, currencyFrom, currencyTo)
        ).get(TimelineViewModel::class.java)

        // views
        findViews()

        // configure timeline view
        initChartView()

        // listeners & stuff
        setListeners()

        // heavy lifting
        observe()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun findViews() {
        this.refreshIndicator = findViewById(R.id.refreshIndicator)
        this.timelineChart = findViewById(R.id.timeline_chart)
        this.textRateDifference = findViewById(R.id.text_rate_difference_percent)
        this.divider = findViewById(R.id.divider)

        this.textPastRateDate = findViewById(R.id.text_date_past)
        this.textPastRateSymbol = findViewById(R.id.text_symbol_past)
        this.textPastRateValue = findViewById(R.id.text_rate_past)

        this.textCurrentRateDate = findViewById(R.id.text_date_current)
        this.textCurrentRateSymbol = findViewById(R.id.text_symbol_current)
        this.textCurrentRateValue = findViewById(R.id.text_rate_current)
    }

    private fun initChartView() {
        timelineChart.apply {
            // dashed base line
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
        // error
        timelineModel.getError().observe(this, {
            findViewById<TextView>(R.id.error).apply {
                visibility = View.VISIBLE
                text = it
            }
        })

        // progress bar
        timelineModel.isUpdating().observe(this, { isRefreshing ->
            refreshIndicator.visibility = if (isRefreshing) View.VISIBLE else View.GONE
        })

        // populate the chart
        timelineModel.getRates().observe(this, {
            (timelineChart.adapter as ChartAdapter).entries = it?.entries?.toList()
        })

        // difference in percent
        timelineModel.getRatesDifferencePercent().observe(this, {
            textRateDifference.text = it?.toHumanReadableNumber(2, true, "%")
            if (it != null) {
                textRateDifference.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        if (it < 0) android.R.color.holo_red_light
                        else R.color.dollarBill
                    )
                )
            }
        })

        // past rate
        timelineModel.getRatePast().observe(this, {
            val rate = it?.value
            if (rate != null) {
                textPastRateDate.text = it.key.format(formatter)
                textPastRateSymbol.text = rate.currency.symbol()
                textPastRateValue.text = rate.value.toHumanReadableNumber(3)
                // only show the divider if this row is populated
                // highest chance of it populated is with this "past rate" data
                divider.visibility = View.VISIBLE
            } else {
                divider.visibility = View.GONE
            }
        })

        // current rate
        timelineModel.getRateCurrent().observe(this, {
            val rate = it?.value
            if (rate != null) {
                textCurrentRateDate.text = it.key.format(formatter)
                textCurrentRateSymbol.text = rate.currency.symbol()
                textCurrentRateValue.text = rate.value.toHumanReadableNumber(3)
            }
        })

        // average rate
        timelineModel.getRatesAverage().observe(this, {
            populateStat(
                findViewById(R.id.stats_row_1),
                getString(R.string.rate_average),
                it?.currency?.symbol(),
                it?.value,
                null
            )
        })

        // min rate
        timelineModel.getRatesMin().observe(this, {
            val rate = it.first
            populateStat(
                findViewById(R.id.stats_row_2),
                getString(R.string.rate_min),
                rate?.currency?.symbol(),
                rate?.value,
                it.second
            )
        })

        // max rate
        timelineModel.getRatesMax().observe(this, {
            val rate = it.first
            populateStat(
                findViewById(R.id.stats_row_3),
                getString(R.string.rate_max),
                rate?.currency?.symbol(),
                rate?.value,
                it.second
            )
        })

    }

    private fun populateStat(parent: View, title: String?, symbol: String?, value: Float?, date: LocalDate?) {
        // hide entire row when there's no data
        parent.visibility = if (symbol == null) View.GONE else View.VISIBLE
        // hide dotted line when there's no date
        parent.findViewById<View>(R.id.dotted_line).visibility = if (date == null) View.GONE else View.VISIBLE

        parent.findViewById<TextView>(R.id.text).text = title
        parent.findViewById<TextView>(R.id.text2).text = symbol
        parent.findViewById<TextView>(R.id.text3).text = value?.toHumanReadableNumber(3)
        parent.findViewById<TextView>(R.id.text4).text = date?.format(formatter)
    }

}
