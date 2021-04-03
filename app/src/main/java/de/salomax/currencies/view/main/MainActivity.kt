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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import de.salomax.currencies.R
import de.salomax.currencies.util.humanReadableFee
import de.salomax.currencies.view.preference.PreferenceActivity
import de.salomax.currencies.viewmodel.main.CurrentInputViewModel
import de.salomax.currencies.viewmodel.main.ExchangeRatesViewModel
import de.salomax.currencies.view.main.searchablespinner.SearchableSpinner
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class MainActivity : AppCompatActivity() {

    private lateinit var ratesModel: ExchangeRatesViewModel
    private lateinit var inputModel: CurrentInputViewModel

    private lateinit var refreshIndicator: LinearProgressIndicator
    private lateinit var tvCalculations: TextView
    private lateinit var tvFrom: TextView
    private lateinit var tvTo: TextView
    private lateinit var tvCurrencyFrom: TextView
    private lateinit var tvCurrencyTo: TextView
    private lateinit var spinnerFrom: SearchableSpinner
    private lateinit var spinnerTo: Spinner
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
                    (parent?.adapter as SpinnerAdapter).getItem(position)
                )
            }
        }
        spinnerTo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                inputModel.setCurrencyTo(
                    (parent?.adapter as SpinnerAdapter).getItem(position)
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
            .show()
    }

    private fun observe() {
        //exchange rates changed
        ratesModel.getExchangeRate().observe(this, {
            // date
            it?.let {
                val date = it.date?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
                tvDate.text = getString(R.string.last_updated, date)
            }
            // rates
            spinnerFrom.adapter = it?.rates?.let { rates ->
                SpinnerAdapter(this, android.R.layout.simple_spinner_item, rates)
            }
            spinnerTo.adapter = it?.rates?.let { rates ->
                SpinnerAdapter(this, android.R.layout.simple_spinner_item, rates)
            }
            // restore state
            inputModel.getLastRateFrom()?.let { last ->
                (spinnerFrom.adapter as? SpinnerAdapter)?.getPosition(last)?.let { position ->
                    spinnerFrom.setSelection(position)
                }
            }
            inputModel.getLastRateTo()?.let { last ->
                (spinnerTo.adapter as? SpinnerAdapter)?.getPosition(last)?.let { position ->
                    spinnerTo.setSelection(position)
                }
            }
        })
        ratesModel.getError().observe(this, {
            // error
            it?.let {
                Snackbar.make(tvCalculations, it, Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getColor(android.R.color.holo_red_light))
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
            tvFee.text = it.humanReadableFee(this)
            tvFee.setTextColor(
                if (it >= 0) getColor(android.R.color.holo_red_light)
                else getColor(R.color.dollarBill)
            )
        })
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
