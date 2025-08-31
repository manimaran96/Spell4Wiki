package com.manimarank.spell4wiki.ui.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import androidx.core.content.ContextCompat
import com.manimarank.spell4wiki.R

/**
 * Utility function to style dialog buttons consistently across the app
 */
fun AlertDialog.styleDialogButtons(activity: Activity) {
    // Set positive button style
    getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
        setTextColor(ContextCompat.getColor(activity, R.color.colorAccent))
        textSize = 14f
        isAllCaps = false
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    // Set negative button style
    getButton(AlertDialog.BUTTON_NEGATIVE)?.apply {
        setTextColor(ContextCompat.getColor(activity, R.color.black))
        textSize = 14f
        isAllCaps = false
        alpha = 0.87f
    }

    // Set neutral button style if exists
    getButton(AlertDialog.BUTTON_NEUTRAL)?.apply {
        setTextColor(ContextCompat.getColor(activity, R.color.gray))
        textSize = 14f
        isAllCaps = false
    }
}

fun Activity.showConfirmBackDialog(confirmAction: () -> Unit = {}) {
    val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
    builder.setTitle(R.string.confirmation)
    builder.setMessage(R.string.confirm_to_back)
    builder.setCancelable(false)
    builder.setPositiveButton(getString(R.string.yes)) { _: DialogInterface?, _: Int -> confirmAction() }
    builder.setNegativeButton(getString(R.string.cancel)) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
    val dialog = builder.create()
    dialog.show()

    // Apply consistent button styling
    dialog.styleDialogButtons(this)
}