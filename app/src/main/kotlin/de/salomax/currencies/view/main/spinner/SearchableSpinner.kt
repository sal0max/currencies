package de.salomax.currencies.view.main.spinner

import android.content.Context
import android.content.ContextWrapper
import android.util.AttributeSet
import android.widget.SpinnerAdapter
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.FragmentActivity
import de.salomax.currencies.model.Currency
import de.salomax.currencies.model.Rate

class SearchableSpinner : AppCompatSpinner {

    private val mContext: Context
    private lateinit var spinnerDialog: SearchableSpinnerDialog

    val adapter = SearchableSpinnerAdapter(context, android.R.layout.simple_spinner_item)

    constructor(context: Context) : super(context) {
        this.mContext = context
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        this.mContext = context
        init()
    }

    constructor (context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        this.mContext = context
        init()
    }

    private fun init() {
        super.setAdapter(adapter)

        spinnerDialog = SearchableSpinnerDialog(context)
        // click listeners
        spinnerDialog.onRateClicked = { rate: Rate, _: Int ->
            setSelection(adapter.getPosition(rate.currency))
        }
    }

    override fun setAdapter(adapter: SpinnerAdapter?) {
        throw NoSuchMethodException("This Spinner sets its own adapter.")
    }

    // click on spinner -> open the dialog
    override fun performClick(): Boolean {
        return when {
            // dialog is already active
            spinnerDialog.isAdded -> true
            // else show dialog, if this spinner is backed by an adapter
            !spinnerDialog.isVisible -> {
                val fm = findActivity(mContext)?.supportFragmentManager
                if (fm != null) { spinnerDialog.show(fm, null) }
                true
            }
            // else do nothing
            else -> super.performClick()
        }
    }

    fun setRates(rates: List<Rate>?) {
        // set in own adapter...
        adapter.setRates(rates)
        // ...and in dialog
        spinnerDialog.setRates(rates)
    }

    fun setStars(stars: Set<Currency>?) {
        // set in dialog
        spinnerDialog.setStars(stars)
    }

    //  conversion preview
    fun setPreviewConversionEnabled(enabled: Boolean) {
        spinnerDialog.setPreviewConversionEnabled(enabled)
    }
    fun setCurrentRate(currentRate: Rate) {
        // set in dialog
        spinnerDialog.setCurrentRate(currentRate)
    }
    fun setCurrentSum(currentSum: Double) {
        // set in dialog
        spinnerDialog.setCurrentSum(currentSum)
    }

    private fun findActivity(context: Context?): FragmentActivity? {
        return when (context) {
            is FragmentActivity -> context
            is ContextWrapper -> findActivity(context.baseContext)
            else -> null
        }
    }

}
