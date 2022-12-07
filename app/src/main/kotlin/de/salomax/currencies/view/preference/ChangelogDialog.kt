package de.salomax.currencies.view.preference

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.text.HtmlCompat
import de.salomax.currencies.R
import java.lang.reflect.Modifier

class ChangelogDialog : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = View.inflate(context, R.layout.fragment_changelog, null)
        val textView = view.findViewById<TextView>(R.id.changelog)

        // HINT: needs proguard rule to work in release config
        for (declaredField in R.array::class.java.declaredFields
            .filter { field -> field.name.startsWith("changelog_") }
            .sortedByDescending { field ->
                val s = field.name.substringAfter('_').split('_')
                try {
                    val major = s[0].toInt()
                    val minor = s[1].toInt()
                    val patch = s[2].toInt()
                    major * 10_000 + minor * 100 + patch
                } catch (e: Exception) {
                    0
                }
            }) {
            val modifiers = declaredField.modifiers
            if (Modifier.isStatic(modifiers)
                && !Modifier.isPrivate(modifiers)
                && declaredField.type == Int::class.java
            )
                try {
                    val arrayId = declaredField.getInt(null)
                    // version number
                    val versionNumber = "<b>" + declaredField.name
                        .substringAfter('_')
                        .replace('_', '.') +
                            "</b><br>&#11834;"
                    // changes
                    val versionChanges = resources.getTextArray(arrayId)
                        .fold("") { acc, string -> "$acc<li>&nbsp;$string</li>" }
                        .plus("<br>")

                    textView.append(
                        HtmlCompat.fromHtml(
                            versionNumber + versionChanges,
                            HtmlCompat.FROM_HTML_MODE_COMPACT
                        )
                    )
                } catch (ignored: IllegalAccessException) {}
        }

        return AlertDialog.Builder(requireContext())
            .setPositiveButton(android.R.string.ok, null)
            .setTitle(R.string.title_changelog)
            .setView(view)
            .create()
    }

}
