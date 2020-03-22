package com.manimaran.wikiaudio.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.utils.PrefManager;
import com.manimaran.wikiaudio.utils.SyncHelper;

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

        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom);
        ImageView splash = findViewById(R.id.img_splash);
        splash.startAnimation(animation);

        //splash screen will be shown for 1.5 seconds
        int SPLASH_DISPLAY_LENGTH = 1000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // If app launch very first time to show the app intro. Other wise got ot login page
                Intent mainIntent = new Intent(SplashActivity.this, pref.isFirstTimeLaunch() ? AppIntroActivity.class : LoginActivity.class);
                startActivity(mainIntent);
                finish();
            }
        }, SPLASH_DISPLAY_LENGTH);

        // Sync Wiki Languages
        new SyncHelper(this).syncWikiLanguages();

    }


}

