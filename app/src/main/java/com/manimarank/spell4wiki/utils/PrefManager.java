package com.manimarank.spell4wiki.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.manimarank.spell4wiki.R;
import com.manimarank.spell4wiki.activities.LoginActivity;
import com.manimarank.spell4wiki.auth.AccountUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PrefManager {
    // shared pref
    private static final String PREF_NAME = "spell4wiki_pref";
    private static final int PRIVATE_MODE = 0;
    // Kay values
    private static final String USERNAME = "username";
    private static final String IS_LOGGED_IN_USER = "is_logged_in_user";
    private static final String IS_ANONYMOUS_USER = "is_anonymous_user"; // Only wiktionary use
    private static final String IS_FIRST_TIME_LAUNCH = "is_first_time_launch";
    private static final String LANGUAGE_CODE_SPELL_4_WIKI = "language_code_spell_4_wiki";
    private static final String LANGUAGE_CODE_SPELL_4_WORD_LIST = "language_code_spell_4_word_list";
    private static final String LANGUAGE_CODE_SPELL_4_WORD = "language_code_spell_4_word";
    private static final String LANGUAGE_CODE_WIKTIONARY = "language_code_wiktionary";
    private static final String LANGUAGE_CODE_APP = "language_code_app";
    private static final String UPLOAD_AUDIO_LICENSE = "upload_audio_license";
    private static final String COMMON_CATEGORIES = "common_categories";
    private static final String CSRF_TOKEN = "csrf_token";
    private static final String COOKIE = "cookie";
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context mContext;


    public PrefManager(Context context) {
        this.mContext = context;
        pref = mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
        editor.apply();
    }

    // Is Login
    public void setUserSession(String name) {
        editor.putString(USERNAME, name);
        editor.putBoolean(IS_LOGGED_IN_USER, true);
        editor.putBoolean(IS_ANONYMOUS_USER, false);
        editor.apply();
    }

    public String getName() {
        return pref.getString(USERNAME, null);
    }

    public Boolean isLoggedIn() {
        return !pref.getBoolean(IS_ANONYMOUS_USER, false) && pref.getBoolean(IS_LOGGED_IN_USER, false) && AccountUtils.isLoggedIn();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    // First time launch for App Intro
    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.apply();
    }

    public String getCsrfToken() {
        return pref.getString(CSRF_TOKEN, null);
    }

    public void setCsrfToken(String editToken) {
        editor.putString(CSRF_TOKEN, editToken);
        editor.apply();
    }

    public Boolean getIsAnonymous() {
        return pref.getBoolean(IS_ANONYMOUS_USER, false);
    }

    public void setIsAnonymous(Boolean isAnonymous) {
        editor.putBoolean(IS_ANONYMOUS_USER, isAnonymous);
        editor.apply();
    }

    /**
     * Clear session details when click logout
     */
    public void logoutUser() {
        boolean isFirstTime = isFirstTimeLaunch();
        String appLanguage = getAppLanguage();
        editor.clear();
        setFirstTimeLaunch(isFirstTime);
        setAppLanguage(appLanguage);

        // Remove sync - authenticator account
        AccountUtils.removeAccount();

        if (mContext != null && mContext instanceof Activity) {
            Activity activity = (Activity) mContext;
            // After logout redirect user to Login Activity
            activity.finishAffinity();
            activity.startActivity(new Intent(mContext, LoginActivity.class));
        }
    }

    private Set<String> getCookies() {
        return pref.getStringSet(COOKIE, null);
    }

    public void setCookies(Set<String> cookieList) {
        Set<String> existingCookie = getCookies();
        if (existingCookie != null && existingCookie.size() > 0)
            cookieList.addAll(existingCookie);
        editor.putStringSet(COOKIE, cookieList);
        editor.apply();
    }

    public String getLanguageCodeSpell4Wiki() {
        return pref.getString(LANGUAGE_CODE_SPELL_4_WIKI, "ta");
    }

    public void setLanguageCodeSpell4Wiki(String languageCode) {
        editor.putString(LANGUAGE_CODE_SPELL_4_WIKI, languageCode);
        editor.apply();
    }

    public String getLanguageCodeSpell4WordList() {
        return pref.getString(LANGUAGE_CODE_SPELL_4_WORD_LIST, "ta");
    }

    public void setLanguageCodeSpell4WordList(String languageCode) {
        editor.putString(LANGUAGE_CODE_SPELL_4_WORD_LIST, languageCode);
        editor.apply();
    }

    public String getLanguageCodeSpell4Word() {
        return pref.getString(LANGUAGE_CODE_SPELL_4_WORD, "ta");
    }

    public void setLanguageCodeSpell4Word(String languageCode) {
        editor.putString(LANGUAGE_CODE_SPELL_4_WORD, languageCode);
        editor.apply();
    }

    public String getLanguageCodeWiktionary() {
        return pref.getString(LANGUAGE_CODE_WIKTIONARY, "ta");
    }

    public void setLanguageCodeWiktionary(String languageCode) {
        editor.putString(LANGUAGE_CODE_WIKTIONARY, languageCode);
        editor.apply();
    }

    public String getUploadAudioLicense() {
        return pref.getString(UPLOAD_AUDIO_LICENSE, WikiLicense.LicensePrefs.CC_0);
    }

    public void setUploadAudioLicense(String uploadAudioLicense) {
        editor.putString(UPLOAD_AUDIO_LICENSE, uploadAudioLicense);
        editor.apply();
    }

    void setCommonCategories(List<String> categoryCommon) {
        editor.putStringSet(COMMON_CATEGORIES, new HashSet<>(categoryCommon));
        editor.apply();
    }

    public void setAppLanguage(String language){
        editor.putString(LANGUAGE_CODE_APP, language);
        editor.apply();
    }

    public String getAppLanguage() {
        return pref.getString(LANGUAGE_CODE_APP, "en");
    }


    public Set<String> getCommonCategories(){
        return pref.getStringSet(COMMON_CATEGORIES, null);
    }
}