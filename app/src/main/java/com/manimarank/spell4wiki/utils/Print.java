package com.manimarank.spell4wiki.utils;

import android.util.Log;

import com.manimarank.spell4wiki.BuildConfig;

public class Print {

    private final static String TAG = "Spell4Wiki - App";

    public static void log(String message){
        if(BuildConfig.DEBUG){
            Log.d(TAG, message);
        }
    }

    public static void error(String message){
        if(BuildConfig.DEBUG){
            Log.e(TAG, message);
        }
    }
}
