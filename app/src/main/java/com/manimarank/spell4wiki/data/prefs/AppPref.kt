package com.manimarank.spell4wiki.data.prefs

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.manimarank.spell4wiki.utils.extensions.getAppVersion

/**
 * Preference utility class for App level base configurations preference
 */
class AppPref {

    /**
     * Companion - Preference utility class for App level base configurations preference
     */
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

        // Notification Permission
        private const val NOTIFICATION_PERMISSION_REQUESTED = "notification_permission_requested"

        lateinit var pref: SharedPreferences
        fun init(context: Context) {
            pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }

        /**
         * Set common categories for uploaded words like Files uploaded by spell4wiki
         * @return Set<String>?
         */
        fun setCommonCategories(commonCategoryList: List<String>) {
            pref.edit()?.apply {
                putStringSet(COMMON_CATEGORIES, commonCategoryList.toSet())
                apply()
            }
        }

        /**
         * Get common categories for uploaded words like Files uploaded by spell4wiki
         * @return Set<String>?
         */
        fun getCommonCategories(): Set<String>? {
            return pref.getStringSet(COMMON_CATEGORIES, null)
        }

        /**
         * Set app language code
         * @param langCode String
         */
        fun setAppLanguage(langCode: String) {
            pref.edit().apply {
                putString(APP_LANGUAGE_CODE, langCode)
                apply()
            }
        }

        /**
         * Get app language code
         * @return String?
         */
        fun getAppLanguage(): String? {
            return pref.getString(APP_LANGUAGE_CODE, "en")
        }

        /**
         * Set Flag for app rate dialog show restrict
         */
        fun setDontShowAgain() {
            pref.edit()?.apply {
                putBoolean(DONT_SHOW_AGAIN, true)
                apply()
            }
        }

        /**
         * Get Flag for app rate dialog show restrict
         * @return Boolean
         */
        fun getDontShowAgain(): Boolean {
            return pref.getBoolean(DONT_SHOW_AGAIN, false)
        }

        /**
         * Set app last launch time stamp
         * @param timestamp Long
         */
        fun setLastLaunchTimeStamp(timestamp: Long) {
            pref.edit()?.apply {
                putLong(LAST_LAUNCH_TIMESTAMP, timestamp)
                apply()
            }
        }

        /**
         * Getting app last launch time stamp
         * @return Long
         */
        fun getLastLaunchTimeStamp(): Long {
            return pref.getLong(LAST_LAUNCH_TIMESTAMP, 0)
        }

        /**
         * Set Maximum app launch count value for showing rate app dialog
         * @param count Int
         */
        fun setLaunchCount(count: Int) {
            pref.edit()?.apply {
                putInt(LAUNCH_COUNT, count)
                apply()
            }
        }

        /**
         * Getting Maximum app launch count value for showing rate app dialog
         * @return Int
         */
        fun getLaunchCount(): Int {
            return pref.getInt(LAUNCH_COUNT, 0)
        }

        /**
         * Set latest app version code from API
         * @param count Int
         */
        fun setUpdateAppVersion(count: Int) {
            pref.edit()?.apply {
                putInt(UPDATE_VERSION, count)
                apply()
            }
        }

        /**
         * Getting latest app version code
         * @return Int
         */
        private fun getUpdateAppVersion(): Int {
            return pref.getInt(UPDATE_VERSION, 0)
        }

        /**
         * Set app update type once or force
         * @param type String
         */
        fun setUpdateAppType(type: String) {
            pref.edit().apply {
                putString(UPDATE_TYPE, type)
                apply()
            }
        }

        /**
         * Set app update flag as true
         */
        fun setUpdateShowed() {
            pref.edit().apply {
                putBoolean(UPDATE_SHOWED, true)
                apply()
            }
        }

        /**
         * Get app update flag value
         * @return Boolean
         */
        private fun getUpdateShowed(): Boolean {
            return pref.getBoolean(UPDATE_SHOWED, false)
        }

        /**
         * Check app update dialog already showed or not
         * @param context Context
         * @return Boolean
         */
        fun checkAppUpdateAvailable(context: Context): Boolean {
            return !getUpdateShowed() && getUpdateAppVersion() > context.getAppVersion()
        }

        /**
         * Set fetch limit from API max 1 to 500 max
         * @param limit Int
         */
        fun setFetchLimit(limit: Int) {
            pref.edit().apply {
                putInt(FETCH_LIMIT, limit)
                apply()
            }
        }

        /**
         * Getting words fetch limit count 1 to 500 max
         * @return Int
         */
        fun getFetchLimit(): Int {
            return if (pref.getInt(FETCH_LIMIT, DEFAULT_FETCH_LIMIT) > 0) pref.getInt(FETCH_LIMIT, DEFAULT_FETCH_LIMIT) else DEFAULT_FETCH_LIMIT
        }

        /**
         * Set fetch sort by property value Name/Time
         * @param fetchBy String
         */
        fun setFetchBy(fetchBy: String) {
            pref.edit().apply {
                putString(FETCH_BY, fetchBy)
                apply()
            }
        }

        /**
         * Getting fetch sort by property value Name/Time
         * @return String
         */
        fun getFetchBy(): String {
            return if (TextUtils.isEmpty(pref.getString(FETCH_BY, DEFAULT_FETCH_BY)))
                DEFAULT_FETCH_BY
            else
                pref.getString(FETCH_BY, DEFAULT_FETCH_BY)!!
        }

        /**
         * Set fetch sorting direction value Ascending / Descending
         * @param fetchDir String
         */
        fun setFetchDir(fetchDir: String) {
            pref.edit().apply {
                putString(FETCH_DIR, fetchDir)
                apply()
            }
        }

        /**
         * Getting fetch sorting direction value Ascending / Descending
         * @return String
         */
        fun getFetchDir(): String {
            return if (TextUtils.isEmpty(pref.getString(FETCH_DIR, DEFAULT_FETCH_DIR)))
                DEFAULT_FETCH_DIR
            else
                pref.getString(FETCH_DIR, DEFAULT_FETCH_DIR)!!
        }

        /**
         * Set - Recorded audio already exist then show info message
         */
        fun setRecordInfoShowed() {
            pref.edit().apply {
                putBoolean(RECORD_INFO_SHOWED, true)
                apply()
            }
        }

        /**
         * Getting force update flag value for version 4
         * @return Boolean
         */
        fun getRecordInfoShowed(): Boolean {
            return pref.getBoolean(RECORD_INFO_SHOWED, false)
        }

        /**
         * Set notification permission requested flag
         */
        fun setNotificationPermissionRequested() {
            pref.edit().apply {
                putBoolean(NOTIFICATION_PERMISSION_REQUESTED, true)
                apply()
            }
        }

        /**
         * Check if notification permission has been requested before
         * @return Boolean
         */
        fun hasRequestedNotificationPermission(): Boolean {
            return pref.getBoolean(NOTIFICATION_PERMISSION_REQUESTED, false)
        }
    }
}