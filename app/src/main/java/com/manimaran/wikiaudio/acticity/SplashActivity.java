package com.manimaran.wikiaudio.acticity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.manimaran.wikiaudio.R;

/**
 * Splash screen activity
 */
public class SplashActivity extends Activity {

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_splash);

        //splash screen will be shown for 1.5 seconds
        int SPLASH_DISPLAY_LENGTH = 800;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Todo : If login go Spell4Wiktionary else Ask for login
                Intent mainIntent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(mainIntent);
                finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}

