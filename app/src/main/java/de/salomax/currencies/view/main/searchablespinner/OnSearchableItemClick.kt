package de.salomax.currencies.view.main.searchablespinner

import java.io.Serializable

interface OnSearchableItemClick<T> : Serializable {
    fun onSearchableItemClicked(item: T?, position: Int)
}
