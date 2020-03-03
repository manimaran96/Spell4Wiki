package com.manimaran.wikiaudio.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.constants.UrlType;
import com.manimaran.wikiaudio.utils.GeneralUtils;
import com.manimaran.wikiaudio.utils.PrefManager;

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
        searchView.setQueryHint(getString(R.string.hint_search));
        searchView.setIconifiedByDefault(false);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(getApplicationContext(), WiktionarySearchActivity.class);
                intent.putExtra("search_text", query);
                startActivity(intent);
                new Handler().postDelayed(() -> {
                    if(searchView  != null)
                        searchView.setQuery("",false);
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
        if(pref.getIsAnonymous()){
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
        startActivity(new Intent(getApplicationContext(), nextClass));
    }

    /**
     * Init views
     */
    private void initViews() {

        CardView cardView1 = findViewById(R.id.card_spell4wiki);
        CardView cardView2 = findViewById(R.id.card_spell4wordlist);
        CardView cardView3 = findViewById(R.id.card_spell4word);

        if(pref.getIsAnonymous()){
            cardView1.setBackgroundColor(Color.GRAY);
        }
        cardView1.setOnClickListener(this);
        cardView2.setOnClickListener(this);
        cardView3.setOnClickListener(this);

        TextView txtWelcomeUser = findViewById(R.id.txt_welcome_user);
        txtWelcomeUser.setText(String.format(getString(R.string.welcome_user), pref.getName()));

        findViewById(R.id.btn_about).setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), AboutActivity.class)));
        findViewById(R.id.btn_settings).setOnClickListener(v -> GeneralUtils.showSnack(searchView, "Settings clicked"));
        findViewById(R.id.btn_logout).setOnClickListener(v -> GeneralUtils.logoutAlert(MainActivity.this));

        String urlViewMyContribution = String.format(getString(R.string.link_view_my_contribution), pref.getName());
        TextView btnMyContributions = findViewById(R.id.txtViewMyContribution);
        TextView btnLogin = findViewById(R.id.txtLogin);

        btnMyContributions.setOnClickListener(v -> GeneralUtils.openUrl(MainActivity.this, urlViewMyContribution, UrlType.INTERNAL, getString(R.string.view_my_contribution)));
        btnLogin.setOnClickListener(v-> pref.logoutUser());

        View viewContribute = findViewById(R.id.layoutContributeOptions);
        View viewLogin = findViewById(R.id.layoutLogin);
        if(pref.getIsAnonymous()){
            viewContribute.setVisibility(View.GONE);
            btnMyContributions.setVisibility(View.GONE);
            txtWelcomeUser.setVisibility(View.GONE);
            findViewById(R.id.btn_logout).setVisibility(View.GONE);
            viewLogin.setVisibility(View.VISIBLE);
        }
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
