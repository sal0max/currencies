package de.salomax.currencies.util

import android.content.res.Resources

@Suppress("unused")
fun Float.pxToDp(): Float = (this / Resources.getSystem().displayMetrics.density)

@Suppress("unused")
fun Float.dpToPx(): Float = (this * Resources.getSystem().displayMetrics.density)
