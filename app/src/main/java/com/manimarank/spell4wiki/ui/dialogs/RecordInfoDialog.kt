package com.manimarank.spell4wiki.ui.dialogs

import android.app.Activity
import android.app.AlertDialog
import androidx.core.content.ContextCompat
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.prefs.AppPref

object RecordInfoDialog {


    fun show(activity: Activity) {
        try {
            if (!AppPref.getRecordInfoShowed()) {
                // Show Dialog
                val builder = AlertDialog.Builder(activity, R.style.AlertDialogTheme)
                builder.setTitle(R.string.record_info_dialog_title)
                builder.setMessage(R.string.record_info_dialog_message)
                builder.setCancelable(false)

                builder.setPositiveButton(R.string.record_info_dialog_ok) { dialog, _ ->
                    dialog.dismiss()
                }
                val dialog = builder.create()
                AppPref.setRecordInfoShowed()
                dialog.show()

                // Set button colors programmatically for better visibility
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
                    setTextColor(ContextCompat.getColor(activity, R.color.colorAccent))
                    textSize = 14f
                    isAllCaps = false
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}