package de.salomax.currencies.widget.searchablespinner

import java.io.Serializable

interface OnSearchableItemClick<T> : Serializable {
    fun onSearchableItemClicked(item: T?, position: Int)
}
