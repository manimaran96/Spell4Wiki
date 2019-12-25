package com.manimaran.wikiaudio.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.constant.UrlType;
import com.manimaran.wikiaudio.fragment.MyCustomDialogFragment;
import com.manimaran.wikiaudio.utils.GeneralUtils;
import com.manimaran.wikiaudio.utils.PrefManager;
import com.manimaran.wikiaudio.wiki_api.ApiClient;
import com.manimaran.wikiaudio.wiki_api.ApiInterface;


public class Spell4WordActivity extends AppCompatActivity {

    private EditText editSpell4Word;
    private Button btnRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spell4_word);

        setTitle();

        btnRecord = findViewById(R.id.btn_record);
        editSpell4Word = findViewById(R.id.editWord);

        btnRecord.setOnClickListener(v -> {
            if(!TextUtils.isEmpty(editSpell4Word.getText()) && editSpell4Word.getText().toString().length() < 15){
                GeneralUtils.showRecordDialog(Spell4WordActivity.this, editSpell4Word.getText().toString().trim());
            }else
                GeneralUtils.showSnack(btnRecord, "Enter valid word");
        });
    }

    private void setTitle() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.spell4word));
            getSupportActionBar().setSubtitle(ApiClient.getUrl(UrlType.WIKTIONARY_PAGE, getApplicationContext()));
        }
    }
}

