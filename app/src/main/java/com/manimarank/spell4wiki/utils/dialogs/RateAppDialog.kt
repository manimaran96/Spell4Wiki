package com.manimarank.spell4wiki.utils.dialogs

import android.app.Activity
import android.app.AlertDialog
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.utils.AppPref
import com.manimarank.spell4wiki.utils.AppPref.INSTANCE.DAYS_UNTIL_WAIT
import com.manimarank.spell4wiki.utils.AppPref.INSTANCE.MAX_LAUNCHES
import com.manimarank.spell4wiki.utils.GeneralUtils
import com.manimarank.spell4wiki.utils.constants.Urls
import java.util.concurrent.TimeUnit

object RateAppDialog {

    fun show(activity: Activity) {
        try {
            if (!AppPref.getDontShowAgain()) {

                // Add first launch time
                var lastLaunch = AppPref.getLastLaunchTimeStamp()
                if (lastLaunch <= 0) {
                    lastLaunch = System.currentTimeMillis()
                    AppPref.setLastLaunchTimeStamp(lastLaunch)
                }

                val launchCount = AppPref.getLaunchCount()
                if (launchCount <= MAX_LAUNCHES) {
                    val duration = System.currentTimeMillis() - lastLaunch
                    if (TimeUnit.MILLISECONDS.toDays(duration) >= DAYS_UNTIL_WAIT) {

                        // Update launch count
                        AppPref.setLastLaunchTimeStamp(System.currentTimeMillis())
                        AppPref.setLaunchCount(launchCount + 1)

                        // Show Dialog
                        val builder = AlertDialog.Builder(activity)
                        builder.setTitle(R.string.rta_dialog_title)
                        builder.setMessage(R.string.rta_dialog_message)
                        builder.setCancelable(false)

                        builder.setPositiveButton(R.string.rta_dialog_ok) { dialog, _ ->
                            AppPref.setDontShowAgain()
                            dialog.dismiss()
                            GeneralUtils.openUrlInBrowser(activity, Urls.APP_LINK)
                        }

                        builder.setNeutralButton(R.string.rta_dialog_cancel) { dialog, _ ->
                            dialog.dismiss()
                        }

                        builder.setNegativeButton(R.string.rta_dialog_no) { dialog, _ ->
                            AppPref.setDontShowAgain()
                            dialog.dismiss()
                        }
                        val dialog = builder.create()
                        dialog.show()
                    }
                } else {
                    AppPref.setDontShowAgain()
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}