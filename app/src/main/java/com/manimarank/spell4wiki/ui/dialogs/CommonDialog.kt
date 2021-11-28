package com.manimarank.spell4wiki.ui.dialogs

import android.app.Activity
import android.app.AlertDialog
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.prefs.AppPref

object CommonDialog {


    fun Activity.openInfoDialog(title: String, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setCancelable(false)

        builder.setPositiveButton(R.string.record_info_dialog_ok) { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        AppPref.setRecordInfoShowed()
        dialog.show()
    }

}