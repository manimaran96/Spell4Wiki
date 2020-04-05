package com.manimaran.wikiaudio.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.constants.Constants;
import com.manimaran.wikiaudio.constants.Urls;
import com.manimaran.wikiaudio.fragments.LanguageSelectionFragment;
import com.manimaran.wikiaudio.listerners.OnLanguageSelectionListener;
import com.manimaran.wikiaudio.utils.GeneralUtils;
import com.manimaran.wikiaudio.utils.PrefManager;

import static com.manimaran.wikiaudio.constants.EnumTypeDef.ListMode;


public class Spell4WordActivity extends AppCompatActivity {

    private EditText editSpell4Word;

    private PrefManager pref;
    private String languageCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spell_4_word);

        initUI();

        pref = new PrefManager(this);
        languageCode = pref.getLanguageCodeSpell4Word();

    }

    private void openWiktionaryPage(String wordInfo) {
        Intent intent = new Intent(getApplicationContext(), CommonWebActivity.class);
        String url = String.format(Urls.WIKTIONARY_WEB, pref.getLanguageCodeSpell4Word(), wordInfo);
        intent.putExtra(Constants.TITLE, wordInfo);
        intent.putExtra(Constants.URL, url);
        intent.putExtra(Constants.IS_WIKTIONARY_WORD, true);
        intent.putExtra(Constants.LANGUAGE_CODE, pref.getLanguageCodeSpell4Word());
        startActivity(intent);
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initUI() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.spell4word));
        }

        Button btnRecord = findViewById(R.id.btn_record);
        editSpell4Word = findViewById(R.id.editWord);

        editSpell4Word.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (editSpell4Word.getRight() - editSpell4Word.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    if (!TextUtils.isEmpty(editSpell4Word.getText()) && editSpell4Word.getText().length() < 30)
                        openWiktionaryPage(editSpell4Word.getText().toString());
                    else
                        GeneralUtils.showSnack(editSpell4Word, "Enter valid word");
                    return true;
                }
            }
            return false;
        });


        btnRecord.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(editSpell4Word.getText()) && editSpell4Word.getText().length() < 30) {
                GeneralUtils.showRecordDialog(Spell4WordActivity.this, editSpell4Word.getText().toString().trim(), languageCode);
            } else
                GeneralUtils.showSnack(editSpell4Word, "Enter valid word");
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


    private void loadLanguages() {
        OnLanguageSelectionListener callback = langCode -> {
            languageCode = langCode;
            invalidateOptionsMenu();
        };
        LanguageSelectionFragment languageSelectionFragment = new LanguageSelectionFragment();
        languageSelectionFragment.init(callback, ListMode.SPELL_4_WORD);
        languageSelectionFragment.show(getSupportFragmentManager(), languageSelectionFragment.getTag());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.spell4wiki_view_menu, menu);
        return true;
    }

    private void setupLanguageSelectorMenuItem(Menu menu) {
        MenuItem item = menu.findItem(R.id.menu_lang_selector);
        item.setVisible(true);
        View rootView = item.getActionView();
        TextView selectedLang = rootView.findViewById(R.id.txtSelectedLanguage);
        selectedLang.setText(this.languageCode.toUpperCase());
        rootView.setOnClickListener(v -> {
            loadLanguages();
        });

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        setupLanguageSelectorMenuItem(menu);
        return super.onPrepareOptionsMenu(menu);
    }
}

