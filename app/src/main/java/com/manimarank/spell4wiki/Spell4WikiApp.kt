package com.manimarank.spell4wiki

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.manimaran.crash_reporter.CrashReporter
import com.manimaran.crash_reporter.CrashReporterConfiguration
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.ui.dialogs.AppLanguageDialog
import kotlin.Exception as KotlinException

/**
 * Application class for Spell4Wiki
 */
class Spell4WikiApp : Application() {
    companion object {
        lateinit var instance: Spell4WikiApp

        /**
         * Getting common app level context value
         * @return Spell4WikiApp
         */
        fun getApplicationContext(): Spell4WikiApp {
            return instance
        }
    }

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        try {
            val sharedPref = PrefManager(applicationContext)
            val emailIds = arrayOf("manimarankumar96@gmail.com")
            var extraInfo = ""
            if (sharedPref.isAnonymous == true)
                extraInfo = "User Name : Anonymous User"
            else if (sharedPref.name != null)
                extraInfo = "User Name : ${sharedPref.name}"

            val config = CrashReporterConfiguration()
                    .setExtraInformation(extraInfo)
                    .setMaxNumberOfCrashToBeReport(if (Build.VERSION.SDK_INT >= 23) 8 else 5)
                    .setMaxNoOfLineInCrash(if (Build.VERSION.SDK_INT >= 23) 25 else 15)
                    .setAlertDialogTitle(getString(R.string.crash_report_alert_title))
                    .setAlertDialogMessage(getString(R.string.crash_report_alert_message))
                    .setAlertDialogPositiveButton(getString(R.string.send))
                    .setAlertDialogNegativeButton(getString(R.string.cancel))
                    .setIncludeDeviceInformation(true)
                    .setCrashReportSubjectForEmail("${getString(R.string.app_name)} App - Crash Report")
                    .setCrashReportSendEmailIds(emailIds)

            CrashReporter.initialize(applicationContext, config)
        } catch (e: KotlinException) {
            e.printStackTrace()
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(AppLanguageDialog.applyLanguageConfig(base))
    }
}
