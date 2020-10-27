package de.salomax.currencies.view.preference

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import de.salomax.currencies.R

class ChangelogDialog : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = View.inflate(context, R.layout.fragment_changelog, null)

        return AlertDialog.Builder(requireContext())
            .setPositiveButton(android.R.string.ok, null)
            .setTitle(R.string.title_changelog)
            .setView(view)
            .create()
    }

}
