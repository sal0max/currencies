package de.salomax.currencies.view.main

import android.content.*
import android.graphics.Color
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.text.HtmlCompat
import androidx.core.view.doOnLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import com.google.android.material.color.MaterialColors
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import de.salomax.currencies.R
import de.salomax.currencies.model.Rate
import de.salomax.currencies.util.getDecimalSeparator
import de.salomax.currencies.util.getLocale
import de.salomax.currencies.util.toHumanReadableNumber
import de.salomax.currencies.util.toNumber
import de.salomax.currencies.view.BaseActivity
import de.salomax.currencies.view.main.spinner.SearchableSpinner
import de.salomax.currencies.view.preference.PreferenceActivity
import de.salomax.currencies.view.timeline.TimelineActivity
import de.salomax.currencies.viewmodel.main.MainViewModel
import de.salomax.currencies.viewmodel.preference.PreferenceViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
        this.viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        this.preferenceModel = ViewModelProvider(this)[PreferenceViewModel::class.java]

        // views
        this.refreshIndicator = findViewById(R.id.refreshIndicator)
        this.swipeRefresh = findViewById(R.id.swipeRefresh)
        this.tvCalculations = findViewById(R.id.textCalculations)
        this.tvFrom = findViewById(R.id.textFrom)
        this.tvTo = findViewById(R.id.textTo)
        this.spinnerFrom = findViewById(R.id.spinnerFrom)
        this.spinnerTo = findViewById(R.id.spinnerTo)
        this.tvDate = findViewById(R.id.textRefreshed)
        this.tvFee = findViewById(R.id.textFee)

        // swipe-to-refresh: color scheme (not accessible in xml)
        swipeRefresh.setColorSchemeColors(MaterialColors.getColor(this, R.attr.colorOnPrimary, null))
        swipeRefresh.setProgressBackgroundColorSchemeColor(MaterialColors.getColor(this, R.attr.colorPrimary, null))

        // listeners & stuff
        setListeners()

        // heavy lifting
        observe()

        // foldable devices
        prepareFoldableLayoutChanges()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
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
            R.id.date_picker -> {
                // allow historical rates back until 2010-01-01, as every API at least provides a subset of rates since then
                val startDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    .apply { this.set(2010, Calendar.JANUARY, 1) }
                    .timeInMillis

                // load all the views
                val layout = layoutInflater.inflate(R.layout.main_dialog_historical_rates, null)
                val toggle: SwitchMaterial = layout.findViewById(R.id.toggle)
                val datePicker: DatePicker = layout.findViewById(R.id.date_picker)
                val border: View = layout.findViewById(R.id.border)

                val historicalDate = viewModel.getHistoricalDate()

                // enables/disables the date picker and the border on top of it
                fun showDatePicker(show: Boolean) {
                    datePicker.visibility = if (show) View.VISIBLE else View.GONE
                    border.visibility = if (show) View.VISIBLE else View.GONE
                }
                // initial dialog state
                showDatePicker(historicalDate != null)
                // configure the date picker
                datePicker.apply {
                    minDate = startDate
                    maxDate = Calendar.getInstance().timeInMillis
                    firstDayOfWeek = Calendar.getInstance().firstDayOfWeek
                    historicalDate?.let {
                        updateDate(it.year, it.monthValue - 1, it.dayOfMonth)
                    }
                }
                // configure the toggle button
                toggle.apply {
                    setOnCheckedChangeListener { _, enabled -> showDatePicker(enabled) }
                    isChecked = historicalDate != null
                }
                // finally, build the dialog and show it
                AlertDialog.Builder(this)
                    .setTitle(R.string.historical_rates_dialog_title)
                    .setView(layout)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        viewModel.setHistoricalDate(
                            // use historical
                            if (toggle.isChecked) LocalDate.of(
                                datePicker.year,
                                datePicker.month + 1,
                                datePicker.dayOfMonth
                            )
                            // use current
                            else null
                        )
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .create()
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        when (v.id) {
            R.id.textFrom -> {
                // copy
                menu.add(0, 0, 0, android.R.string.copy)
                // paste
                val paste = menu.add(0, 1, 0, android.R.string.paste)
                // only show "paste" when applicable
                val clipboard: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipboardContent = clipboard.primaryClip?.getItemAt(0)?.text?.toNumber()
                paste.isVisible = (clipboard.hasPrimaryClip()
                        && clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true
                        &&  clipboardContent != null)
            }
            R.id.textTo -> {
                menu.add(0, 2, 0, android.R.string.copy)
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            0 -> { // copy "from"
                val copyText = findViewById<TextView>(R.id.textFrom).text
                copyToClipboard(copyText.toString())
            }
            1 -> { // paste "from"
                val clipboard: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                // no need to check if clipboard is filled -> menu item only shown when it is
                clipboard.primaryClip?.getItemAt(0)?.text?.toNumber()?.let {
                    viewModel.paste(it)
                }
            }
            2 -> { // copy "to"
                val copyText = findViewById<TextView>(R.id.textTo).text
                copyToClipboard(copyText.toString())
            }
        }
        return true
    }

    private fun setListeners() {
        // long click on delete
        arrayOf<View>(findViewById(R.id.keypad), findViewById(R.id.keypad_extended)).forEach {
            it.findViewById<AppCompatImageButton>(R.id.btn_delete).setOnLongClickListener {
                viewModel.clear()
                true
            }
        }

        // long click on input "from"
        registerForContextMenu(findViewById<LinearLayout>(R.id.textFrom))
        // long click on input "to"
        registerForContextMenu(findViewById<LinearLayout>(R.id.textTo))

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
        HtmlCompat.fromHtml(
            getString(R.string.copied_to_clipboard, copyText),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        ).let {
            Snackbar.make(this, tvCalculations, it, Snackbar.LENGTH_SHORT)
                .placeOnTop()
                .setBackgroundTint(MaterialColors.getColor(this, R.attr.colorPrimary, null))
                .setTextColor(MaterialColors.getColor(this, R.attr.colorOnPrimary, null))
                .show()
        }
    }

    private fun observe() {
        //exchange rates changed
        viewModel.getExchangeRates().observe(this) {
            // date
            it?.let {
                val date = it.date
                val dateString = date
                    ?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(getLocale(this)))
                    ?.replace("\u200F", "") // remove rtl-mark (fixes broken arab date)
                val providerString = it.provider?.getName()

                // show rate age and rate source
                tvDate.text =
                    if (dateString != null && providerString != null)
                        HtmlCompat.fromHtml(
                            getString(
                                if (viewModel.getHistoricalDate() != null)
                                    R.string.rates_date_historical
                                else
                                    R.string.rates_date_latest,
                                dateString,
                                providerString
                            ),
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                    else
                        null

                // paint text in red in case the data is old (at least 3 days) or historical rates are enabled
                tvDate.setTextColor(
                    if (date?.isBefore(LocalDate.now().minusDays(3)) == true || viewModel.getHistoricalDate() != null)
                        MaterialColors.getColor(this, R.attr.colorError, null)
                    else
                        getTextColorSecondary()
                )

                // show little icon to indicate when historical rates are used
                findViewById<ImageView>(R.id.iconHistorical).visibility =
                    if (viewModel.getHistoricalDate() != null)
                        View.VISIBLE
                    else
                        View.GONE
            }
            // rates
            spinnerFrom.setRates(it?.rates)
            spinnerTo.setRates(it?.rates)
        }

        // something bad happened
        viewModel.getError().observe(this) {
            // error
            it?.let {
                Snackbar.make(this, tvCalculations, HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY), Snackbar.LENGTH_INDEFINITE) // show for 5s
                    .placeOnTop()
                    .setBackgroundTint(MaterialColors.getColor(this, R.attr.colorError, null))
                    .setTextColor(MaterialColors.getColor(this, R.attr.colorOnError, null))
                    .setActionTextColor(MaterialColors.getColor(this, R.attr.colorOnError, null))
                    .setAction(android.R.string.ok) { /* onClick dismisses, by default */ }
                    .setTextMaxLines(20)
                    .show()
            }
        }

        // rates are updating
        viewModel.isUpdating().observe(this) { isRefreshing ->
            refreshIndicator.visibility = if (isRefreshing) View.VISIBLE else View.GONE
            // disable manual refresh, while refreshing
            swipeRefresh.isEnabled = isRefreshing.not()
            menuItemRefresh?.isEnabled = isRefreshing.not()
        }

        // input changed
        viewModel.getCurrentBaseValueFormatted().observe(this) {
            tvFrom.text = it
        }
        viewModel.getResultFormatted().observe(this) {
            tvTo.text = it
        }
        viewModel.getCalculationInputFormatted().observe(this) {
            tvCalculations.text = it
        }

        // selected rates changed
        viewModel.getBaseCurrency().observe(this) { currency ->
            spinnerFrom.setSelection(currency)
            // conversion preview
            if (currency != null)
                // get rate from currency
                viewModel.getExchangeRates().value?.rates?.find { it.currency == currency }?.value
                    // give it to the adapter
                    ?.let { spinnerTo.setCurrentRate(Rate(currency, it)) }
        }
        viewModel.getDestinationCurrency().observe(this) { currency ->
            spinnerTo.setSelection(currency)
            // conversion preview
            if (currency != null)
                // get rate from currency
                viewModel.getExchangeRates().value?.rates?.find { it.currency == currency }?.value
                    // give it to the adapter
                    ?.let { spinnerFrom.setCurrentRate(Rate(currency, it)) }
        }

        // fee changed
        viewModel.isFeeEnabled().observe(this) {
            tvFee.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModel.getFee().observe(this) {
            tvFee.text = it.toHumanReadableNumber(this, showPositiveSign = true, suffix = "%")
            tvFee.setTextColor(
                if (it >= 0) MaterialColors.getColor(this, R.attr.colorError, null)
                else MaterialColors.getColor(this, R.attr.colorPrimary, null)
            )
        }

        viewModel.getCurrentBaseValueAsNumber().observe(this) {
            spinnerTo.setCurrentSum(it)
        }
        viewModel.getResultAsNumber().observe(this) {
            spinnerFrom.setCurrentSum(it)
        }

        viewModel.isExtendedKeypadEnabled.observe(this) { extendedEnabled ->
            val keypadRegular = findViewById<View>(R.id.keypad)
            val keypadExtended = findViewById<View>(R.id.keypad_extended)
            // activate the correct keypad
            keypadRegular.visibility = if (extendedEnabled) View.GONE else View.VISIBLE
            keypadExtended.visibility = if (extendedEnabled) View.VISIBLE else View.GONE
            // decimal button: use correct char for the current locale
            val separator = getDecimalSeparator(this)
            keypadExtended.findViewById<TextView>(R.id.btn_decimal).text = separator
            keypadRegular.findViewById<TextView>(R.id.btn_decimal).text = separator
        }
    }

    private fun getTextColorSecondary(): Int {
        val attrs = intArrayOf(android.R.attr.textColorSecondary)
        val a = theme.obtainStyledAttributes(R.style.AppTheme, attrs)
        val color = a.getColor(0, Color.TRANSPARENT)
        a.recycle()
        return color
    }

    /*
     * keyboard: number input
     */
    fun numberEvent(view: View) {
        viewModel.addNumber((view as AppCompatButton).text.toString())
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
        when ((view as AppCompatButton).text.toString()) {
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

    private fun prepareFoldableLayoutChanges() {
        lifecycleScope.launch(Dispatchers.Main) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                WindowInfoTracker.getOrCreate(this@MainActivity)
                    .windowLayoutInfo(this@MainActivity)
                    .collect { newLayoutInfo ->
                        newLayoutInfo.displayFeatures.filterIsInstance(FoldingFeature::class.java)
                            .firstOrNull ()?.let { foldingFeature ->
                                // flat
                                if (foldingFeature.state == FoldingFeature.State.FLAT) {
                                    // portrait
                                    if (resources.configuration.screenHeightDp >= resources.configuration.screenWidthDp)
                                        findViewById<LinearLayout>(R.id.main_root).orientation = LinearLayout.VERTICAL
                                    // landscape
                                    else
                                        findViewById<LinearLayout>(R.id.main_root).orientation = LinearLayout.HORIZONTAL
                                }
                                // half & portrait
                                else if (foldingFeature.orientation == FoldingFeature.Orientation.VERTICAL)
                                    findViewById<LinearLayout>(R.id.main_root).orientation = LinearLayout.HORIZONTAL
                                // half & landscape
                                else
                                    findViewById<LinearLayout>(R.id.main_root).orientation = LinearLayout.VERTICAL
                            }
                    }
            }
        }
    }

    /**
     * Show snackbar at the top instead of the bottom.
     * Also make it only as wide as the main display is. Looking way better in landscape mode.
     */
    private fun Snackbar.placeOnTop(): Snackbar {
        swipeRefresh.doOnLayout {
            val view = this.view
            val params = view.layoutParams as FrameLayout.LayoutParams
            params.gravity = Gravity.TOP
            // setting width
            params.width = it.measuredWidth - this.view.paddingStart - this.view.paddingEnd
            view.layoutParams = params
        }
        return this
    }

}
