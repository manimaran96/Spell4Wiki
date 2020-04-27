package com.manimaran.wikiaudio.utils;

import android.util.Log;

import com.manimaran.wikiaudio.BuildConfig;

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
