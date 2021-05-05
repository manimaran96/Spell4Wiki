package com.manimarank.spell4wiki.utils

import android.text.Editable
import android.text.TextWatcher
import android.text.style.MetricAffectingSpan
import android.view.View
import android.widget.TextView
import kotlin.Exception as KotlinException

/**
 * Extension function for Text view : Removing styles after paste
 */
fun TextView.removeStyleAfterPaste() {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(string: Editable?) {
            try {
                if (string != null && string.isNotEmpty()) {
                    val toBeRemovedSpans = string.getSpans(0, string.length, MetricAffectingSpan::class.java)
                    for (toBeRemovedSpan in toBeRemovedSpans) string.removeSpan(toBeRemovedSpan)
                }
            } catch (e: KotlinException) {
                e.printStackTrace()
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
    })
}

/**
 * View Gone extension
 */
fun View?.makeGone() {
    this?.visibility = View.GONE
}

/**
 * View Visible extension
 */
fun View?.makeVisible() {
    this?.visibility = View.VISIBLE
}

/**
 * View In-Visible extension
 */
fun View?.makeInVisible() {
    this?.visibility = View.INVISIBLE
}