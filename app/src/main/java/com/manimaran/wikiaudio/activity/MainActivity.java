package com.manimaran.wikiaudio.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.constant.UrlType;
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

        pref = new PrefManager(getApplicationContext());

        init();

        searchView = findViewById(R.id.search_view);
        searchView.setQueryHint(getString(R.string.hint_search));
        searchView.setIconifiedByDefault(false);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
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


        CardView cardView1 = findViewById(R.id.card_spell4wiki);
        CardView cardView2 = findViewById(R.id.card_spell4wordlist);
        CardView cardView3 = findViewById(R.id.card_spell4word);

        cardView1.setOnClickListener(this);
        cardView2.setOnClickListener(this);
        cardView3.setOnClickListener(this);

        TextView txtWelcomeUser = findViewById(R.id.txt_welcome_user);
        txtWelcomeUser.setText(String.format(getString(R.string.welcome_user), pref.getName()));

        findViewById(R.id.btn_about).setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), AboutActivity.class)));
        findViewById(R.id.btn_settings).setOnClickListener(v -> GeneralUtils.showSnack(searchView, "Settings clicked"));
        findViewById(R.id.btn_logout).setOnClickListener(v -> GeneralUtils.logoutAlert(MainActivity.this));

        String urlViewMyContribution = String.format(getString(R.string.link_view_my_contribution), pref.getName());
        findViewById(R.id.txtViewMyContribution).setOnClickListener(v -> GeneralUtils.openUrl(MainActivity.this, urlViewMyContribution, UrlType.INTERNAL, getString(R.string.view_my_contribution)));

        new Handler().post(() -> GeneralUtils.hideKeyboard(MainActivity.this));

    }

    @Override
    public void onClick(View view) {
        Class nextClass = SearchActivity.class;
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
    private void init() {

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
