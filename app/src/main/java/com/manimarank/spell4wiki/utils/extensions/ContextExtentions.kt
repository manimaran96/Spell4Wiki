package com.manimarank.spell4wiki.utils.extensions

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.utils.WikiLicense
import kotlin.Exception as KotlinException

/**
 * Get App version
 */
fun Context.getAppVersion(): Long {
    val pInfo = this.packageManager.getPackageInfo(this.packageName, 0)
    var versionCode: Long = 0
    try {
        versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pInfo.longVersionCode
        } else {
            pInfo.versionCode.toLong()
        }
    } catch (e: KotlinException) {
        e.printStackTrace()
    }
    return versionCode
}

fun Activity.showLicenseChooseDialog(confirmAction: () -> Unit = {}) {
    try {
        val pref = PrefManager(this)
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.license_choose_alert) // add a radio button list
        val licensePrefList = arrayOf(
            WikiLicense.LicensePrefs.CC_0,
            WikiLicense.LicensePrefs.CC_BY_3,
            WikiLicense.LicensePrefs.CC_BY_SA_3,
            WikiLicense.LicensePrefs.CC_BY_4,
            WikiLicense.LicensePrefs.CC_BY_SA_4
        )
        val licenseList = arrayOf(
            getString(R.string.license_name_cc_zero),
            getString(R.string.license_name_cc_by_three),
            getString(R.string.license_name_cc_by_sa_three),
            getString(R.string.license_name_cc_by_four),
            getString(R.string.license_name_cc_by_sa_four)
        )
        val checkedItem = licensePrefList.indexOf(pref.uploadAudioLicense)
        builder.setSingleChoiceItems(licenseList, checkedItem) { dialog: DialogInterface, which: Int ->
            pref.uploadAudioLicense = licensePrefList[which]
            confirmAction()
            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.cancel), null)
        val dialog = builder.create()
        dialog.show()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}