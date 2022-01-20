package de.salomax.currencies.viewmodel.main

import android.app.Application
import androidx.lifecycle.*
import de.salomax.currencies.model.Currency
import de.salomax.currencies.model.Rate
import de.salomax.currencies.repository.Database
import de.salomax.currencies.util.getDecimalSeparator
import de.salomax.currencies.util.toHumanReadableNumber
import org.mariuszgromada.math.mxparser.Expression

class CurrentInputViewModel(val app: Application) : AndroidViewModel(app) {

    @Suppress("RemoveExplicitTypeArguments")
    private val currentInput = MutableLiveData<String>("0")
    private val currentCalculationInput = MutableLiveData<String?>(null)
    private val currentBaseRate = MutableLiveData<Rate>()
    private val currentDestinationRate = MutableLiveData<Rate>()

    /**
     * the total base value
     */
    private fun getCurrentBaseValue(): LiveData<String?> {
        return MediatorLiveData<String?>().apply {
            var input: String? = null
            var calculatorInput: String? = null

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

            fun update() {
                if (isInCalculationMode())
                    this.value = calculatorInput
                else
                    this.value = input
            }

            addSource(currentInput) {
                input = it
                update()
            }

            addSource(currentCalculationInput) {
                calculatorInput = it?.evaluateMathExpression()
                update()
            }
        }
    }

    /**
     * the total base value, converted to double (internal is string)
     */
    fun getCurrentBaseValueAsNumber(): LiveData<Double> {
        return Transformations.map(getCurrentBaseValue()) {
            it?.toBigDecimal()?.toDouble() ?: 0.0
        }
    }

    /**
     * the nicely formatted, total base value
     */
    fun getCurrentBaseValueFormatted(): LiveData<String> {
        return Transformations.map(getCurrentBaseValue()) {
            it?.toHumanReadableNumber(
                app,
                trim = isInCalculationMode(),
                decimalPlaces = if (isInCalculationMode()) 2 else null
            ) ?: "0"
        }
    }

    /**
     * the nicely formatted, calculation string: e.g. 4 + 2.2 - 4 / 2
     */
    fun getCalculationInputFormatted(): LiveData<String?> {
        return Transformations.map(currentCalculationInput) {
            it?.replace(".", getDecimalSeparator(app))
        }
    }

    /**
     * the nicely formatted, total destination value
     */
    fun getResultFormatted(): LiveData<String> {
        return MediatorLiveData<String>().apply {
            var baseValue: Double? = null
            var baseRate: Rate? = null
            var destinationRate: Rate? = null
            var fee: Float? = null
            var feeEnabled: Boolean? = null


            fun calculateResult() {
                if (fee != null && feeEnabled != null && baseValue != null && baseRate != null && destinationRate != null) {
                    // calculate destination value
                    this.value = baseValue!!.div(baseRate!!.value).times(destinationRate!!.value)
                        .let {
                            // add fee, if enabled
                            if (feeEnabled!!) {
                                it + (it * (fee!! / 100))
                            } else {
                                it
                            }
                        }
                        // format nicely to two decimal places
                        .toString()
                        .toHumanReadableNumber(app, decimalPlaces = 2, trim = true)
                }
            }

            addSource(getCurrentBaseValue()) {
                baseValue = it?.toBigDecimal()?.toDouble() ?: 0.0
                calculateResult()
            }

            addSource(currentBaseRate) {
                baseRate = it
                calculateResult()
            }

            addSource(currentDestinationRate) {
                destinationRate = it
                calculateResult()
            }

            addSource(getFee()) {
                fee = it
                calculateResult()
            }

            addSource(isFeeEnabled()) {
                feeEnabled = it
                calculateResult()
            }
        }
    }

    /**
     * the currency that's selected as base
     */
    fun getBaseCurrency(): LiveData<Currency> {
        return Transformations.map(currentBaseRate) {
            it.currency
        }
    }

    /**
     * the currency that's selected as destination
     */
    fun getDestinationCurrency(): LiveData<Currency> {
        return Transformations.map(currentDestinationRate) {
            it.currency
        }
    }

    /*
     * fee *****************************************************************************************
     */

    fun isFeeEnabled(): LiveData<Boolean> {
        return Database(getApplication()).isFeeEnabled()
    }

