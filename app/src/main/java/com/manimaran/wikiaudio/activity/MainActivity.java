package com.manimaran.wikiaudio.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

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


        CardView cardView1 = findViewById(R.id.card_explore);
        CardView cardView2 = findViewById(R.id.card_wiki);
        CardView cardView3 = findViewById(R.id.card_word_list);
        CardView cardView4 = findViewById(R.id.card_word);

        cardView1.setOnClickListener(this);
        cardView2.setOnClickListener(this);
        cardView3.setOnClickListener(this);
        cardView4.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        Class nextClass = SearchActivity.class;
        switch (view.getId()) {
            case R.id.card_explore:
                nextClass = SearchActivity.class;
                break;
            case R.id.card_wiki:
                nextClass = Spell4Wiktionary.class;
                break;
            case R.id.card_word_list:
                nextClass = UploadToCommonsActivity.class;
                break;
            case R.id.card_word:
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
