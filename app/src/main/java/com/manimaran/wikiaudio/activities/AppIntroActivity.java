package com.manimaran.wikiaudio.activities;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

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

        int titleColor = ContextCompat.getColor(getApplicationContext(), R.color.app_intro_title);
        int descriptionColor = ContextCompat.getColor(getApplicationContext(), R.color.app_intro_description);
        int bgColor = ContextCompat.getColor(getApplicationContext(), R.color.app_intro_bg);

        SliderPage sliderPage1 = new SliderPage();
        sliderPage1.setTitle(getString(R.string.app_intro_slide_1_title));
        sliderPage1.setTitleColor(titleColor);
        sliderPage1.setDescription(getString(R.string.app_intro_slide_1_description));
        sliderPage1.setDescColor(descriptionColor);
        sliderPage1.setImageDrawable(R.drawable.ic_spell4wiki);
        sliderPage1.setBgColor(bgColor);

        SliderPage sliderPage2 = new SliderPage();
        sliderPage2.setTitle(getString(R.string.app_intro_slide_2_title));
        sliderPage2.setTitleColor(titleColor);
        sliderPage2.setDescription(getString(R.string.app_intro_slide_2_description));
        sliderPage2.setDescColor(descriptionColor);
        sliderPage2.setImageDrawable(R.drawable.ic_spell4explore);
        sliderPage2.setBgColor(bgColor);

        SliderPage sliderPage3 = new SliderPage();
        sliderPage3.setTitle(getString(R.string.app_intro_slide_3_title));
        sliderPage3.setTitleColor(titleColor);
        sliderPage3.setDescription(getString(R.string.app_intro_slide_3_description));
        sliderPage3.setDescColor(descriptionColor);
        sliderPage3.setImageDrawable(R.drawable.ic_spell4word_list);
        sliderPage3.setBgColor(bgColor);

        SliderPage sliderPage4 = new SliderPage();
        sliderPage4.setTitle(getString(R.string.app_intro_slide_4_title));
        sliderPage4.setTitleColor(titleColor);
        sliderPage4.setDescription(getString(R.string.app_intro_slide_4_description));
        sliderPage4.setDescColor(descriptionColor);
        sliderPage4.setImageDrawable(R.drawable.ic_spell4word);
        sliderPage4.setBgColor(bgColor);


        SliderPage sliderPage5 = new SliderPage();
        sliderPage5.setTitle(getString(R.string.app_intro_slide_5_title));
        sliderPage5.setTitleColor(titleColor);
        sliderPage5.setDescription(getString(R.string.app_intro_slide_5_description));
        sliderPage5.setDescColor(descriptionColor);
        sliderPage5.setImageDrawable(R.drawable.ic_spell4wiki);
        sliderPage5.setBgColor(bgColor);


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
        setNextArrowColor(Color.DKGRAY);

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
