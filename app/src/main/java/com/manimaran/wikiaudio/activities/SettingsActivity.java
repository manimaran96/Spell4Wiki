package com.manimaran.wikiaudio.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.fragments.BottomSheetFragment;
import com.manimaran.wikiaudio.listerners.CallBackListener;
import com.manimaran.wikiaudio.utils.PrefManager;


public class SettingsActivity extends AppCompatActivity {

    private PrefManager pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle(getString(R.string.settings));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TextView txtSpell4WikiLang = findViewById(R.id.txtSpell4WikiLang);
        TextView txtSpell4WordListLang = findViewById(R.id.txtSpell4WordListLang);
        TextView txtSpell4WordLang = findViewById(R.id.txtSpell4WordLang);
        TextView txtWiktionaryLang = findViewById(R.id.txtWiktionaryLang);
        TextView txtLicenseOfUploadAudio = findViewById(R.id.txtLicenseOfUploadAudio);


        View layoutSpell4WikiLang = findViewById(R.id.layoutSpell4WikiLang);
        View layoutSpell4WordListLang = findViewById(R.id.layoutSpell4WordListLang);
        View layoutSpell4WordLang = findViewById(R.id.layoutSpell4WordLang);
        View layoutWiktionaryLang = findViewById(R.id.layoutWiktionaryLang);
        View layoutLicenseOfUploadAudio = findViewById(R.id.layoutLicenseOfUploadAudio);


        pref = new PrefManager(getApplicationContext());
        txtSpell4WikiLang.setText(pref.getLanguageCodeSpell4Wiki());
        txtSpell4WordListLang.setText(pref.getLanguageCodeSpell4WordList());
        txtSpell4WordLang.setText(pref.getLanguageCodeSpell4Word());
        txtWiktionaryLang.setText(pref.getLanguageCodeWiktionary());

        layoutSpell4WikiLang.setOnClickListener(v -> {
            BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
            CallBackListener callback = langCode -> {
                pref.setLanguageCodeSpell4Wiki(langCode);
                txtSpell4WikiLang.setText(pref.getLanguageCodeSpell4Wiki());
            };
            bottomSheetFragment.setCalBack(callback);
            bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
            bottomSheetFragment.setCancelable(false);
        });

        layoutSpell4WordListLang.setOnClickListener(v -> {
            BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
            CallBackListener callback = langCode -> {
                pref.setLanguageCodeSpell4WordList(langCode);
                txtSpell4WordListLang.setText(pref.getLanguageCodeSpell4WordList());
            };
            bottomSheetFragment.setCalBack(callback);
            bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
            bottomSheetFragment.setCancelable(false);
            bottomSheetFragment.setIsTempMode(true);
        });


        layoutSpell4WordLang.setOnClickListener(v -> {
            BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
            CallBackListener callback = langCode -> {
                pref.setLanguageCodeSpell4Word(langCode);
                txtSpell4WordLang.setText(pref.getLanguageCodeSpell4Word());
            };
            bottomSheetFragment.setCalBack(callback);
            bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
            bottomSheetFragment.setCancelable(false);
            bottomSheetFragment.setIsTempMode(true);
        });

        layoutWiktionaryLang.setOnClickListener(v -> {
            BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
            CallBackListener callback = langCode -> {
                pref.setLanguageCodeWiktionary(langCode);
                txtWiktionaryLang.setText(pref.getLanguageCodeWiktionary());
            };
            bottomSheetFragment.setCalBack(callback);
            bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
            bottomSheetFragment.setCancelable(false);
            bottomSheetFragment.setIsTempMode(true);
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }

}
