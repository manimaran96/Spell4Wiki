package com.manimarank.spell4wiki.data.prefs

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.manimarank.spell4wiki.data.auth.AccountUtils
import com.manimarank.spell4wiki.ui.login.LoginActivity
import com.manimarank.spell4wiki.utils.WikiLicense
import com.manimarank.spell4wiki.utils.constants.AppConstants

/**
 * Preference utility class for App level settings and login related preference
 */
class PrefManager(private val mContext: Context?) {
    private val pref: SharedPreferences
    private val editor: SharedPreferences.Editor

    /**
     * Set user logged in flag
     * @param name String?
     */
    fun setUserSession(name: String?) {
        editor.putString(USERNAME, name)
        editor.putBoolean(IS_LOGGED_IN_USER, true)
        editor.putBoolean(IS_ANONYMOUS_USER, false)
        editor.apply()
    }

    val name: String?
        get() = pref.getString(USERNAME, null)
    val isLoggedIn: Boolean
        get() = !pref.getBoolean(IS_ANONYMOUS_USER, false) && pref.getBoolean(IS_LOGGED_IN_USER, false) && AccountUtils.isLoggedIn

    // First time launch for App Intro
    var isFirstTimeLaunch: Boolean
        get() = pref.getBoolean(IS_FIRST_TIME_LAUNCH, true)
        set(isFirstTime) {
            editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime)
            editor.apply()
        }
    var csrfToken: String?
        get() = pref.getString(CSRF_TOKEN, null)
        set(editToken) {
            editor.putString(CSRF_TOKEN, editToken)
            editor.apply()
        }
    var isAnonymous: Boolean?
        get() = pref.getBoolean(IS_ANONYMOUS_USER, false)
        set(isAnonymous) {
            editor.putBoolean(IS_ANONYMOUS_USER, isAnonymous!!)
            editor.apply()
        }

    /**
     * Clear session details when click logout
     */
    fun logoutUser() {
        clearLoginData()
        if (mContext != null && mContext is Activity) {
            val activity = mContext
            // After logout redirect user to Login Activity
            activity.finishAffinity()
            activity.startActivity(Intent(mContext, LoginActivity::class.java))
        }
    }

    /**
     * Clear login information once logged out
     */
    fun clearLoginData() {
        val isFirstTime = isFirstTimeLaunch
        editor.clear()
        isFirstTimeLaunch = isFirstTime

        // Remove sync - authenticator account
        AccountUtils.removeAccount()
    }

    private val cookies: Set<String>?
        get() = pref.getStringSet(COOKIE, null)

    /**
     * Set cookie information
     * @param cookieList MutableSet<String?>
     */
    fun setCookies(cookieList: MutableSet<String?>) {
        val existingCookie = cookies
        if (existingCookie != null && existingCookie.size > 0) cookieList.addAll(existingCookie)
        editor.putStringSet(COOKIE, cookieList)
        editor.apply()
    }

    var languageCodeSpell4WikiAll: String?
        get() = pref.getString(SPELL_4_WIKI_ALL, "ta")
        set(languageCode) {
            editor.putString(SPELL_4_WIKI_ALL, languageCode)
            editor.apply()
        }

    var uploadAudioLicense: String?
        get() = pref.getString(UPLOAD_AUDIO_LICENSE, WikiLicense.LicensePrefs.CC_0)
        set(uploadAudioLicense) {
            editor.putString(UPLOAD_AUDIO_LICENSE, uploadAudioLicense)
            editor.apply()
        }

    var runFilterNumberOfWordsToCheck: Int?
        get() = pref.getInt(RUN_FILTER_NO_OF_WORDS_TO_CHECK, AppConstants.RUN_FILTER_NO_OF_WORDS_CHECK_COUNT)
        set(noOfWordCount) {
            editor.putInt(RUN_FILTER_NO_OF_WORDS_TO_CHECK, noOfWordCount ?: AppConstants.RUN_FILTER_NO_OF_WORDS_CHECK_COUNT)
            editor.apply()
        }

    var isWiktionaryCleanupEnabled: Boolean
        get() = pref.getBoolean(WIKTIONARY_CLEANUP_ENABLED, true)
        set(isEnabled) {
            editor.putBoolean(WIKTIONARY_CLEANUP_ENABLED, isEnabled)
            editor.apply()
        }

    fun getWordsCategoryList(langCode: String?): MutableList<String> {
        return (pref.getStringSet(String.format(WORDS_CATEGORY_FOR_LANG, langCode ?: "ta"), null) ?: mutableSetOf()).toMutableList()
    }

    fun setWordsCategoryList(langCode: String?, categoryList: MutableSet<String>?) {
        editor.putStringSet(String.format(WORDS_CATEGORY_FOR_LANG, langCode ?: "ta"), categoryList)
        editor.apply()
    }

    fun getSelectedWordsCategory(langCode: String?): String? {
        return pref.getString(String.format(SELECTED_WORDS_CATEGORY_FOR_LANG, langCode ?: "ta"), null)
    }

    fun setSelectedWordsCategory(langCode: String?, category: String?) {
        editor.putString(String.format(SELECTED_WORDS_CATEGORY_FOR_LANG, langCode ?: "ta"), category)
        editor.apply()
    }

    companion object {
        // shared pref
        private const val PREF_NAME = "spell4wiki_pref"

        // Key values
        private const val USERNAME = "username"
        private const val IS_LOGGED_IN_USER = "is_logged_in_user"
        private const val IS_ANONYMOUS_USER = "is_anonymous_user" // Only wiktionary use
        private const val IS_FIRST_TIME_LAUNCH = "is_first_time_launch"
        private const val SPELL_4_WIKI_ALL = "language_code_spell_4_wiki_all"
        private const val UPLOAD_AUDIO_LICENSE = "upload_audio_license"
        private const val RUN_FILTER_NO_OF_WORDS_TO_CHECK = "run_filter_no_of_words_to_check"
        private const val WORDS_CATEGORY_FOR_LANG = "words_category_for_%s"
        private const val SELECTED_WORDS_CATEGORY_FOR_LANG = "selected_words_category_for_%s"
        private const val CSRF_TOKEN = "csrf_token"
        private const val COOKIE = "cookie"
        private const val WIKTIONARY_CLEANUP_ENABLED = "wiktionary_cleanup_enabled"
    }

    init {
        pref = mContext!!.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        editor = pref.edit()
        editor.apply()
    }
}