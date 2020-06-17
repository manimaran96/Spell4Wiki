package com.manimarank.spell4wiki.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.manimarank.spell4wiki.R;
import com.manimarank.spell4wiki.activities.base.BaseActivity;
import com.manimarank.spell4wiki.utils.AppLanguageUtils;
import com.manimarank.spell4wiki.utils.GeneralUtils;
import com.manimarank.spell4wiki.utils.NetworkUtils;
import com.manimarank.spell4wiki.utils.PrefManager;
import com.manimarank.spell4wiki.utils.ShowCasePref;
import com.manimarank.spell4wiki.utils.SnackBarUtils;
import com.manimarank.spell4wiki.utils.constants.AppConstants;
import com.manimarank.spell4wiki.utils.constants.Urls;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    // Views
    SearchView searchView;
    IntentFilter filter = new IntentFilter(AppLanguageUtils.LANGUAGE_FILTER);
    BroadcastReceiver languageChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isDestroyed() && !isFinishing() && intent != null && intent.getExtras() != null) {
                String value = intent.getExtras().getString(AppLanguageUtils.SELECTED_LANGUAGE, "");
                if (value != null) {
                    recreate();
                }
            }
        }
    };
    private boolean doubleBackToExitPressedOnce = false;
    private PrefManager pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = new PrefManager(MainActivity.this);
        setContentView(R.layout.activity_main);

        initViews();
        registerReceiver(languageChangeReceiver, filter);

        searchView.setQueryHint(getString(R.string.wiktionary_search));
        searchView.setIconifiedByDefault(false);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(getApplicationContext(), WiktionarySearchActivity.class);
                intent.putExtra(AppConstants.SEARCH_TEXT, query);
                startActivity(intent);
                new Handler().postDelayed(() -> {
                    if (searchView != null)
                        searchView.setQuery("", false);
                }, 100);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        new Handler().post(() -> GeneralUtils.hideKeyboard(MainActivity.this));

    }

    @Override
    public void onClick(View view) {
        if (!NetworkUtils.INSTANCE.isConnected(getApplicationContext())) {
            SnackBarUtils.INSTANCE.showLong(view, getString(R.string.check_internet));
            return;
        }

        if (pref.getIsAnonymous()) {
            SnackBarUtils.INSTANCE.showLong(view, getString(R.string.login_to_contribute));
            return;
        }
        Class nextClass = WiktionarySearchActivity.class;
        switch (view.getId()) {
            case R.id.card_spell4wiki:
                if (ShowCasePref.INSTANCE.isNotShowed(ShowCasePref.SPELL_4_WIKI)) {
                    callShowCase();
                    return;
                } else
                    nextClass = Spell4Wiktionary.class;
                break;
            case R.id.card_spell4wordlist:
                nextClass = Spell4WordListActivity.class;
                break;
            case R.id.card_spell4word:
                nextClass = Spell4WordActivity.class;
                break;
        }
        Intent intent = new Intent(getApplicationContext(), nextClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    /**
     * Init views
     */
    private void initViews() {

        CardView cardView1 = findViewById(R.id.card_spell4wiki);
        CardView cardView2 = findViewById(R.id.card_spell4wordlist);
        CardView cardView3 = findViewById(R.id.card_spell4word);
        searchView = findViewById(R.id.search_view);

        cardView1.setOnClickListener(this);
        cardView2.setOnClickListener(this);
        cardView3.setOnClickListener(this);

        TextView txtWelcomeUser = findViewById(R.id.txt_welcome_user);
        txtWelcomeUser.setText(String.format(getString(R.string.welcome_user), pref.getName()));

        findViewById(R.id.btn_about).setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), AboutActivity.class)));
        findViewById(R.id.btn_settings).setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), SettingsActivity.class)));
        findViewById(R.id.btn_logout).setOnClickListener(v -> logoutUser());

        String urlMyContribution = String.format(Urls.COMMONS_CONTRIBUTION, pref.getName());
        TextView btnMyContributions = findViewById(R.id.txtViewMyContribution);
        TextView btnLogin = findViewById(R.id.txtLogin);

        btnMyContributions.setOnClickListener(v -> {
            if (NetworkUtils.INSTANCE.isConnected(getApplicationContext()))
                GeneralUtils.openUrl(MainActivity.this, urlMyContribution, getString(R.string.view_my_contribution));
            else
                SnackBarUtils.INSTANCE.showNormal(btnMyContributions, getString(R.string.check_internet));
        });
        btnLogin.setOnClickListener(v -> {
            if (NetworkUtils.INSTANCE.isConnected(getApplicationContext()))
                pref.logoutUser();
            else
                SnackBarUtils.INSTANCE.showNormal(searchView, getString(R.string.check_internet));
        });

        View viewContribute = findViewById(R.id.layoutContributeOptions);
        View viewLogin = findViewById(R.id.layoutLogin);
        if (pref.getIsAnonymous()) {
            //viewContribute.setVisibility(View.GONE);
            btnMyContributions.setVisibility(View.GONE);
            txtWelcomeUser.setVisibility(View.GONE);
            findViewById(R.id.btn_logout).setVisibility(View.GONE);
            viewLogin.setVisibility(View.VISIBLE);
        }
    }

    private void logoutUser() {
        if (NetworkUtils.INSTANCE.isConnected(getApplicationContext())) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.logout_confirmation)
                    .setMessage(R.string.logout_message)
                    .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                        // Logout user
                        logoutApi();
                        pref.logoutUser();
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
        } else
            SnackBarUtils.INSTANCE.showNormal(searchView, getString(R.string.check_internet));
    }

    private void logoutApi() {
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (searchView != null) {
            searchView.clearFocus();
            GeneralUtils.hideKeyboard(this);
        }
        if (!NetworkUtils.INSTANCE.isConnected(getApplicationContext()))
            SnackBarUtils.INSTANCE.showNormal(searchView, getString(R.string.check_internet));
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce)
            super.onBackPressed();
        else {
            this.doubleBackToExitPressedOnce = true;
            SnackBarUtils.INSTANCE.showLong(searchView, getString(R.string.alert_to_exit));
        }
        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }

    private void callShowCase() {
        if (!isFinishing() && !isDestroyed() && ShowCasePref.INSTANCE.isNotShowed(ShowCasePref.SPELL_4_WIKI)) {
            new MaterialTapTargetPrompt.Builder(MainActivity.this)
                    .setPromptFocal(new RectanglePromptFocal())
                    .setAnimationInterpolator(new FastOutSlowInInterpolator())
                    .setTarget(R.id.card_spell4wiki)
                    .setPrimaryText(R.string.sc_t_spell4wiki)
                    .setSecondaryText(R.string.sc_d_spell4wiki)
                    .setFocalPadding(R.dimen.show_case_focal_padding)
                    .setPromptStateChangeListener((prompt, state) -> {
                        if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED)
                            ShowCasePref.INSTANCE.showed(ShowCasePref.SPELL_4_WIKI);
                    }).show();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (languageChangeReceiver != null)
            unregisterReceiver(languageChangeReceiver);
    }
}
