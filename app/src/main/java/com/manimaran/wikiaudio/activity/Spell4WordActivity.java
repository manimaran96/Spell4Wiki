package com.manimaran.wikiaudio.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.adapter.EndlessAdapter;
import com.manimaran.wikiaudio.constant.UrlType;
import com.manimaran.wikiaudio.fragment.BottomSheetFragment;
import com.manimaran.wikiaudio.listerner.CallBackListener;
import com.manimaran.wikiaudio.utils.GeneralUtils;
import com.manimaran.wikiaudio.utils.PrefManager;
import com.manimaran.wikiaudio.view.EndlessListView;
import com.manimaran.wikiaudio.wiki_api.ApiClient;
import com.manimaran.wikiaudio.wiki_api.ApiInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Spell4WordActivity extends AppCompatActivity{

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
    }

    private void setTitle() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.spell4word));
            getSupportActionBar().setSubtitle(ApiClient.getUrl(UrlType.WIKTIONARY_PAGE, getApplicationContext()));
        }
    }
}

