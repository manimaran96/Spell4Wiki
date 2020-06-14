package com.manimarank.spell4wiki.utils

import android.view.View
import com.google.android.material.snackbar.Snackbar


object SnackBarUtils {

    private fun showSnackBar(view: View, msg: String, duration: Int) {
        try {
            Snackbar.make(view, msg, duration).show()
        }catch (e : Exception){
        }
    }

    fun showLong(view: View, msg: String) {
        showSnackBar(view, msg, Snackbar.LENGTH_LONG)
    }

    fun showNormal(view: View, msg: String) {
        showSnackBar(view, msg, Snackbar.LENGTH_SHORT)
    }
}