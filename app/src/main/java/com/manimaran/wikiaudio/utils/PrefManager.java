package com.manimaran.wikiaudio.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.activities.LoginActivity;

import java.util.Set;

public class PrefManager {
    // shared pref mode
    private static final int PRIVATE_MODE = 0;
    // Kay values
    private static final String KEY_NAME = "name";
    private static final String KEY_COOKIE = "cookie";
    private static final String IS_LOGIN = "is_login";
    private static final String CONTRIBUTION_LANG_CODE = "contribution_lang_code";
    private static final String WIKTIONARY_LANG_CODE = "wiktionary_lang_code";
    private static final String LANGUAGE_CODE_SPELL_4_WIKI = "language_code_spell_4_wiki";
    private static final String LANGUAGE_CODE_SPELL_4_WORD_LIST = "language_code_spell_4_word_list";
    private static final String LANGUAGE_CODE_SPELL_4_WORD = "language_code_spell_4_word";
    private static final String LANGUAGE_CODE_WIKTIONARY = "language_code_wiktionary";
    private static final String TITLE_WORDS_WITHOUT_AUDIO = "title_words_without_audio";
    private static final String CSRF_TOKEN = "csrf_token";
    private static final String IS_FIRST_TIME_LAUNCH = "is_first_time_launch";
    private static final String IS_ANONYMOUS = "is_anonymous"; // Only wiktionay use
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context mContext;


    public PrefManager(Context context) {
        this.mContext = context;
        pref = mContext.getSharedPreferences(mContext.getString(R.string.pref_file_key), PRIVATE_MODE);
        editor = pref.edit();
        editor.apply();
    }

    // Is Login
    public void setSession(String name, boolean isLogin) {
        editor.putString(KEY_NAME, name);
        editor.putBoolean(IS_LOGIN, isLogin);
        editor.putBoolean(IS_ANONYMOUS, false);
        editor.commit();
    }

    public String getName() {
        return pref.getString(KEY_NAME, null);
    }

    public Boolean isIsLogin() {
        return pref.getBoolean(IS_LOGIN, false);
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    // First time launch
    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public String getCsrfToken() {
        return pref.getString(CSRF_TOKEN, null);
    }

    public void setCsrfToken(String editToken) {
        editor.putString(CSRF_TOKEN, editToken);
        editor.commit();
    }

    public String getContributionLangCode() {
        return pref.getString(CONTRIBUTION_LANG_CODE, "ta");
    }

    public void setContributionLangCode(String code) {
        editor.putString(CONTRIBUTION_LANG_CODE, code);
        editor.commit();
    }

    public String getWiktionaryLangCode() {
        return pref.getString(WIKTIONARY_LANG_CODE, "ta");
    }

    public void setWiktionaryLangCode(String code) {
        editor.putString(WIKTIONARY_LANG_CODE, code);
        editor.commit();
    }

    public String getTitleWordsWithoutAudio() {
        return pref.getString(TITLE_WORDS_WITHOUT_AUDIO, "பகுப்பு:தமிழ்-ஒலிக்கோப்புகளில்லை");
    }

    public void setTitleWordsWithoutAudio(String titleWordsWithoutAudio) {
        editor.putString(TITLE_WORDS_WITHOUT_AUDIO, titleWordsWithoutAudio);
        editor.commit();
    }

    public Boolean getIsAnonymous() {
        return pref.getBoolean(IS_ANONYMOUS, false);
    }

    public void setIsAnonymous(Boolean isAnonymous) {
        editor.putBoolean(IS_ANONYMOUS, isAnonymous);
        editor.commit();
    }

    /**
     * Clear session details when click logout
     */
    public void logoutUser() {
        // Clearing all data from Shared Preferences
        boolean isFirstTime = isFirstTimeLaunch();
        editor.clear();
        setFirstTimeLaunch(isFirstTime);
        editor.commit();

        if(mContext !=null) {
            Activity activity = (Activity) mContext;
            // After logout redirect user to Login Activity
            activity.finishAffinity();
            activity.startActivity(new Intent(mContext, LoginActivity.class));
        }
    }

    public void setCookies(Set<String> cookieList) {
        Set<String> existigCookie = getCookies();
        if (existigCookie != null && existigCookie.size() > 0)
            cookieList.addAll(existigCookie);
        editor.putStringSet(KEY_COOKIE, cookieList);
        editor.commit();
        Log.e("TAG", "Addin " + getCookies().toString());
    }

    public Set<String> getCookies() {
        return pref.getStringSet(KEY_COOKIE, null);
    }


    public void setLanguageCodeSpell4Wiki(String languageCode){
        editor.putString(LANGUAGE_CODE_SPELL_4_WIKI, languageCode);
        editor.commit();
    }

    public void setLanguageCodeSpell4WordList(String languageCode){
        editor.putString(LANGUAGE_CODE_SPELL_4_WORD_LIST, languageCode);
        editor.commit();
    }

    public void setLanguageCodeSpell4Word(String languageCode){
        editor.putString(LANGUAGE_CODE_SPELL_4_WORD, languageCode);
        editor.commit();
    }

    public void setLanguageCodeWiktionary(String languageCode){
        editor.putString(LANGUAGE_CODE_WIKTIONARY, languageCode);
        editor.commit();
    }

    public String getLanguageCodeSpell4Wiki() {
        return pref.getString(LANGUAGE_CODE_SPELL_4_WIKI, "ta");
    }

    public String getLanguageCodeSpell4WordList() {
        return pref.getString(LANGUAGE_CODE_SPELL_4_WORD_LIST, "ta");
    }

    public String getLanguageCodeSpell4Word() {
        return pref.getString(LANGUAGE_CODE_SPELL_4_WORD, "ta");
    }

    public String getLanguageCodeWiktionary() {
        return pref.getString(LANGUAGE_CODE_WIKTIONARY, "ta");
    }

}