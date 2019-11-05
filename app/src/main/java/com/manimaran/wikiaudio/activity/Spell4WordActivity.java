package com.manimaran.wikiaudio.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.constant.UrlType;
import com.manimaran.wikiaudio.fragment.MyCustomDialogFragment;
import com.manimaran.wikiaudio.utils.PrefManager;
import com.manimaran.wikiaudio.wiki_api.ApiClient;
import com.manimaran.wikiaudio.wiki_api.ApiInterface;


public class Spell4WordActivity extends AppCompatActivity {

    private EditText editSpell4Word;
    private Button btnRecord;

    private PrefManager pref;
    private ApiInterface api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spell4_word);

        pref = new PrefManager(getApplicationContext());
        api = ApiClient.getWiktionaryApi(getApplicationContext()).create(ApiInterface.class);
        setTitle();

        findViewById(R.id.btn_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                DialogFragment dialogFragment = new MyCustomDialogFragment();
                dialogFragment.show(fm, "dialog");
            }
        });
    }

    private void setTitle() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.spell4word));
            getSupportActionBar().setSubtitle(ApiClient.getUrl(UrlType.WIKTIONARY_PAGE, getApplicationContext()));
        }
    }
}

