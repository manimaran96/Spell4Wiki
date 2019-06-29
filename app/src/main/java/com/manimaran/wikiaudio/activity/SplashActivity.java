package com.manimaran.wikiaudio.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.utils.PrefManager;

/**
 * Splash screen activity
 */
public class SplashActivity extends Activity {

    private PrefManager pref;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_splash);

        pref = new PrefManager(getApplicationContext());

        //splash screen will be shown for 1.5 seconds
        int SPLASH_DISPLAY_LENGTH = 800;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // If app launch very first time to show the app intro. Other wise got ot login page
                Intent mainIntent = new Intent(SplashActivity.this, pref.isFirstTimeLaunch() ? AppIntroActivity.class : LoginActivity.class);
                startActivity(mainIntent);
                finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}

