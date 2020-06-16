package com.manimarank.spell4wiki.utils

import android.text.Editable
import android.text.TextWatcher
import android.text.style.MetricAffectingSpan
import android.widget.TextView

fun TextView.removeStyleAfterPaste() {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(string: Editable?) {
            try {
                if (string != null && string.isNotEmpty()) {
                    val toBeRemovedSpans = string.getSpans(0, string.length, MetricAffectingSpan::class.java)
                    for (toBeRemovedSpan in toBeRemovedSpans) string.removeSpan(toBeRemovedSpan)
                }
            }catch (e : Exception){
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    })
}