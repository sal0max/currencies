package de.salomax.currencies.view.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import de.salomax.currencies.R
import de.salomax.currencies.model.Rate
import de.salomax.currencies.util.toHumanReadableNumber
import de.salomax.currencies.view.BaseActivity
import de.salomax.currencies.view.main.spinner.SearchableSpinner
import de.salomax.currencies.view.preference.PreferenceActivity
import de.salomax.currencies.view.timeline.TimelineActivity
import de.salomax.currencies.viewmodel.main.MainViewModel
import de.salomax.currencies.viewmodel.preference.PreferenceViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class MainActivity : BaseActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var preferenceModel: PreferenceViewModel

    private lateinit var refreshIndicator: LinearProgressIndicator
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private var menuItemRefresh: MenuItem? = null

    private lateinit var tvCalculations: TextView
    private lateinit var tvFrom: TextView
    private lateinit var tvTo: TextView
    private lateinit var tvCurrencySymbolFrom: TextView
    private lateinit var tvCurrencySymbolTo: TextView
    private lateinit var spinnerFrom: SearchableSpinner
    private lateinit var spinnerTo: SearchableSpinner
    private lateinit var tvDate: TextView
    private lateinit var tvFee: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // general layout
        setContentView(R.layout.activity_main)
        title = null

        // model
        this.viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        this.preferenceModel = ViewModelProvider(this).get(PreferenceViewModel::class.java)

        // views
        this.refreshIndicator = findViewById(R.id.refreshIndicator)
        this.swipeRefresh = findViewById(R.id.swipeRefresh)
        this.tvCalculations = findViewById(R.id.textCalculations)
        this.tvFrom = findViewById(R.id.textFrom)
        this.tvTo = findViewById(R.id.textTo)
        this.tvCurrencySymbolFrom = findViewById(R.id.currencyFrom)
        this.tvCurrencySymbolTo = findViewById(R.id.currencyTo)
        this.spinnerFrom = findViewById(R.id.spinnerFrom)
        this.spinnerTo = findViewById(R.id.spinnerTo)
        this.tvDate = findViewById(R.id.textRefreshed)
        this.tvFee = findViewById(R.id.textFee)

        // swipe-to-refresh: color scheme (not accessible in xml)
        swipeRefresh.setColorSchemeResources(R.color.blackOlive)
        swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.dollarBill)

        // listeners & stuff
        setListeners()

        // heavy lifting
        observe()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        this.menuItemRefresh = menu.findItem(R.id.refresh)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settings -> {
                startActivity(Intent(this, PreferenceActivity().javaClass))
                true
            }
            R.id.refresh -> {
                viewModel.forceUpdateExchangeRate()
                true
            }
            R.id.timeline -> {
                val from = viewModel.getBaseCurrency().value
                val to = viewModel.getDestinationCurrency().value
                if (from != null && to != null) {
                    startActivity(
                        Intent(Intent(this, TimelineActivity().javaClass)).apply {
                            putExtra("ARG_FROM", from)
                            putExtra("ARG_TO", to)
                        }
                    )
                    true
                } else {
                    false
                }
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setListeners() {
        // long click on delete
        findViewById<ImageButton>(R.id.btn_delete).setOnLongClickListener {
            viewModel.clear()
            true
        }

        // long click on input "from"
        findViewById<LinearLayout>(R.id.clickFrom).setOnLongClickListener {
            val copyText = "${it.findViewById<TextView>(R.id.currencyFrom).text} ${it.findViewById<TextView>(R.id.textFrom).text}"
            copyToClipboard(copyText)
            true
        }
        // long click on input "to"
        findViewById<LinearLayout>(R.id.clickTo).setOnLongClickListener {
            val copyText = "${it.findViewById<TextView>(R.id.currencyTo).text} ${it.findViewById<TextView>(R.id.textTo).text}"
            copyToClipboard(copyText)
            true
        }

        // spinners: listen for changes
        spinnerFrom.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position != -1 && parent?.adapter?.isEmpty != true) {
                    val rate = parent?.adapter?.getItem(position) as Rate?
                    rate?.let { viewModel.setBaseCurrency(it.currency) }
                }
            }
        }
        spinnerTo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position != -1 && parent?.adapter?.isEmpty != true) {
                    val rate = parent?.adapter?.getItem(position) as Rate?
                    rate?.let { viewModel.setDestinationCurrency(it.currency) }
                }
            }
        }

        // swipe to refresh
        swipeRefresh.setOnRefreshListener {
            // update
            viewModel.forceUpdateExchangeRate()
            swipeRefresh.isRefreshing = false
        }
    }

    private fun copyToClipboard(copyText: String) {
        // copy
        val clipboard: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(null, copyText))
        // notify
        Snackbar.make(
            tvCalculations,
            HtmlCompat.fromHtml(
                getString(R.string.copied_to_clipboard, copyText),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            ),
            Snackbar.LENGTH_SHORT
        )
            .setBackgroundTint(getColor(R.color.colorAccent))
            .setTextColor(getColor(R.color.colorTextOnAccent))
            .show()
    }

    private fun observe() {
        //exchange rates changed
        viewModel.getExchangeRates().observe(this, {
            // date
            it?.let {
                val date = it.date
                val dateString = date
                    ?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
                    ?.replace("\u200F", "") // remove rtl-mark (fixes broken arab date)

                tvDate.text = getString(R.string.last_updated, dateString)
                // today
                if (date?.isEqual(LocalDate.now()) == true)
                    tvDate.append(" (${getString(R.string.today)})")
                // yesterday
                else if (date?.isEqual(LocalDate.now().minusDays(1)) == true)
                    tvDate.append(" (${getString(R.string.yesterday)})")

                // paint text in red in case the data is old
                tvDate.setTextColor(
                    if (date?.isBefore(LocalDate.now().minusDays(3)) == true)
                        getColor(android.R.color.holo_red_light)
                    else
                        getTextColorSecondary()
                )
            }
            // rates
            spinnerFrom.setRates(it?.rates)
            spinnerTo.setRates(it?.rates)
        })

        // something bad happened
        viewModel.getError().observe(this, {
            // error
            it?.let {
                Snackbar.make(tvCalculations, it, 5000) // show for 5s
                    .setBackgroundTint(getColor(android.R.color.holo_red_light))
                    .setTextColor(getColor(android.R.color.white))
                    .show()
            }
        })

        // rates are updating
        viewModel.isUpdating().observe(this, { isRefreshing ->
            refreshIndicator.visibility = if (isRefreshing) View.VISIBLE else View.GONE
            // disable manual refresh, while refreshing
            swipeRefresh.isEnabled = isRefreshing.not()
            menuItemRefresh?.isEnabled = isRefreshing.not()
        })

        // input changed
        viewModel.getCurrentBaseValueFormatted().observe(this, {
            tvFrom.text = it
        })
        viewModel.getResultFormatted().observe(this, {
            tvTo.text = it
        })
        viewModel.getCalculationInputFormatted().observe(this, {
            tvCalculations.text = it
        })

        // selected rates changed
        viewModel.getBaseCurrency().observe(this, { currency ->
            tvCurrencySymbolFrom.text = currency?.symbol()
            spinnerFrom.setSelection(currency)
            // conversion preview
            if (currency != null)
                // get rate from currency
                viewModel.getExchangeRates().value?.rates?.find { it.currency == currency }?.value
                    // give it to the adapter
                    ?.let { spinnerTo.setCurrentRate(Rate(currency, it)) }
        })
        viewModel.getDestinationCurrency().observe(this, { currency ->
            tvCurrencySymbolTo.text = currency?.symbol()
            spinnerTo.setSelection(currency)
            // conversion preview
            if (currency != null)
                // get rate from currency
                viewModel.getExchangeRates().value?.rates?.find { it.currency == currency }?.value
                    // give it to the adapter
                    ?.let { spinnerFrom.setCurrentRate(Rate(currency, it)) }
        })

        // fee changed
        viewModel.isFeeEnabled().observe(this, {
            tvFee.visibility = if (it) View.VISIBLE else View.GONE
        })
        viewModel.getFee().observe(this, {
            tvFee.text = it.toHumanReadableNumber(this, showPositiveSign = true, suffix = "%")
            tvFee.setTextColor(
                if (it >= 0) getColor(android.R.color.holo_red_light)
                else getColor(R.color.dollarBill)
            )
        })

        viewModel.getCurrentBaseValueAsNumber().observe(this, {
            spinnerTo.setCurrentSum(it)
        })
        viewModel.getResultAsNumber().observe(this, {
            spinnerFrom.setCurrentSum(it)
        })
    }

    private fun getTextColorSecondary(): Int {
        val attrs = intArrayOf(android.R.attr.textColorSecondary)
        val a = theme.obtainStyledAttributes(R.style.AppTheme, attrs)
        val color = a.getColor(0, getColor(R.color.colorAccent))
        a.recycle()
        return color
    }

    /*
     * keyboard: number input
     */
    fun numberEvent(view: View) {
        viewModel.addNumber((view as Button).text.toString())
    }

    /*
     * keyboard: add decimal point
     */
    fun decimalEvent(@Suppress("UNUSED_PARAMETER") view: View) {
        viewModel.addDecimal()
    }

    /*
     * keyboard: delete
     */
    fun deleteEvent(@Suppress("UNUSED_PARAMETER") view: View) {
        viewModel.delete()
    }

    /*
     * keyboard: do some calculations
     */
    fun calculationEvent(view: View) {
        when((view as Button).text.toString()) {
            "+" -> viewModel.addition()
            "−" -> viewModel.subtraction()
            "×" -> viewModel.multiplication()
            "÷" -> viewModel.division()
        }
    }

    /*
     * swap currencies
     */
    fun toggleEvent(@Suppress("UNUSED_PARAMETER") view: View) {
        val from = spinnerFrom.selectedItemPosition
        val to = spinnerTo.selectedItemPosition
        spinnerFrom.setSelection(to)
        spinnerTo.setSelection(from)
    }

}
