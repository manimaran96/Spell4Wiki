package com.manimaran.wikiaudio.activities;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.constants.Constants;
import com.manimaran.wikiaudio.constants.EnumTypeDef.ListMode;
import com.manimaran.wikiaudio.fragments.LanguageSelectionFragment;
import com.manimaran.wikiaudio.fragments.WebViewFragment;
import com.manimaran.wikiaudio.listerners.OnLanguageSelectionListener;
import com.manimaran.wikiaudio.utils.PrefManager;

public class CommonWebActivity extends AppCompatActivity {

    private boolean isWiktionaryWord = false;
    private WebViewFragment fragment = new WebViewFragment();
    private String languageCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_web_view);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        PrefManager pref = new PrefManager(getApplicationContext());
        languageCode = pref.getLanguageCodeWiktionary();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            String title = "";
            if (bundle.containsKey(Constants.TITLE)) {
                title = bundle.getString(Constants.TITLE);
                setTitle(title);
            }

            if (bundle.containsKey(Constants.IS_WIKTIONARY_WORD))
                isWiktionaryWord = bundle.getBoolean(Constants.IS_WIKTIONARY_WORD);

            if (bundle.containsKey(Constants.LANGUAGE_CODE))
                languageCode = bundle.getString(Constants.LANGUAGE_CODE);

            loadFragment(fragment);
        }

    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.web_view_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_favorite:
                return true;
            case R.id.action_share:
                fragment.shareLink();
                return true;
            case R.id.action_refresh:
                fragment.refreshWebPage();
                return true;
            case R.id.action_forward:
                fragment.forwardWebPage();
                return true;
            case R.id.action_backward:
                fragment.backwardWebPage();
                return true;
            case R.id.action_open_in_browser:
                fragment.openInAppBrowser();
                return true;
            case R.id.action_copy_link:
                fragment.copyLink();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupLanguageSelectorMenuItem(Menu menu) {
        menu.findItem(R.id.menu_lang_selector).setVisible(isWiktionaryWord);
        if (isWiktionaryWord) {
            MenuItem item = menu.findItem(R.id.menu_lang_selector);
            View rootView = item.getActionView();
            TextView selectedLang = rootView.findViewById(R.id.txtSelectedLanguage);
            selectedLang.setText(this.languageCode.toUpperCase());
            rootView.setOnClickListener(v -> {
                loadLanguages();
            });
        }
    }

    private void loadLanguages() {
        if (isWiktionaryWord) {
            OnLanguageSelectionListener callback = langCode -> {
                this.languageCode = langCode;
                invalidateOptionsMenu();
                if (fragment != null)
                    fragment.loadWordWithOtherLang(langCode);
            };
            LanguageSelectionFragment languageSelectionFragment = new LanguageSelectionFragment();
            languageSelectionFragment.init(callback, ListMode.TEMP, languageCode);
            languageSelectionFragment.show(getSupportFragmentManager(), languageSelectionFragment.getTag());
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean result = super.onPrepareOptionsMenu(menu);
        changeMenuButtonStyle(menu.findItem(R.id.action_forward), fragment.canGoForward());
        changeMenuButtonStyle(menu.findItem(R.id.action_backward), fragment.canGoBackward());
        setupLanguageSelectorMenuItem(menu);
        return result;
    }

    private void changeMenuButtonStyle(MenuItem menuItem, boolean isAllow) {
        if (menuItem != null) {
            SpannableString s = new SpannableString(menuItem.getTitle());
            s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getApplicationContext(), isAllow ? R.color.black : R.color.light_gray)), 0, s.length(), 0);
            menuItem.setEnabled(isAllow);
            menuItem.setTitle(s);
        }
    }


}
