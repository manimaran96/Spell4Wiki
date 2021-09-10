package com.manimarank.spell4wiki.ui.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import com.manimarank.spell4wiki.R

fun Activity.showConfirmBackDialog(confirmAction: () -> Unit = {}) {
    val builder = AlertDialog.Builder(this)
    builder.setTitle(R.string.confirmation)
    builder.setMessage(R.string.confirm_to_back)
    builder.setCancelable(false)
    builder.setPositiveButton(getString(R.string.yes)) { _: DialogInterface?, _: Int -> confirmAction() }
    builder.setNegativeButton(getString(R.string.cancel)) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
    val dialog = builder.create()
    dialog.show()
}