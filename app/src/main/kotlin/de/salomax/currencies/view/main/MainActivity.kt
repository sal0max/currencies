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
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import de.salomax.currencies.R
import de.salomax.currencies.util.prettyPrintPercent
import de.salomax.currencies.view.BaseActivity
import de.salomax.currencies.view.main.spinner.SearchableSpinner
import de.salomax.currencies.view.main.spinner.SearchableSpinnerAdapter
import de.salomax.currencies.view.preference.PreferenceActivity
import de.salomax.currencies.view.timeline.TimelineActivity
import de.salomax.currencies.viewmodel.main.CurrentInputViewModel
import de.salomax.currencies.viewmodel.main.ExchangeRatesViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class MainActivity : BaseActivity() {

    private lateinit var ratesModel: ExchangeRatesViewModel
    private lateinit var inputModel: CurrentInputViewModel

    private lateinit var refreshIndicator: LinearProgressIndicator
    private lateinit var tvCalculations: TextView
    private lateinit var tvFrom: TextView
    private lateinit var tvTo: TextView
    private lateinit var tvCurrencyFrom: TextView
    private lateinit var tvCurrencyTo: TextView
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
        this.ratesModel = ViewModelProvider(this).get(ExchangeRatesViewModel::class.java)
        this.inputModel = ViewModelProvider(this).get(CurrentInputViewModel::class.java)

        // views
        this.refreshIndicator = findViewById(R.id.refreshIndicator)
        this.tvCalculations = findViewById(R.id.textCalculations)
        this.tvFrom = findViewById(R.id.textFrom)
        this.tvTo = findViewById(R.id.textTo)
        this.tvCurrencyFrom = findViewById(R.id.currencyFrom)
        this.tvCurrencyTo = findViewById(R.id.currencyTo)
        this.spinnerFrom = findViewById(R.id.spinnerFrom)
        this.spinnerTo = findViewById(R.id.spinnerTo)
        this.tvDate = findViewById(R.id.textRefreshed)
        this.tvFee = findViewById(R.id.textFee)

        // listeners & stuff
        setListeners()

        // heavy lifting
        observe()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settings -> {
                startActivity(Intent(this, PreferenceActivity().javaClass))
                true
            }
            R.id.refresh -> {
                ratesModel.forceUpdateExchangeRate()
                true
            }
            R.id.timeline -> {
                val from = inputModel.getLastRateFrom()
                val to = inputModel.getLastRateTo()
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
            inputModel.clear()
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
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                inputModel.setCurrencyFrom(
                    (parent?.adapter as SearchableSpinnerAdapter).getItem(position)
                )
            }
        }
        spinnerTo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                inputModel.setCurrencyTo(
                    (parent?.adapter as SearchableSpinnerAdapter).getItem(position)
                )
            }
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
        ratesModel.exchangeRates.observe(this, {
            // date
            it?.let {
                val date = it.date
                val dateString = date?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))

                tvDate.text = getString(R.string.last_updated, dateString)
                // today
                if (date?.isEqual(LocalDate.now()) == true)
                    tvDate.append(" (${getString(R.string.today)})")
                // yesterday
                else if (date?.isEqual(LocalDate.now().minusDays(1)) == true)
                    tvDate.append(" (${getString(R.string.yesterday)})")

                // paint text in red in case the data is old
                tvDate.setTextColor(
                    if (date?.isBefore(LocalDate.now().minusDays(3)) == true) getColor(android.R.color.holo_red_light)
                    else getTextColorSecondary()
                )
            }
            // rates
            spinnerFrom.setRates(it?.rates)
            spinnerTo.setRates(it?.rates)

            // restore state
            inputModel.getLastRateFrom()?.let { last ->
                (spinnerFrom.adapter as? SearchableSpinnerAdapter)?.getPosition(last)?.let { position ->
                    spinnerFrom.setSelection(position)
                }
            }
            inputModel.getLastRateTo()?.let { last ->
                (spinnerTo.adapter as? SearchableSpinnerAdapter)?.getPosition(last)?.let { position ->
                    spinnerTo.setSelection(position)
                }
            }
        })
        ratesModel.getStarredCurrencies().observe(this, { stars ->
            // starred rates
            stars.let {
                spinnerFrom.setStars(it)
                spinnerTo.setStars(it)
            }

        })
        ratesModel.getError().observe(this, {
            // error
            it?.let {
                Snackbar.make(tvCalculations, it, 5000) // show for 5s
                    .setBackgroundTint(getColor(android.R.color.holo_red_light))
                    .setTextColor(getColor(android.R.color.white))
                    .show()
            }
        })
        ratesModel.isUpdating().observe(this, { isRefreshing ->
            refreshIndicator.visibility = if (isRefreshing) View.VISIBLE else View.GONE
        })

        // input changed
        inputModel.getCurrentInput().observe(this, {
            tvFrom.text = it
        })
        inputModel.getCurrentInputConverted().observe(this, {
            tvTo.text = it
        })
        inputModel.getCalculationInput().observe(this, {
            tvCalculations.text = it
        })
        inputModel.getCurrencyFrom().observe(this, {
            tvCurrencyFrom.text = it
        })
        inputModel.getCurrencyTo().observe(this, {
            tvCurrencyTo.text = it
        })

        // fee changed
        inputModel.getFeeEnabled().observe(this, {
            tvFee.visibility = if (it) View.VISIBLE else View.GONE
        })
        inputModel.getFee().observe(this, {
            tvFee.text = it.prettyPrintPercent(this)
            tvFee.setTextColor(
                if (it >= 0) getColor(android.R.color.holo_red_light)
                else getColor(R.color.dollarBill)
            )
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
        inputModel.addNumber((view as Button).text.toString())
    }

    /*
     * keyboard: add decimal point
     */
    fun decimalEvent(@Suppress("UNUSED_PARAMETER") view: View) {
        inputModel.addDecimal()
    }

    /*
     * keyboard: delete
     */
    fun deleteEvent(@Suppress("UNUSED_PARAMETER") view: View) {
        inputModel.delete()
    }

    /*
     * keyboard: do some calculations
     */
    fun calculationEvent(view: View) {
        when((view as Button).text.toString()) {
            "+" -> inputModel.addition()
            "−" -> inputModel.subtraction()
            "×" -> inputModel.multiplication()
            "÷" -> inputModel.division()
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
