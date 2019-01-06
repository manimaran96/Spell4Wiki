package com.manimaran.wikiaudio.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.manimaran.wikiaudio.R;

public class PrefManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;
 
    // shared pref mode
    private int PRIVATE_MODE = 0;
 
    // Shared preferences file name
    private static String PREF_NAME = null;
 
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
 
    public PrefManager(Context context) {
        this._context = context;
        PREF_NAME = _context.getString(R.string.pref_file_key);
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }
 
    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }
 
    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }
 
}