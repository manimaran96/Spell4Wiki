package com.manimaran.wikiaudio.activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.model.SliderPage;
import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.utils.GeneralUtils;
import com.manimaran.wikiaudio.utils.PrefManager;

public class AppIntroActivity extends AppIntro {

    private PrefManager pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pref = new PrefManager(getApplicationContext());

        SliderPage sliderPage1 = new SliderPage();
        sliderPage1.setTitle("Welcome");
        sliderPage1.setTitleColor(ContextCompat.getColor(getApplicationContext(), R.color.w_blue));
        sliderPage1.setDescription("Spell4Wiki app to record and upload audios for Wiki commons.\nAlso act as wiki dictionary.");
        sliderPage1.setDescColor(ContextCompat.getColor(getApplicationContext(), R.color.w_blue));
        sliderPage1.setImageDrawable(R.drawable.ic_spell4wiki);
        sliderPage1.setBgColor(ContextCompat.getColor(getApplicationContext(), R.color.app_intro_bg));

        SliderPage sliderPage2 = new SliderPage();
        sliderPage2.setTitle("Wiktionary Explore");
        sliderPage2.setTitleColor(ContextCompat.getColor(getApplicationContext(), R.color.w_blue));
        sliderPage2.setDescription("You can search and view words meaning form wikitionary for any languages.");
        sliderPage2.setDescColor(ContextCompat.getColor(getApplicationContext(), R.color.w_blue));
        sliderPage2.setImageDrawable(R.drawable.ic_spell4explore);
        sliderPage2.setBgColor(ContextCompat.getColor(getApplicationContext(), R.color.app_intro_bg));

        SliderPage sliderPage3 = new SliderPage();
        sliderPage3.setTitle("Spell For Word List");
        sliderPage3.setTitleColor(ContextCompat.getColor(getApplicationContext(), R.color.w_blue));
        sliderPage3.setDescription("You can record and upload audios for your own word list from your device.");
        sliderPage3.setDescColor(ContextCompat.getColor(getApplicationContext(), R.color.w_blue));
        sliderPage3.setImageDrawable(R.drawable.ic_spell4wordlist);
        sliderPage3.setBgColor(ContextCompat.getColor(getApplicationContext(), R.color.app_intro_bg));

        SliderPage sliderPage4 = new SliderPage();
        sliderPage4.setTitle("Spell For Word");
        sliderPage4.setTitleColor(ContextCompat.getColor(getApplicationContext(), R.color.w_blue));
        sliderPage4.setDescription("You can record and upload audio for your custom word.");
        sliderPage4.setDescColor(ContextCompat.getColor(getApplicationContext(), R.color.w_blue));
        sliderPage4.setImageDrawable(R.drawable.ic_spell4word);
        sliderPage4.setBgColor(ContextCompat.getColor(getApplicationContext(), R.color.app_intro_bg));


        SliderPage sliderPage5 = new SliderPage();
        sliderPage5.setTitle("Permission Request");
        sliderPage5.setTitleColor(ContextCompat.getColor(getApplicationContext(), R.color.w_blue));
        sliderPage5.setDescription("Spell4Wiki requires storage and record audio permissions.\nAllow those permissions");
        sliderPage5.setDescColor(ContextCompat.getColor(getApplicationContext(), R.color.w_blue));
        sliderPage5.setImageDrawable(R.drawable.ic_spell4wiki);
        sliderPage5.setBgColor(ContextCompat.getColor(getApplicationContext(), R.color.app_intro_bg));


        addSlide(AppIntroFragment.newInstance(sliderPage1));
        addSlide(AppIntroFragment.newInstance(sliderPage2));
        addSlide(AppIntroFragment.newInstance(sliderPage3));
        addSlide(AppIntroFragment.newInstance(sliderPage4));
        addSlide(AppIntroFragment.newInstance(sliderPage5));


        // OPTIONAL METHODS
        // Override bar/separator color.
        //setBarColor(Color.parseColor("#3F51B5"));
        //setSeparatorColor(Color.parseColor("#2196F3"));
        showSeparator(false);

        showStatusBar(false);
        showPagerIndicator(true);
        showSkipButton(false);
        setDepthAnimation();

        setColorSkipButton(Color.BLACK);
        setIndicatorColor(Color.GRAY, Color.BLACK);
        setColorDoneText(Color.BLACK);
        setNextArrowColor(Color.BLACK);

        //askForPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 5);

    }


    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);

        // Ask required permission on done pressed
        if (!GeneralUtils.checkPermissionGranted((this)) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE}
                    , 200);
        } else
            openMainActivity();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 200)
            openMainActivity();
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        openMainActivity();
    }

    private void openMainActivity() {
        pref.setFirstTimeLaunch(false);
        finish();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }
}
