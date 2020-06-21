package com.manimarank.spell4wiki.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.text.TextUtils

class AppPref {

    companion object INSTANCE {


        private const val PREF_NAME = "app_pref"

        // App Settings
        private const val COMMON_CATEGORIES = "common_categories"
        private const val APP_LANGUAGE_CODE = "app_language_code"

        // Rate APP
        private const val DONT_SHOW_AGAIN = "dont_show_again"
        private const val LAUNCH_COUNT = "launch_count"
        private const val LAST_LAUNCH_TIMESTAMP = "last_launch_timestamp"
        const val MAX_LAUNCHES = 2
        const val DAYS_UNTIL_WAIT = 2

        // Update App
        private const val UPDATE_VERSION = "update_version"
        private const val UPDATE_TYPE = "update_type"
        private const val UPDATE_SHOWED = "update_showed"

        // Fetch Config
        private const val FETCH_BY = "fetch_by"
        private const val FETCH_DIR = "fetch_dir"
        private const val FETCH_LIMIT = "fetch_limit"

        private const val DEFAULT_FETCH_BY = "timestamp"
        private const val DEFAULT_FETCH_DIR = "desc"
        private const val DEFAULT_FETCH_LIMIT = 150

        // Record
        private const val RECORD_INFO_SHOWED = "record_info_showed"

        lateinit var pref: SharedPreferences
        fun init(context: Context) {
            pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }


        fun setCommonCategories(commonCategoryList: List<String>) {
            pref.edit()?.apply {
                putStringSet(COMMON_CATEGORIES, commonCategoryList.toSet())
                apply()
            }
        }

        fun getCommonCategories(): Set<String>? {
            return pref.getStringSet(COMMON_CATEGORIES, null)
        }

        fun setAppLanguage(langCode: String) {
            pref.edit().apply {
                putString(APP_LANGUAGE_CODE, langCode)
                apply()
            }
        }

        fun getAppLanguage(): String? {
            return pref.getString(APP_LANGUAGE_CODE, "en")
        }

        fun setDontShowAgain() {
            pref.edit()?.apply {
                putBoolean(DONT_SHOW_AGAIN, true)
                apply()
            }
        }

        fun getDontShowAgain(): Boolean {
            return pref.getBoolean(DONT_SHOW_AGAIN, false)
        }

        fun setLastLaunchTimeStamp(timestamp: Long) {
            pref.edit()?.apply {
                putLong(LAST_LAUNCH_TIMESTAMP, timestamp)
                apply()
            }
        }

        fun getLastLaunchTimeStamp(): Long {
            return pref.getLong(LAST_LAUNCH_TIMESTAMP, 0)
        }

        fun setLaunchCount(count: Int) {
            pref.edit()?.apply {
                putInt(LAUNCH_COUNT, count)
                apply()
            }
        }

        fun getLaunchCount(): Int {
            return pref.getInt(LAUNCH_COUNT, 0)
        }

        fun setUpdateAppVersion(count: Int) {
            pref.edit()?.apply {
                putInt(UPDATE_VERSION, count)
                apply()
            }
        }

        private fun getUpdateAppVersion(): Int {
            return pref.getInt(UPDATE_VERSION, 0)
        }

        fun setUpdateAppType(type: String) {
            pref.edit().apply {
                putString(UPDATE_TYPE, type)
                apply()
            }
        }

        fun setUpdateShowed() {
            pref.edit().apply {
                putBoolean(UPDATE_SHOWED, true)
                apply()
            }
        }

        private fun getUpdateShowed(): Boolean {
            return pref.getBoolean(UPDATE_SHOWED, false)
        }


        fun checkAppUpdateAvailable(context: Context): Boolean {
            return !getUpdateShowed() && getUpdateAppVersion() > getAppVersion(context)
        }

        private fun getAppVersion(context: Context): Long {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            var versionCode: Long = 0
            try {
                versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pInfo.longVersionCode
                } else {
                    pInfo.versionCode.toLong()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return versionCode
        }

        fun setFetchLimit(limit: Int) {
            pref.edit().apply {
                putInt(FETCH_LIMIT, limit)
                apply()
            }
        }

        fun getFetchLimit(): Int {
            return if (pref.getInt(FETCH_LIMIT, DEFAULT_FETCH_LIMIT) > 0) pref.getInt(FETCH_LIMIT, DEFAULT_FETCH_LIMIT) else 150
        }

        fun setFetchBy(fetchBy: String) {
            pref.edit().apply {
                putString(FETCH_BY, fetchBy)
                apply()
            }
        }

        fun getFetchBy(): String {
            return if (TextUtils.isEmpty(pref.getString(FETCH_BY, DEFAULT_FETCH_BY)))
                DEFAULT_FETCH_BY
            else
                pref.getString(FETCH_BY, DEFAULT_FETCH_BY)!!
        }

        fun setFetchDir(fetchDir: String) {
            pref.edit().apply {
                putString(FETCH_DIR, fetchDir)
                apply()
            }
        }

        fun getFetchDir(): String {
            return if (TextUtils.isEmpty(pref.getString(FETCH_DIR, DEFAULT_FETCH_DIR)))
                DEFAULT_FETCH_DIR
            else
                pref.getString(FETCH_DIR, DEFAULT_FETCH_DIR)!!
        }

        fun setRecordInfoShowed() {
            pref.edit().apply {
                putBoolean(RECORD_INFO_SHOWED, true)
                apply()
            }
        }

        fun getRecordInfoShowed(): Boolean {
            return pref.getBoolean(RECORD_INFO_SHOWED, false)
        }
    }
}