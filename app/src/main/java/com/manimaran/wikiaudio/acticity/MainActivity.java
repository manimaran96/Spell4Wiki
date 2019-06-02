package com.manimaran.wikiaudio.acticity;

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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_wiktionary:
                startActivity(new Intent(getApplicationContext(), SearchActivity.class));
                finish();
                break;
            // action with ID action_logout was selected
            case R.id.action_logout:
                GeneralUtils.logoutAlert(MainActivity.this);
                break;
            // action with ID action_settings was selected
            case R.id.action_settings:
                break;
            case R.id.action_upload_commons:
                startActivity(new Intent(getApplicationContext(), UploadToCommonsActivity.class));
                break;
            case R.id.action_about:
                startActivity(new Intent(getApplicationContext(), AboutActivity.class));
                break;
            case R.id.action_lang_change:
                BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
                bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
                bottomSheetFragment.setCancelable(false);
                break;
            default:
                break;
        }

        return true;
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
