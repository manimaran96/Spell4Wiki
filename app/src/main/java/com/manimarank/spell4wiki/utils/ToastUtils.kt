package com.manimarank.spell4wiki.utils

import android.widget.Toast
import com.manimarank.spell4wiki.Spell4WikiApp


object ToastUtils {

    private fun showToast(msg: String, duration: Int) {
        Toast.makeText(Spell4WikiApp.getApplicationContext(), msg, duration).show()
    }

    fun showNormal(msg: String) {
        showToast(msg, Toast.LENGTH_SHORT)
    }

    fun showLong(msg: String) {
        showToast(msg, Toast.LENGTH_LONG)
    }

    fun showCustom(msg: String, duration: Int) {
        showToast(msg, duration)
    }

}