package com.manimaran.wikiaudio.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.TextView;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.fragment.BottomSheetFragment;
import com.manimaran.wikiaudio.utils.GeneralUtils;
import com.manimaran.wikiaudio.utils.PrefManager;

import java.text.Format;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Views

    private boolean doubleBackToExitPressedOnce = false;
    private PrefManager pref;
    SearchView searchView;

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
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                startActivity(intent);
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                intent.putExtra("search_text", query);
                startActivity(intent);
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
        findViewById(R.id.btn_logout).setOnClickListener(v -> GeneralUtils.logoutAlert(MainActivity.this));

        new Handler().post(() -> hideKeyboard(MainActivity.this));

    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onClick(View view) {
        Class nextClass = SearchActivity.class;
        switch (view.getId()) {
            case R.id.search_view:
                nextClass = SearchActivity.class;
                break;
            case R.id.card_spell4wiki:
                nextClass = Spell4Wiktionary.class;
                break;
            case R.id.card_spell4wordlist:
                nextClass = UploadToCommonsActivity.class;
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
        if(searchView != null)
        {
            searchView.setQuery("",false);
            searchView.clearFocus();
            hideKeyboard(this);
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce)
            super.onBackPressed();
        else {
            this.doubleBackToExitPressedOnce = true;
            GeneralUtils.showToast(getApplicationContext(), getString(R.string.alert_to_exit));
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}
