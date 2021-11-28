package com.manimarank.spell4wiki.data.apis

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.os.AsyncTask
import android.view.Window
import android.widget.TextView
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.ui.listerners.FileAvailabilityCallback
import com.manimarank.spell4wiki.utils.constants.Urls
import java.net.HttpURLConnection
import java.net.URL

object WikimediaCommonsUtils {
    fun checkFileAvailability(activity: Activity, word: String, langCode: String, callback: FileAvailabilityCallback) {

        val fileAvailabilityTask = @SuppressLint("StaticFieldLeak")
        object : AsyncTask<Void, Void, Boolean>() {

            lateinit var dialog: Dialog
            val startTime = System.currentTimeMillis()

            override fun onPreExecute() {
                super.onPreExecute()
                try {
                    if (!activity.isDestroyed && !activity.isFinishing) {
                        dialog = Dialog(activity, R.style.RecordAudioDialogTheme)
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                        dialog.setContentView(R.layout.loading_file_availability)
                        val txtInfo = dialog.findViewById<TextView>(R.id.txtFileName)
                        val fileName = "$langCode-$word.ogg"
                        txtInfo.text = String.format(activity.getString(R.string.checking_file_availability), fileName)
                        dialog.setCancelable(false)
                        dialog.show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun doInBackground(vararg params: Void?): Boolean {
                var fileExist = false
                try {
                    val url = String.format(Urls.AUDIO_FILE_IN_COMMONS, langCode, word)
                    val u = URL(url)
                    val huc: HttpURLConnection = u.openConnection() as HttpURLConnection
                    huc.requestMethod = "HEAD"
                    huc.connect()
                    fileExist = huc.responseCode == 200
                    huc.disconnect()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val duration = System.currentTimeMillis() - startTime
                if (duration < 500) {
                    Thread.sleep(500 - duration)
                }
                return fileExist
            }

            override fun onPostExecute(fileExist: Boolean) {
                super.onPostExecute(fileExist)
                try {
                    if (dialog.isShowing)
                        dialog.dismiss()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                callback.status(fileExist)
            }
        }

        fileAvailabilityTask.execute()
    }
}