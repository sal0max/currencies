package de.salomax.currencies.view.main

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.DatePicker

@Suppress("unused")
class ScrollableDatePicker : DatePicker {

    constructor(context: Context) :
            super(context)

    constructor(context: Context, attrs: AttributeSet) :
            super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) :
            super(context, attrs, defStyle)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int, defStyleRes: Int) :
            super(context, attrs, defStyle, defStyleRes)

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        val parentView = parent
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            parentView?.requestDisallowInterceptTouchEvent(true)
        }
        return false
    }
}
