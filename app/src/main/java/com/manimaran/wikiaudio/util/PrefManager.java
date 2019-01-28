package com.manimaran.wikiaudio.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.acticity.LoginActivity;
import com.manimaran.wikiaudio.wiki.ServiceGenerator;

public class PrefManager {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context mContext;

    // shared pref mode
    private static final int PRIVATE_MODE = 0;

    // Kay values
    private static final String KEY_NAME = "name";
    private static final String IS_LOGIN = "is_login";
    private static final String LANG_CODE = "lang_code";
    private static final String CSRF_TOKEN = "csrf_token";
    private static final String IS_FIRST_TIME_LAUNCH = "is_first_time_launch";
    private static final String IS_ANONYMOUS = "is_anonymous"; // Only wiktionay use

 
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
        editor.commit();
    }

    public String getName()
    {
        return pref.getString(KEY_NAME, null);
    }

    public Boolean isIsLogin() {
        return pref.getBoolean(IS_LOGIN, false);
    }


    // First time launch
    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }
 
    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    public void setCsrfToken(String editToken)
    {
        editor.putString(CSRF_TOKEN, editToken);
        editor.commit();
    }

    public String getCsrfToken() { return pref.getString(CSRF_TOKEN,null); }


    public void setLangCode(String code)
    {
        editor.putString(LANG_CODE, code);
        editor.commit();
    }

    public String getLangCode() { return pref.getString(LANG_CODE,"en"); }

    public void setIsAnonymous(Boolean isAnonymous)
    {
        editor.putBoolean(IS_ANONYMOUS, isAnonymous);
        editor.commit();
    }

    public Boolean getIsAnonymous()
    {
        return pref.getBoolean(IS_ANONYMOUS, false);
    }

    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     */
    public void checkLogin() {
        // Check login status
        if (!isIsLogin()) {
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(mContext, LoginActivity.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            mContext.startActivity(i);
        }

    }

    /**
     * Clear session details when click logout
     */
    void logoutUser() {
        // Clearing all data from Shared Preferences
        editor.clear();
        setFirstTimeLaunch(true); // Already done
        editor.commit();
        ServiceGenerator.clearCookies(); // Clear Cookies

        // After logout redirect user to Login Activity
        checkLogin();
    }

}