package de.salomax.currencies.view.main.spinner

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.SpinnerAdapter
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.FragmentActivity
import de.salomax.currencies.R
import de.salomax.currencies.model.Currency
import de.salomax.currencies.model.Rate

class SearchableSpinner : AppCompatSpinner {

    private val mContext = context
    private lateinit var spinnerDialog: SearchableSpinnerDialog

    private val adapter = SearchableSpinnerAdapter(context, android.R.layout.simple_spinner_item)

    constructor(
        context: Context
    ) : this(context, null)

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : this(context, attrs, R.attr.spinnerStyle)

    constructor (
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        super.setAdapter(adapter)

        spinnerDialog = SearchableSpinnerDialog(context)
        // click listeners
        spinnerDialog.onRateClicked = { rate: Rate, _: Int ->
            setSelection(adapter.getPosition(rate.currency))
        }
        // prevent "drag-to-open" (interferes with pull-to-refresh): https://stackoverflow.com/questions/27923266/
        setOnTouchListener { v, event ->
            if (event.action != MotionEvent.ACTION_MOVE)
                v.onTouchEvent(event)
            else
                true
        }
    }

    fun setSelection(currency: Currency?) {
        setSelection(currency?.let { adapter.getPosition(it) } ?: -1)
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
    }

    //  conversion preview
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
