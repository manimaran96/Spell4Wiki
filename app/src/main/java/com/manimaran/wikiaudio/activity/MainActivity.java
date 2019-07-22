package com.manimaran.wikiaudio.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.fragment.BottomSheetFragment;
import com.manimaran.wikiaudio.utils.GeneralUtils;
import com.manimaran.wikiaudio.utils.PrefManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Views

    private boolean doubleBackToExitPressedOnce = false;
    private PrefManager pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = new PrefManager(getApplicationContext());

        init();

        CardView cardView2 = findViewById(R.id.card_wiki);
        CardView cardView3 = findViewById(R.id.card_word_list);
        CardView cardView4 = findViewById(R.id.card_word);

        cardView2.setOnClickListener(this);
        cardView3.setOnClickListener(this);
        cardView4.setOnClickListener(this);


        /*
         * Search View
         */
        SearchView searchView = findViewById(R.id.search_view);
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint(getString(R.string.search_here));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {


                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });



    }

    @Override
    public void onClick(View view) {
        Class nextClass = SearchActivity.class;
        switch (view.getId()) {
            case R.id.card_wiki:
                nextClass = Spell4Wiktionary.class;
                break;
            case R.id.card_word_list:
                nextClass = UploadToCommonsActivity.class;
                break;
            case R.id.card_word:
                nextClass = MainActivity.class;
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
