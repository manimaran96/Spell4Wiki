package com.manimarank.spell4wiki.ui.common

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var currentPosition = 0

    protected abstract fun clear()
    open fun onBind(position: Int) {
        currentPosition = position
        clear()
    }
}