package com.manimarank.spell4wiki.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.manimarank.spell4wiki.R;
import com.manimarank.spell4wiki.databases.DBHelper;
import com.manimarank.spell4wiki.databases.dao.WordsHaveAudioDao;
import com.manimarank.spell4wiki.fragments.LanguageSelectionFragment;
import com.manimarank.spell4wiki.listerners.OnLanguageSelectionListener;
import com.manimarank.spell4wiki.utils.GeneralUtils;
import com.manimarank.spell4wiki.utils.NetworkUtils;
import com.manimarank.spell4wiki.utils.PrefManager;
import com.manimarank.spell4wiki.utils.ShowCasePref;
import com.manimarank.spell4wiki.utils.SnackBarUtils;
import com.manimarank.spell4wiki.utils.constants.AppConstants;
import com.manimarank.spell4wiki.utils.constants.Urls;

import java.util.List;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetSequence;
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal;

import static com.manimarank.spell4wiki.utils.constants.EnumTypeDef.ListMode;


public class Spell4WordActivity extends AppCompatActivity {

    private EditText editSpell4Word;

    private PrefManager pref;
    private String languageCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spell_4_word);

        pref = new PrefManager(this);
        languageCode = pref.getLanguageCodeSpell4Word();

        initUI();
    }

    private void openWiktionaryPage(String wordInfo) {
        Intent intent = new Intent(getApplicationContext(), CommonWebActivity.class);
        String url = String.format(Urls.WIKTIONARY_WEB, pref.getLanguageCodeSpell4Word(), wordInfo);
        intent.putExtra(AppConstants.TITLE, wordInfo);
        intent.putExtra(AppConstants.URL, url);
        intent.putExtra(AppConstants.IS_WIKTIONARY_WORD, true);
        intent.putExtra(AppConstants.LANGUAGE_CODE, pref.getLanguageCodeSpell4Word());
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
                        SnackBarUtils.INSTANCE.showLong(editSpell4Word, getString(R.string.enter_valid_word));
                    return true;
                }
            }
            return false;
        });


        btnRecord.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(editSpell4Word.getText()) && editSpell4Word.getText().length() < 30) {
                if(NetworkUtils.INSTANCE.isConnected(getApplicationContext())) {
                    String word = editSpell4Word.getText().toString().trim();
                    if (isAllowRecord(word))
                        GeneralUtils.showRecordDialog(Spell4WordActivity.this, word, languageCode);
                    else
                        SnackBarUtils.INSTANCE.showLong(editSpell4Word, String.format(getString(R.string.audio_file_already_exist), word));
                }else
                    SnackBarUtils.INSTANCE.showLong(editSpell4Word, getString(R.string.check_internet));
            } else
                SnackBarUtils.INSTANCE.showLong(editSpell4Word, getString(R.string.enter_valid_word));
        });
    }

    private Boolean isAllowRecord(String word){
        boolean isValid = false;
        try {
            if(!pref.getIsAnonymous() && !TextUtils.isEmpty(word)){
                WordsHaveAudioDao wordsHaveAudioDao = DBHelper.getInstance(getApplicationContext()).getAppDatabase().getWordsHaveAudioDao();
                List<String> wordsAlreadyHaveAudio = wordsHaveAudioDao.getWordsAlreadyHaveAudioByLanguage(languageCode);
                isValid = !wordsAlreadyHaveAudio.contains(word);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return isValid;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            callBackPress();
            return true;
        }else
            return (super.onOptionsItemSelected(menuItem));
    }


    private void loadLanguages() {
        OnLanguageSelectionListener callback = langCode -> {
            if(!languageCode.equals(langCode)) {
                languageCode = langCode;
                invalidateOptionsMenu();
            }
        };
        LanguageSelectionFragment languageSelectionFragment = new LanguageSelectionFragment(this);
        languageSelectionFragment.init(callback, ListMode.SPELL_4_WORD);
        languageSelectionFragment.show(getSupportFragmentManager(), languageSelectionFragment.getTag());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.spell4wiki_view_menu, menu);
        new Handler().post(this::callShowCaseUI);
        return true;
    }

    private void setupLanguageSelectorMenuItem(Menu menu) {
        MenuItem item = menu.findItem(R.id.menu_lang_selector);
        item.setVisible(true);
        View rootView = item.getActionView();
        TextView selectedLang = rootView.findViewById(R.id.txtSelectedLanguage);
        selectedLang.setText(this.languageCode.toUpperCase());
        rootView.setOnClickListener(v -> loadLanguages());
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        setupLanguageSelectorMenuItem(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    private void callShowCaseUI() {
        if (!isFinishing() && !isDestroyed() && ShowCasePref.INSTANCE.isNotShowed(ShowCasePref.SPELL_4_WORD_PAGE)) {
            MaterialTapTargetSequence sequence = new MaterialTapTargetSequence().setSequenceCompleteListener(() -> ShowCasePref.INSTANCE.showed(ShowCasePref.SPELL_4_WORD_PAGE));
            sequence.addPrompt(getPromptBuilder()
                    .setTarget(R.id.editWord)
                    .setPrimaryText(R.string.sc_t_spell4word_page_edit_word)
                    .setSecondaryText(R.string.sc_d_spell4word_page_edit_word))
                    .show();
        }
    }

    private MaterialTapTargetPrompt.Builder getPromptBuilder() {
        return new MaterialTapTargetPrompt.Builder(Spell4WordActivity.this)
                .setPromptFocal(new RectanglePromptFocal())
                .setAnimationInterpolator(new FastOutSlowInInterpolator())
                .setFocalPadding(R.dimen.show_case_focal_padding);
    }

    public void updateList(String word) {

    }

    @Override
    public void onBackPressed() {
        callBackPress();
    }

    private void callBackPress() {
        if (!TextUtils.isEmpty(editSpell4Word.getText())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.confirmation);
            builder.setMessage(R.string.confirm_to_back);
            builder.setCancelable(false);
            builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> super.onBackPressed());
            builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> { });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            super.onBackPressed();
        }
    }
}

