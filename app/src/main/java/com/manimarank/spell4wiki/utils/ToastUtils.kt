package com.manimarank.spell4wiki.utils

import android.widget.Toast
import com.manimarank.spell4wiki.Spell4WikiApp


object ToastUtils {

    private fun showToast(msg: String, duration: Int) {
        try {
            Toast.makeText(Spell4WikiApp.getApplicationContext(), msg, duration).show()
        } catch (ignore: Exception) {
        }
    }

    fun showLong(msg: String) {
        showToast(msg, Toast.LENGTH_LONG)
    }

}