package com.manimarank.spell4wiki.ui.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import com.manimarank.spell4wiki.BuildConfig
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.prefs.AppPref
import java.util.*


object AppLanguageDialog {

    const val LANGUAGE_FILTER = BuildConfig.APPLICATION_ID + ".LANGUAGE_CHANGE"
    const val SELECTED_LANGUAGE = "selected_language"

    private val languageCodeList by lazy {
        arrayOf(
                "en",
                "ta",
                "kn",
                "hi"
        )
    }

    private val languageList by lazy {
        arrayOf(
                "English",
                "தமிழ்",
                "ಕನ್ನಡ",
                "हिंदी"
        )
    }

    private fun setAppLanguageCode(languageCode: String) {
        AppPref.setAppLanguage(languageCode)
    }

    private fun getSelectedLanguageCode(): String {
        return if (AppPref.getAppLanguage() != null) AppPref.getAppLanguage()!! else "en"
    }

    fun getSelectedLanguage(): String {
        return languageList[languageCodeList.indexOf(getSelectedLanguageCode())]
    }

    fun show(activity: Activity) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.select_language) // add a radio button list

        val checkedItem = languageCodeList.indexOf(getSelectedLanguageCode())
        builder.setSingleChoiceItems(languageList, checkedItem) { dialog: DialogInterface, which: Int ->
            val selectedLanguageCode = languageCodeList[which]
            if (selectedLanguageCode != getSelectedLanguageCode() && !activity.isDestroyed && !activity.isFinishing) {
                setAppLanguageCode(languageCode = selectedLanguageCode)
                applyLanguageConfig(activity)
                activity.recreate()
                val intent = Intent(LANGUAGE_FILTER)
                intent.putExtra(SELECTED_LANGUAGE, selectedLanguageCode)
                activity.sendBroadcast(intent)
            }
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.cancel, null)
        val dialog = builder.create()
        dialog.show()
    }

    fun applyLanguageConfig(context: Context): Context {
        AppPref.init(context)
        val languageCode = getSelectedLanguageCode()
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val newConfiguration = Configuration()
        newConfiguration.setLocale(locale)
        return context.createConfigurationContext(newConfiguration)
    }

}