    fun getFee(): LiveData<Float> {
        return Database(getApplication()).getFee()
    }


    /*
     * user input **********************************************************************************
     */

    fun addNumber(value: String) {
        // in calculation mode: add to upper row
        if (isInCalculationMode()) {
            // last number is "0"
            if (currentCalculationInput.value!!.split(" ").last().trim() == "0") {
                // replace "0" with any other number
                if (value != "0")
                    currentCalculationInput.value = currentCalculationInput.value?.trim()?.dropLast(1)?.plus(value)
            } else
                currentCalculationInput.value += value
        }
        // else: add to lower row
        else {
            currentInput.value =
                if (currentInput.value == "0") value
                else currentInput.value.plus(value)
        }

    }

    fun addDecimal() {
        // in calculation mode: add to upper row
        if (isInCalculationMode()) {
            if (!currentCalculationInput.value!!.substringAfterLast(" ").contains(".")) {
                // if last char is not a number: add 0
                if (currentCalculationInput.value!!.trim().last().isDigit().not())
                    currentCalculationInput.value += "0"
                currentCalculationInput.value += "."
            }
        }
        // add to lower row
        else
            if (!currentInput.value!!.contains("."))
                currentInput.value += "."
    }

    fun delete() {
        // in calculation mode: delete from upper row
        if (isInCalculationMode()) {
            currentCalculationInput.value = currentCalculationInput.value!!.trim().dropLast(1)
            // if last char is a number: trim!
            if (currentCalculationInput.value!!.trim().last().isDigit())
                currentCalculationInput.value = currentCalculationInput.value!!.trim()
            // if only a number is left without an operator, delete it completely
            if (!currentCalculationInput.value!!.contains("[\\u002B\\u2212\\u00D7\\u00F7]".toRegex()))
                currentCalculationInput.value = null
        }
        // delete from lower row
        else {
            if (currentInput.value!!.length > 1)
                currentInput.value = currentInput.value?.dropLast(1)
            else
                clear()
        }

    }

    fun clear() {
        currentInput.value = "0"
        currentCalculationInput.value = null

    }

    fun addition() {
        addOperator("\u002B")
    }

    fun subtraction() {
        addOperator("\u2212")
    }

    fun multiplication() {
        addOperator("\u00D7")
    }

    fun division() {
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
        if (isInCalculationMode() && currentCalculationInput.value!!.trim().last().isOperator())
            currentCalculationInput.value = currentCalculationInput.value?.trim()?.dropLast(1) + "$operator "
        // in calculation mode & last position is '.' -> remove it and add operator
        else if (isInCalculationMode() && currentCalculationInput.value!!.trim().last() == '.')
            currentCalculationInput.value = currentCalculationInput.value?.trim()?.dropLast(1) + " $operator "
        else {
            // switch to calculation mode if necessary
            if (!isInCalculationMode())
                currentCalculationInput.value = currentInput.value
            // add operator
            currentCalculationInput.value = currentCalculationInput.value?.trim().plus(" $operator ")
        }
    }

    /*
     * selected currencies *************************************************************************
     */

    fun setBaseRate(rate: Rate) {
        currentBaseRate.value = rate
        saveSelectedCurrencies()
        // hack: refresh currency symbol
        currentInput.value = currentInput.value
    }

    fun setDestinationRate(rate: Rate) {
        currentDestinationRate.value = rate
        saveSelectedCurrencies()
    }

    /**
     * @return the last currency that was used as base
     */
    fun getLastCurrencyFrom(): Currency? {
        return Database(getApplication()).getLastRateFrom()
    }

    /**
     * @return the last currency that was used as target
     */
    fun getLastCurrencyTo(): Currency? {
        return Database(getApplication()).getLastRateTo()
    }

    /*
     * helpers =====================================================================================
     */

    private fun isInCalculationMode(): Boolean {
        return currentCalculationInput.value.isNullOrBlank().not()
    }

    /**
     * Saves currencyFrom and currencyTo to the database in order to restore them after restart
     */
    private fun saveSelectedCurrencies() {
        Database(getApplication()).saveLastUsedRates(
            currentBaseRate.value?.currency,
            currentDestinationRate.value?.currency
        )
    }

}
