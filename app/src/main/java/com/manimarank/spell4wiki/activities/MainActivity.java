package com.manimarank.spell4wiki.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;

import com.manimarank.spell4wiki.R;
import com.manimarank.spell4wiki.utils.constants.AppConstants;
import com.manimarank.spell4wiki.utils.constants.Urls;
import com.manimarank.spell4wiki.utils.GeneralUtils;
import com.manimarank.spell4wiki.utils.PrefManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Views

    SearchView searchView;
    private boolean doubleBackToExitPressedOnce = false;
    private PrefManager pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = new PrefManager(MainActivity.this);

        initViews();

        searchView = findViewById(R.id.search_view);
        searchView.setQueryHint(getString(R.string.search));
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
        if (pref.getIsAnonymous()) {
            GeneralUtils.showSnack(view, getString(R.string.login_to_contribute));
            return;
        }
        Class nextClass = WiktionarySearchActivity.class;
        switch (view.getId()) {
            case R.id.card_spell4wiki:
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

        btnMyContributions.setOnClickListener(v -> GeneralUtils.openUrl(MainActivity.this, urlMyContribution, getString(R.string.view_my_contribution)));
        btnLogin.setOnClickListener(v -> pref.logoutUser());

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
        new AlertDialog.Builder(this)
                //.setIcon(R.drawable.ic_logout)
                .setTitle(R.string.logout_confirmation)
                .setMessage(R.string.logout_message)
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    // Logout user
                    logoutApi();
                    pref.logoutUser();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void logoutApi() {
        //Call<ResponseBody>
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (searchView != null) {
            searchView.clearFocus();
            GeneralUtils.hideKeyboard(this);
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce)
            super.onBackPressed();
        else {
            this.doubleBackToExitPressedOnce = true;
            GeneralUtils.showSnack(searchView, getString(R.string.alert_to_exit));
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}
