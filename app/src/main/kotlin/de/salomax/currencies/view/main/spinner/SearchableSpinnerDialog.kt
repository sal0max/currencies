package de.salomax.currencies.view.main.spinner

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageButton
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.salomax.currencies.R
import de.salomax.currencies.model.Rate
import de.salomax.currencies.viewmodel.main.ExchangeRatesViewModel

class SearchableSpinnerDialog(context: Context) : DialogFragment(), SearchView.OnQueryTextListener {

    private lateinit var ratesModel: ExchangeRatesViewModel

    private var filterStarredButton: ImageButton? = null
    private var searchView: SearchView? = null
    private var listView: RecyclerView? = null

    var onRateClicked: ((Rate, Int) -> Unit)? = null

    private var adapter: SearchableSpinnerDialogAdapter = SearchableSpinnerDialogAdapter(context)

    fun setRates(rates: List<Rate>?) {
        adapter.setRates(rates)
    }

    fun setStars(stars: Set<String>?) {
        adapter.setStars(stars)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val layoutInflater = LayoutInflater.from(activity)
        val rootView = layoutInflater.inflate(R.layout.searchable_spinner_dialog, null)

        this.ratesModel = ViewModelProvider(this).get(ExchangeRatesViewModel::class.java)

        // listView
        listView = rootView.findViewById(R.id.listView)
        listView?.layoutManager = LinearLayoutManager(context)
        listView?.adapter = adapter
        adapter.onRateClicked = { rate: Rate, position: Int ->
            onRateClicked?.invoke(rate, position)
            dismiss()
        }
        adapter.onStarClicked = {
            ratesModel.toggleCurrencyStar(it.code)
        }

        // searchView
        searchView = rootView.findViewById(R.id.searchView)
        searchView?.setOnQueryTextListener(this)
        searchView?.clearFocus()

        // filter starred
        filterStarredButton = rootView.findViewById(R.id.btn_toggle_fav)
        filterStarredButton?.setOnClickListener {
            ratesModel.toggleStarredActive()
        }
        ratesModel.isFilterStarredEnabled().observe(this, { enabled ->
            filterStarredButton?.setImageDrawable(
                if (enabled) ContextCompat.getDrawable(requireContext(), R.drawable.ic_favorite_on)
                else ContextCompat.getDrawable(requireContext(), R.drawable.ic_favorite_off)
            )
        })
        ratesModel.isFilterStarredEnabled().observe(this, {
            adapter.filterStarred(it)
        })

        // build dialog
        val alertBuilder = AlertDialog.Builder(activity)
        alertBuilder.setView(rootView)
        alertBuilder.setNegativeButton(getString(android.R.string.cancel), null)

        return alertBuilder.create()
    }

    override fun onQueryTextChange(query: String?): Boolean {
        adapter.filter(query)
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        searchView?.clearFocus()
        return true
    }

    // intentionally close dialog on orientation change. else it's a real mess to restore the
    // adapter. really not worth the effort!
    override fun onPause() {
        super.onPause()
        // needs to be done manually, as this fragment/adapter seems to get re-used. the filter won't get reset
        adapter.reset()
        dismiss()
    }



}
