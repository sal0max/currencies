package de.salomax.currencies.view.main.searchablespinner

import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.util.AttributeSet
import android.widget.SpinnerAdapter
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.FragmentActivity
import de.salomax.currencies.model.Rate

class SearchableSpinner : AppCompatSpinner, OnSearchableItemClick<Rate> {

    private val mContext: Context
    private lateinit var spinnerDialog: SearchableSpinnerDialog

    private var dialogTitle: String? = null
    private var dialogCloseText: String? = null

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
        spinnerDialog = SearchableSpinnerDialog()
        spinnerDialog.setTitle(dialogTitle)
        spinnerDialog.setDismissText(dialogCloseText)
        spinnerDialog.mClickListener = this
    }

    override fun performClick(): Boolean {
        return when {
            // dialog is already active
            spinnerDialog.isAdded -> true
            // else show dialog, if this spinner is backed by an adapter
            !spinnerDialog.isVisible && adapter != null -> {
                val fm = findActivity(mContext)?.supportFragmentManager
                if (fm != null) {
                    // give currently selected position to dialog
                    spinnerDialog.arguments = Bundle().apply { putInt("position", selectedItemPosition) }
                    spinnerDialog.show(fm, null)
                }
                true
            }
            // else do nothing
            else -> super.performClick()
        }
    }

    override fun onSearchableItemClicked(item: Rate?, position: Int) {
        setSelection(position)
    }

    override fun setAdapter(adapter: SpinnerAdapter?) {
        super.setAdapter(adapter)
        spinnerDialog.listAdapter = SearchableSpinnerDialog.Adapter(context, adapter)
    }

    private fun findActivity(context: Context?): FragmentActivity? {
        return when (context) {
            is FragmentActivity -> context
            is ContextWrapper -> findActivity(context.baseContext)
            else -> null
        }
    }

}
