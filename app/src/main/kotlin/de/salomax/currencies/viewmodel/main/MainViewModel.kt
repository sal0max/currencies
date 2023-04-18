package de.salomax.currencies.viewmodel.main

import android.app.Application
import android.text.SpannableStringBuilder
import androidx.core.text.bold
import androidx.lifecycle.*
import de.salomax.currencies.model.Currency
import de.salomax.currencies.model.ExchangeRates
import de.salomax.currencies.repository.Database
import de.salomax.currencies.repository.ExchangeRatesRepository
import de.salomax.currencies.util.combineWith
import de.salomax.currencies.util.getDecimalSeparator
import de.salomax.currencies.util.hasAppendedCurrencySymbol
import de.salomax.currencies.util.toHumanReadableNumber
import org.mariuszgromada.math.mxparser.Expression
import org.mariuszgromada.math.mxparser.License
import java.text.Collator
import java.time.LocalDate
import java.time.ZoneId

@Suppress("unused", "MemberVisibilityCanBePrivate")
class MainViewModel(val app: Application, onlyCache: Boolean = false) : AndroidViewModel(app) {

    constructor(app: Application) : this(app, false)

    class Factory(val app: Application, val onlyCache: Boolean = false) :
        ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(app, onlyCache) as T
        }
    }

    private var repository: ExchangeRatesRepository = ExchangeRatesRepository(app)

    // repository data
    private var dbLiveItems: LiveData<ExchangeRates?>
    private var exchangeRates: LiveData<ExchangeRates?>
    private val starredLiveItems: LiveData<Set<Currency>>
    private val onlyShowStarred: LiveData<Boolean>
    private val liveError = repository.getError()

    // ui
    private var isUpdating: LiveData<Boolean> = repository.isUpdating()
    val isExtendedKeypadEnabled: LiveData<Boolean> = Database(app).isExtendedKeypadEnabled()


    // number input
    private val currentBaseValueText = MutableLiveData("0")
    private val currentCalculationValueText = MutableLiveData<String?>()

    // currency selection
    private val currentBaseCurrency: LiveData<Currency?>
    private val currentDestinationCurrency: LiveData<Currency?>

    // fee
    private val isFeeEnabled: LiveData<Boolean>
    private val fee: LiveData<Float>

    /*
     * repository data =============================================================================
     */

    init {
        // MathParser.org-mXparser TOS
        License.iConfirmNonCommercialUse("Salomax")

        // only update if data is old: https://github.com/Formicka/exchangerate.host
        // "Rates are updated around midnight UTC every working day."
        val currentDate = LocalDate.now(ZoneId.of("UTC"))
        val cachedDate = Database(app).getDate()
        val historicalDate = Database(app).getHistoricalDate()

        dbLiveItems = when {
            // force-use cache
            onlyCache -> Database(app).getExchangeRates()
            // first run: fetch data
            cachedDate == null -> repository.getExchangeRates()
            // historical rates in use...
            historicalDate != null -> {
                // ...and already cached
                if (historicalDate == cachedDate) Database(app).getExchangeRates()
                // ...and not cached
                else repository.getExchangeRates()
            }
            // fetch if stored date is before the current date
            cachedDate.isBefore(currentDate) -> repository.getExchangeRates()
            // else just use the cached value
            else -> Database(app).getExchangeRates()
        }

        starredLiveItems = Database(app).getStarredCurrencies()
        onlyShowStarred = Database(app).isFilterStarredEnabled()

        isFeeEnabled = Database(getApplication()).isFeeEnabled()
        fee = Database(getApplication()).getFee()

        //
        exchangeRates = object : MediatorLiveData<ExchangeRates?>() {
            var liveItems: ExchangeRates? = null

            init {
                addSource(dbLiveItems) { liveItems = it; calc() }
                addSource(starredLiveItems) { calc() }
                addSource(onlyShowStarred) { calc() }
            }

            private fun calc() {
                liveItems?.let { rates ->
                    this.value = rates
                        // usa a copy with ...
                        .copy(
                            rates = rates.rates
                                // ... the correct sort order of the rates
                                ?.sortedWith(
                                    // sort by full name (locale-aware)
                                    compareBy(Collator.getInstance()) { rate ->
                                        rate.currency.fullName(app)
                                    }
                                )
                        )
                }
            }
        }

        // update currently selected currencies when rates are updated:
        // sometimes the selected rates aren't available anymore, so reset them
        val baseCurrency = Database(app).getLastBaseCurrency()
        val destinationCurrency = Database(app).getLastDestinationCurrency()
        currentBaseCurrency = object : MediatorLiveData<Currency?>() {
            var base: Currency? = null
            var rates: ExchangeRates? = null

            init {
                addSource(baseCurrency) { base = it; update() }
                addSource(exchangeRates) { rates = it; update() }
            }

            private fun update() {
                this.value =
                    // last used is present in the current currency set
                    rates?.rates?.find { it.currency == base }?.currency
                        // not present, so just return the first of the set
                        ?: rates?.rates?.firstOrNull()?.currency
            }
        }
        currentDestinationCurrency = object : MediatorLiveData<Currency?>() {
            var destination: Currency? = null
            var rates: ExchangeRates? = null

            init {
                addSource(destinationCurrency) { destination = it; update() }
                addSource(exchangeRates) { rates = it; update() }
            }

            private fun update() {
                this.value =
                    // last used is present in the current currency set
                    rates?.rates?.find { it.currency == destination }?.currency
                            // not present, so just return the first of the set
                        ?: rates?.rates?.firstOrNull()?.currency
            }
        }

    }

    /**
     * all the current rates and/or an error message, if present
     */
    internal fun getExchangeRates(): LiveData<ExchangeRates?> {
        return exchangeRates
    }

    /**
     * update the data, without checking the cache
     */
    internal fun forceUpdateExchangeRate() {
        if (isUpdating.value != true)
            dbLiveItems = repository.getExchangeRates()
    }

    /**
     * all the currencies that the user has starred
     */
    internal fun getStarredCurrencies(): LiveData<Set<Currency>> {
        return starredLiveItems
    }

    /**
     * whether the currencies should be filtered
     */
    internal fun isFilterStarredEnabled(): LiveData<Boolean> {
        return onlyShowStarred
    }

    /**
     * switch the starred-filter on/off
     */
    internal fun toggleStarredActive() {
        Database(getApplication()).toggleStarredActive()
    }

    /**
     * de-/star a currency
     */
    internal fun toggleCurrencyStar(currencyCode: Currency) {
        Database(getApplication()).toggleCurrencyStar(currencyCode)
    }

    /**
     * the error message, if present
     */
    internal fun getError(): LiveData<String?> = liveError

    /**
     * if the app is updating the rates
     */
    internal fun isUpdating(): LiveData<Boolean> = isUpdating

    /**
     * whether the fee should be included in the calculation
     */
    internal fun isFeeEnabled(): LiveData<Boolean> {
        return isFeeEnabled
    }

    /**
     * the fee amount
     */
    internal fun getFee(): LiveData<Float> {
        return fee
    }

    /*
     * base and destination text ===================================================================
     */

    /**
     * the total base value
     */
    private val currentBaseValue = object : MediatorLiveData<String?>() {
        var baseValueText: String? = null
        var calculationValueText: String? = null

        init {
            addSource(currentBaseValueText) { baseValueText = it; update() }
            addSource(currentCalculationValueText) { calculationValueText= it; update() }
        }

        fun update() {
            if (isInCalculationMode())
                this.value = calculationValueText?.evaluateMathExpression()
            else
                this.value = baseValueText
        }

        // Turns e.g. "1 + 2 × 4" to "9"
        fun String.evaluateMathExpression(): String? {
            // change nice operators to proper computer operators
            var s = this
                .replace(" ", "")
                .replace("\u2212", "-")
                .replace("\u00D7", "*")
                .replace("\u00F7", "/")
            // fill, if last character is an operator
            when (s.trim().last()) {
                '/' -> s += "1"
                '*' -> s += "1"
                '+' -> s += "0"
                '-' -> s += "0"
                '.' -> s += "0"
            }
            // calculate
            val result = Expression(s).calculate()
            return if (result.isNaN())
                "0"
            else
                result.toBigDecimal().toPlainString()
        }
    }

    /**
     * the total base value, converted to double (internal is string)
     */
    internal fun getCurrentBaseValueAsNumber(): LiveData<Double> {
        return currentBaseValue.map {
            it?.toBigDecimal()?.toDouble() ?: 0.0
        }
    }

    /**
     * the nicely formatted, total base value including the currency symbol at the right position.
     */
    internal fun getCurrentBaseValueFormatted(): LiveData<SpannableStringBuilder> {
        return currentBaseValue.combineWith(currentBaseCurrency) { value, currency ->
            val number = (value?.toHumanReadableNumber(
                app,
                trim = isInCalculationMode(),
                decimalPlaces = if (isInCalculationMode()) 2 else null
            ) ?: "0")
            val symbol = currency?.symbol()

            if (hasAppendedCurrencySymbol(app))
                SpannableStringBuilder() // 123 $
                    .bold { append(number) }
                    .append(if (symbol != null) " $symbol" else "")
            else
                SpannableStringBuilder() // $ 123
                    .append(if (symbol != null) "$symbol " else "")
                    .bold { append(number) }
        }
    }

    // ===============================

    /**
     * the nicely formatted calculation string: e.g. 4 + 2.2 - 4 / 2
     */
    internal fun getCalculationInputFormatted(): LiveData<String?> {
        return currentCalculationValueText.map {
            it?.replace(".", getDecimalSeparator(app))
        }
    }

    // ===============================

    /**
     * the total destination value
     */
    private val result = object : MediatorLiveData<String>() {
        var rates : ExchangeRates? = null
        var baseValue: String? = null
        var baseCurrency: Currency? = null
        var destinationCurrency: Currency? = null
        var feeValue: Float? = null
        var feeEnabled: Boolean? = null

        init {
            // rates changed
            addSource(exchangeRates) { rates = it; calculateResult() }
            // base input changed
            addSource(currentBaseValue) { baseValue = it; calculateResult() }
            // base currency changed
            addSource(currentBaseCurrency) { baseCurrency = it; calculateResult() }
            // destination currency changed
            addSource(currentDestinationCurrency) { destinationCurrency = it; calculateResult() }
            // fee changed
            addSource(fee) { feeValue = it; calculateResult() }
            // fee got enabled/disabled
            addSource(isFeeEnabled) { feeEnabled = it; calculateResult() }
        }

        private fun calculateResult() {
            val baseValue: Double = baseValue?.toBigDecimal()?.toDouble() ?: 0.0
            val baseRate = rates?.rates?.find { it.currency == baseCurrency }
            val destinationRate = rates?.rates?.find { it.currency == destinationCurrency }

            if (baseRate != null && destinationRate != null) {
                this.value =
                    baseValue.div(baseRate.value).times(destinationRate.value)
                        .let {
                            // add fee, if enabled
                            if (feeEnabled != null && feeEnabled == true && feeValue != null) {
                                it + (it * (feeValue!! / 100))
                            } else {
                                it
                            }
                        }
                        .toString()
            }
        }
    }

    /**
     * the total destination value, converted to double (internal is string)
     */
    internal fun getResultAsNumber(): LiveData<Double> {
        return result.map {
            it?.toBigDecimal()?.toDouble() ?: 0.0
        }
    }

    /**
     * the nicely formatted, total destination value including the currency symbol at the right position.
     */
    internal fun getResultFormatted(): LiveData<SpannableStringBuilder> {
        return result.combineWith(currentDestinationCurrency) { value, currency ->
            val number = (value?.toHumanReadableNumber(
                app,
                trim = true,
                decimalPlaces = 2
            ) ?: "0")
            val symbol = currency?.symbol()

            if (hasAppendedCurrencySymbol(app))
                SpannableStringBuilder() // 123 $
                    .bold { append(number) }
                    .append(if (symbol != null) " $symbol" else "")
            else
                SpannableStringBuilder() // $ 123
                    .append(if (symbol != null) "$symbol " else "")
                    .bold { append(number) }
        }
    }

    /*
     * user input **********************************************************************************
     */

    internal fun addNumber(value: String) {
        // in calculation mode: add to upper row
        if (isInCalculationMode()) {
            // last input was "0"
            if (currentCalculationValueText.value!!.split(" ").last().trim() == "0") {
                // replace that "0" with any other number
                if (value != "0" && value != "00" && value != "000")
                    currentCalculationValueText.value = currentCalculationValueText.value?.trim()?.dropLast(1)?.plus(value)
            }
            // last input was an operator: replace "00" and "000" with "0"
            else if (currentCalculationValueText.value!!.split(" ").last().isEmpty() && (value == "00" || value == "000"))
                currentCalculationValueText.value += 0
            else
                currentCalculationValueText.value += value
        }
        // else: add to lower row
        else {
            currentBaseValueText.value =
                if (currentBaseValueText.value == "0") {
                    if (value == "00" || value == "000") "0"
                    else value
                } else currentBaseValueText.value.plus(value)
        }
    }

    internal fun paste(value: Number) {
        // clear base value (but not calculation row!)
        currentBaseValueText.value = "0"
        // paste
        value.toString().forEach {
            addNumber(it.toString())
        }
    }

    internal fun addDecimal() {
        // in calculation mode: add to upper row
        if (isInCalculationMode()) {
            if (!currentCalculationValueText.value!!.substringAfterLast(" ").contains(".")) {
                // if last char is not a number: add 0
                if (currentCalculationValueText.value!!.trim().last().isDigit().not())
                    currentCalculationValueText.value += "0"
                currentCalculationValueText.value += "."
            }
        }
        // add to lower row
        else
            if (!currentBaseValueText.value!!.contains("."))
                currentBaseValueText.value += "."
    }

    internal fun delete() {
        // in calculation mode: delete from upper row
        if (isInCalculationMode()) {
            currentCalculationValueText.value = currentCalculationValueText.value!!.trim().dropLast(1)
            // if last char is a number: trim!
            if (currentCalculationValueText.value!!.trim().last().isDigit())
                currentCalculationValueText.value = currentCalculationValueText.value!!.trim()
            // if only a number is left without an operator, delete it completely
            if (!currentCalculationValueText.value!!.contains("[\\u002B\\u2212\\u00D7\\u00F7]".toRegex()))
                currentCalculationValueText.value = null
        }
        // delete from lower row
        else {
            if (currentBaseValueText.value!!.length > 1)
                currentBaseValueText.value = currentBaseValueText.value?.dropLast(1)
            else
                clear()
        }
    }

    internal fun clear() {
        currentBaseValueText.value = "0"
        currentCalculationValueText.value = null
    }

    internal fun addition() {
        addOperator("\u002B")
    }

    internal fun subtraction() {
        addOperator("\u2212")
    }

    internal fun multiplication() {
        addOperator("\u00D7")
    }

    internal fun division() {
        addOperator("\u00F7")
    }

    private fun addOperator(operator: String) {

        fun Char.isOperator(): Boolean {
            return when (this) {
                '\u002B' -> true // +
                '\u2212' -> true // -
                '\u00D7' -> true // *
                '\u00F7' -> true // /
                else -> false
            }
        }

        // in calculation mode & already has operator at end position: exchange it!
        if (isInCalculationMode() && currentCalculationValueText.value!!.trim().last().isOperator())
            currentCalculationValueText.value = currentCalculationValueText.value?.trim()?.dropLast(1) + "$operator "
        // in calculation mode & last position is '.' -> remove it and add operator
        else if (isInCalculationMode() && currentCalculationValueText.value!!.trim().last() == '.')
            currentCalculationValueText.value = currentCalculationValueText.value?.trim()?.dropLast(1) + " $operator "
        else {
            // switch to calculation mode if necessary
            if (!isInCalculationMode())
                currentCalculationValueText.value = currentBaseValueText.value
            // add operator
            currentCalculationValueText.value = currentCalculationValueText.value?.trim().plus(" $operator ")
        }
    }

    /*
     * selected currencies *************************************************************************
     */

    internal fun setBaseCurrency(currency: Currency) {
        Database(getApplication()).saveLastUsedRates(
            currency,
            currentDestinationCurrency.value
        )
    }

    internal fun setDestinationCurrency(currency: Currency) {
        Database(getApplication()).saveLastUsedRates(
            currentBaseCurrency.value,
            currency
        )
    }

    internal fun getBaseCurrency(): LiveData<Currency?> {
        return  currentBaseCurrency
    }

    internal fun getDestinationCurrency(): LiveData<Currency?> {
        return  currentDestinationCurrency
    }

    /*
     * historical rates
     */

    internal fun setHistoricalDate(date: LocalDate?) {
        // check if previous date was "latest" or historical
        val wasLatestActive = Database(app).getHistoricalDate() == null
        // save selected historical date to db
        Database(getApplication()).setHistoricalDate(date)
        // refresh, if new date != cached date or if last state was "latest"
        if (date != Database(app).getDate() || wasLatestActive)
            forceUpdateExchangeRate()
    }

    internal fun getHistoricalDate(): LocalDate? {
        return Database(getApplication()).getHistoricalDate()
    }

    internal fun getHistoricalLiveDate(): LiveData<LocalDate?> {
        return Database(getApplication()).getHistoricalLiveDate()
    }


    /*
     * helpers =====================================================================================
     */

    private fun isInCalculationMode(): Boolean {
        return currentCalculationValueText.value.isNullOrBlank().not()
    }

}
