package com.manimaran.wikiaudio.activity;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.constant.Constants;
import com.manimaran.wikiaudio.fragment.BottomSheetFragment;
import com.manimaran.wikiaudio.fragment.WebViewFragment;
import com.manimaran.wikiaudio.listerner.CallBackListener;
import com.manimaran.wikiaudio.utils.PrefManager;

public class CommonWebActivity extends AppCompatActivity {

    private boolean isWitionaryWord = false;
    private WebViewFragment fragment = new WebViewFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_web_view);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        PrefManager pref = new PrefManager(getApplicationContext());

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            String title = "";
            if (bundle.containsKey(Constants.TITLE)) {
                title = bundle.getString(Constants.TITLE);
                setTitle(title);
            }

            if (bundle.containsKey(Constants.IS_WIKTIONARY_WORD))
                isWitionaryWord = bundle.getBoolean(Constants.IS_WIKTIONARY_WORD);

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
        menu.findItem(R.id.action_lang_change).setVisible(isWitionaryWord);
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
            case R.id.action_lang_change :
                loadLanguages();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadLanguages() {
        if(isWitionaryWord) {
            BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
            CallBackListener callback = langCode -> {
                if(fragment !=null)
                    fragment.loadWordWithOtherLang(langCode);
            };
            bottomSheetFragment.setCalBack(callback);
            bottomSheetFragment.setIsTempMode(true);
            bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
            bottomSheetFragment.setCancelable(false);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean result = super.onPrepareOptionsMenu(menu);
        changeMenuButtonStyle(menu.findItem(R.id.action_forward), fragment.canGoForward());
        changeMenuButtonStyle(menu.findItem(R.id.action_backward), fragment.canGoBackward());
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
