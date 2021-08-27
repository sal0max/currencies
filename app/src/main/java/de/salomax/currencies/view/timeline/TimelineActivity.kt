package de.salomax.currencies.view.timeline

import android.annotation.SuppressLint
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.robinhood.spark.SparkView
import de.salomax.currencies.R
import de.salomax.currencies.util.prettyPrint
import de.salomax.currencies.util.prettyPrintPercent
import de.salomax.currencies.viewmodel.main.TimelineViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class TimelineActivity: AppCompatActivity() {

    private lateinit var timelineModel: TimelineViewModel

    private lateinit var argFrom: String
    private lateinit var argTo: String

    private lateinit var refreshIndicator: LinearProgressIndicator
    private lateinit var timelineChart: SparkView
    private lateinit var textDatePast: TextView
    private lateinit var textRatePast: TextView
    private lateinit var textDateCurrent: TextView
    private lateinit var textRateCurrent: TextView
    private lateinit var textRateDifference: TextView
    private lateinit var textRateAverage: TextView
    private lateinit var textRateMin: TextView
    private lateinit var textRateMax: TextView

    private val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // general layout
        setContentView(R.layout.activity_timeline)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        // what currencies to convert
        this.argFrom = intent.getStringExtra("ARG_FROM")!!
        this.argTo = intent.getStringExtra("ARG_TO")!!
        title = HtmlCompat.fromHtml(
            getString(R.string.activity_timeline_title, argFrom, argTo),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        // model
        this.timelineModel = ViewModelProvider(
            this,
            TimelineViewModel.Factory(this.application, argFrom, argTo)
        ).get(TimelineViewModel::class.java)

        // views
        this.refreshIndicator = findViewById(R.id.refreshIndicator)
        this.timelineChart = findViewById(R.id.timeline_chart)
        this.textDatePast = findViewById(R.id.text_date_past)
        this.textRatePast = findViewById(R.id.text_rate_past)
        this.textDateCurrent = findViewById(R.id.text_date_current)
        this.textRateCurrent = findViewById(R.id.text_rate_current)
        this.textRateDifference = findViewById(R.id.text_rate_difference_percent)
        this.textRateAverage = findViewById(R.id.text_rate_average)
        this.textRateMin = findViewById(R.id.text_rate_min)
        this.textRateMax = findViewById(R.id.text_rate_max)

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

    private fun initChartView() {
        timelineChart.apply {
            // dashed base line
            baseLinePaint = Paint().apply {
                setARGB(255, 127, 127, 127)
                strokeWidth = 3f
                style = Paint.Style.STROKE
                pathEffect = DashPathEffect(floatArrayOf(8f, 24f), 0f)
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
            // TODO
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
            textRateDifference.text = it?.prettyPrintPercent(this, 2)
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
            val rate = it?.value?.first()
            if (rate != null) {
                textRatePast.text = "${rate.getCurrencySymbol()} ${rate.value.prettyPrint(this, 2)}"
                textDatePast.text = it.key.format(formatter)
            }
        })

        // current rate
        timelineModel.getRateCurrent().observe(this, {
            val rate = it?.value?.first()
            if (rate != null) {
                textRateCurrent.text = "${rate.getCurrencySymbol()} ${rate.value.prettyPrint(this, 2)}"
                textDateCurrent.text = it.key.format(formatter)
            }
        })

        // average rate
        timelineModel.getRatesAverage().observe(this, {
            if (it == null) {
                textRateAverage.text = " ..."
            }
            else {
                textRateAverage.text = HtmlCompat.fromHtml(
                    getString(
                        R.string.rate_average_value,
                        it.getCurrencySymbol(),
                        it.value.prettyPrint(this, 2)
                    ), HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            }
        })

        // min rate
        timelineModel.getRatesMin().observe(this, {
            if (it?.first == null) {
                textRateMin.text = " ..."
            } else {
                textRateMin.text = HtmlCompat.fromHtml(
                    getString(
                        R.string.rate_min_value,
                        it.first?.getCurrencySymbol(),
                        it.first?.value?.prettyPrint(this, 2),
                        it.second?.format(formatter)
                    ), HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            }
        })

        // max rate
        timelineModel.getRatesMax().observe(this, {
            if (it?.first == null) {
                textRateMax.text = " ..."
            } else {
                textRateMax.text = HtmlCompat.fromHtml(
                    getString(
                        R.string.rate_max_value,
                        it.first?.getCurrencySymbol(),
                        it.first?.value?.prettyPrint(this, 2),
                        it.second?.format(formatter)
                    ), HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            }
        })

    }

}
