package com.manimarank.spell4wiki.ui.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import androidx.core.content.ContextCompat
import com.manimarank.spell4wiki.R

fun Activity.showConfirmBackDialog(confirmAction: () -> Unit = {}) {
    val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
    builder.setTitle(R.string.confirmation)
    builder.setMessage(R.string.confirm_to_back)
    builder.setCancelable(false)
    builder.setPositiveButton(getString(R.string.yes)) { _: DialogInterface?, _: Int -> confirmAction() }
    builder.setNegativeButton(getString(R.string.cancel)) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
    val dialog = builder.create()
    dialog.show()

    // Set button colors programmatically for better visibility
    dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
        setTextColor(ContextCompat.getColor(this@showConfirmBackDialog, R.color.colorAccent))
        textSize = 14f
        isAllCaps = false
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.apply {
        setTextColor(ContextCompat.getColor(this@showConfirmBackDialog, R.color.black))
        textSize = 14f
        isAllCaps = false
        alpha = 0.87f
    }
}