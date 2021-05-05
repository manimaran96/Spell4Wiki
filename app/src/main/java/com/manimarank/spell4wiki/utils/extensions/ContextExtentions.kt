package com.manimarank.spell4wiki.utils.extensions

import android.content.Context
import android.os.Build
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