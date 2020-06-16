package com.manimarank.spell4wiki.activities;

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
import com.manimarank.spell4wiki.activities.base.BaseActivity;
import com.manimarank.spell4wiki.utils.NetworkUtils;
import com.manimarank.spell4wiki.utils.PrefManager;
import com.manimarank.spell4wiki.utils.SnackBarUtils;
import com.manimarank.spell4wiki.utils.SyncHelper;

/**
 * Splash screen activity
 */
public class SplashActivity extends BaseActivity {

    private PrefManager pref;
    private Button btnStart;
    private boolean isNetworkFail = false;

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

        btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(v -> {
            if (NetworkUtils.INSTANCE.isConnected(getApplicationContext())) {
                btnStart.setVisibility(View.GONE);
                if (isNetworkFail) {
                    loadSplash();
                } else {
                    callNextScreen();
                }
            } else
                SnackBarUtils.INSTANCE.showNormal(btnStart, getString(R.string.check_internet));
        });
        btnStart.setVisibility(View.GONE);

        if (NetworkUtils.INSTANCE.isConnected(getApplicationContext())) {
            loadSplash();
        } else {
            SnackBarUtils.INSTANCE.showNormal(btnStart, getString(R.string.check_internet));
            isNetworkFail = true;
            btnStart.setVisibility(View.VISIBLE);
        }

    }

    private void loadSplash() {
        isNetworkFail = false;
        //splash screen will be shown for 1 second
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
                if (CrashUtil.Companion.isHaveCrashData()) {
                    CrashReporter.INSTANCE.showAlertDialogForShareCrash(this, listener, true);
                } else
                    callNextScreen();
            } catch (Exception e) {
                e.printStackTrace();
                callNextScreen();
            }
        }, SPLASH_DISPLAY_TIME);

        // Sync Wiki Languages
        new SyncHelper(this).syncWikiLanguages();
    }

    private void callNextScreen() {
        // If app launch very first time to show the language selection and app intro. Other wise go to login page
        Intent mainIntent = new Intent(SplashActivity.this, pref.isFirstTimeLaunch() ? LanguageSelectionActivity.class : LoginActivity.class);
        startActivity(mainIntent);
        finish();
    }


}

