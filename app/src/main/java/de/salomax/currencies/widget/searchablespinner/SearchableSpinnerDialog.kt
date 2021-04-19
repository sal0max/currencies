package de.salomax.currencies.widget.searchablespinner

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.ListView
import android.widget.SpinnerAdapter
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.DialogFragment
import de.salomax.currencies.R
import de.salomax.currencies.model.Rate

class SearchableSpinnerDialog : DialogFragment(), SearchView.OnQueryTextListener {

    private var mSearchView: SearchView? = null
    private var mListView: ListView? = null

    private var mDialogTitle: String? = null
    private var mDismissText: String? = null

    lateinit var mClickListener: OnSearchableItemClick<Rate>

    var listAdapter: Adapter? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val layoutInflater = LayoutInflater.from(activity)
        val rootView = layoutInflater.inflate(R.layout.searchable_spinner_dialog, null)

        // listView
        mListView = rootView.findViewById(R.id.listView)
        mListView?.divider = null
        mListView?.adapter = listAdapter
        mListView?.isTextFilterEnabled = true
        mListView?.setOnItemClickListener { _, _, position, _ ->
            mClickListener.onSearchableItemClicked(
                listAdapter?.getItem(position),
                listAdapter?.findOriginalPosition(position) ?: -1
            )
            dismiss()
        }
        // get selected item from spinner and move the lists top position to it
        arguments?.getInt("position")?.let { mListView?.setSelection(it) }
        // searchView
        mSearchView = rootView.findViewById(R.id.searchView)
        mSearchView?.setOnQueryTextListener(this)
        mSearchView?.clearFocus()

        // build dialog
        val alertBuilder = AlertDialog.Builder(activity)
        alertBuilder.setView(rootView)
        // title
        alertBuilder.setTitle(mDialogTitle)
        // close button
        val dismissText = if (mDismissText.isNullOrBlank()) getString(android.R.string.cancel) else mDismissText
        alertBuilder.setNegativeButton(dismissText, null)

        // restore state
        if (savedInstanceState != null) {
            @Suppress("UNCHECKED_CAST")
            mClickListener = savedInstanceState.getSerializable("clickListener") as OnSearchableItemClick<Rate>
            mListView?.onRestoreInstanceState(savedInstanceState.getParcelable("listView.state"))
        }
        return alertBuilder.create()
    }

    override fun onQueryTextChange(query: String?): Boolean {
        listAdapter?.filter?.filter(query)
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        mSearchView?.clearFocus()
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable("clickListener", mClickListener)
        outState.putParcelable("listView.state", mListView?.onSaveInstanceState())
        super.onSaveInstanceState(outState)
    }

    // intentionally close dialog on orientation change. else it's a real mess to restore the
    // adapter. really not worth the effort!
    override fun onPause() {
        super.onPause()
        // needs to be done manually, as this fragment/adapter seems to get re-used. the filter won't get reset
        listAdapter?.reset()
        dismiss()
    }

    fun setDismissText(closeText: String?) {
        mDismissText = closeText
    }

    fun setTitle(dialogTitle: String?) {
        mDialogTitle = dialogTitle
    }


    class Adapter(context: Context, private val mAdapter: SpinnerAdapter?) : ArrayAdapter<Rate>(context, -1) {

        private var mItems: MutableList<Rate> = mutableListOf()
        private var mItemsFiltered: MutableList<Rate> = mutableListOf()

        init {
            // copy all items of the original adapter to the two local lists, to work with them
            if (mAdapter != null)
                for (i in 0 until mAdapter.count) {
                    val item = mAdapter.getItem(i) as Rate
                    mItems.add(item)
                    mItemsFiltered.add(item)
                }
        }

        // find the position of an item in mItems, given the position of an item in mItemsFiltered
        internal fun findOriginalPosition(position: Int): Int {
            if (mAdapter != null)
                for (i in 0 until mAdapter.count) {
                    if (getItem(position) == mAdapter.getItem(i))
                        return i
                }
            return -1
        }

        override fun getCount(): Int {
            return mItemsFiltered.size
        }

        override fun getItem(position: Int): Rate {
            return mItemsFiltered[position]
        }

        // use the dropDownView from the original adapter
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return getDropDownView(position, convertView, parent)!!
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View? {
            return mAdapter?.getDropDownView(findOriginalPosition(position), convertView, parent)
        }

        // find all rates based on both their code name or their full name
        override fun getFilter(): Filter {
            return object : Filter() {
                // background thread
                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    val results = FilterResults()
                    val itemsFiltered = mItems.filter { rate ->
                        if (constraint != null)
                            // full name
                            rate.getName(context)?.contains(constraint, ignoreCase = true) == true
                                    // code name
                                    || rate.code.contains(constraint, ignoreCase = true)
                        else
                            true
                    }
                    results.values = itemsFiltered
                    results.count = itemsFiltered.size
                    return results
                }

                // ui thread
                override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                    @Suppress("UNCHECKED_CAST")
                    if (results?.values != null) {
                        mItemsFiltered = results.values as MutableList<Rate>
                        notifyDataSetChanged()
                    }
                }
            }
        }

        internal fun reset() { mItemsFiltered = mItems }
    }

}
