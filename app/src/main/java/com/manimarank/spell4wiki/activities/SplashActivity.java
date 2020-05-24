package com.manimarank.spell4wiki.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import com.manimaran.crash_reporter.CrashReporter;
import com.manimaran.crash_reporter.interfaces.CrashAlertClickListener;
import com.manimaran.crash_reporter.utils.CrashUtil;
import com.manimarank.spell4wiki.R;
import com.manimarank.spell4wiki.utils.PrefManager;
import com.manimarank.spell4wiki.utils.SyncHelper;

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

        Button btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(v -> {
            callNextScreen();
        });
        btnStart.setVisibility(View.GONE);

        //splash screen will be shown for 1.5 seconds
        int SPLASH_DISPLAY_TIME = 1000;

        new Handler().postDelayed(() -> {
            try {
                CrashAlertClickListener listener = new CrashAlertClickListener() {
                    @Override
                    public void onOkClick() {
                        btnStart.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancelClick() {
                        callNextScreen();
                    }
                };
                if(CrashUtil.Companion.isHaveCrashData()){

                    CrashReporter.INSTANCE.showAlertDialogForShareCrash(this, listener,true);
                }else
                    callNextScreen();
            }catch (Exception e){
                e.printStackTrace();
                callNextScreen();
            }
        }, SPLASH_DISPLAY_TIME);

        // Sync Wiki Languages
        new SyncHelper(this).syncWikiLanguages();

    }

    private void callNextScreen() {
        // If app launch very first time to show the app intro. Other wise got ot login page
        Intent mainIntent = new Intent(SplashActivity.this, pref.isFirstTimeLaunch() ? AppIntroActivity.class : LoginActivity.class);
        startActivity(mainIntent);
        finish();
    }


}

