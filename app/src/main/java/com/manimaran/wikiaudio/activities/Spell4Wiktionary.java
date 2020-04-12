package com.manimaran.wikiaudio.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.adapters.EndlessAdapter;
import com.manimaran.wikiaudio.apis.ApiClient;
import com.manimaran.wikiaudio.apis.ApiInterface;
import com.manimaran.wikiaudio.constants.Constants;
import com.manimaran.wikiaudio.constants.EnumTypeDef.ListMode;
import com.manimaran.wikiaudio.databases.DBHelper;
import com.manimaran.wikiaudio.databases.dao.WordsHaveAudioDao;
import com.manimaran.wikiaudio.databases.entities.WikiLang;
import com.manimaran.wikiaudio.fragments.LanguageSelectionFragment;
import com.manimaran.wikiaudio.listerners.OnLanguageSelectionListener;
import com.manimaran.wikiaudio.models.WikiWordsWithoutAudio;
import com.manimaran.wikiaudio.utils.GeneralUtils;
import com.manimaran.wikiaudio.utils.PrefManager;
import com.manimaran.wikiaudio.views.EndlessListView;

import java.util.ArrayList;
import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Spell4Wiktionary extends AppCompatActivity implements EndlessListView.EndlessListener {

    // Views
    private EndlessListView resultListView;
    private EndlessAdapter adapter;
    private SwipeRefreshLayout refreshLayout = null;

    private String nextOffsetObj;
    private PrefManager pref;
    private String languageCode = "";
    private WordsHaveAudioDao wordsHaveAudioDao;
    private Snackbar snackbar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spell_4_wiktionary);
        init();
        loadDataFromServer();
    }

    /**
     * Init views
     */
    private void init() {
        resultListView = findViewById(R.id.search_result_list);
        refreshLayout = findViewById(R.id.layout_swipe);
        resultListView.setLoadingView(R.layout.loading_row);
        snackbar = Snackbar.make(resultListView, getString(R.string.record_fetch_fail), Snackbar.LENGTH_LONG);

        pref = new PrefManager(getApplicationContext());
        languageCode = pref.getLanguageCodeSpell4Wiki();
        wordsHaveAudioDao = DBHelper.getInstance(getApplicationContext()).getAppDatabase().getWordsHaveAudioDao();

        adapter = new EndlessAdapter(this, new ArrayList<>(), ListMode.SPELL_4_WIKI);
        resultListView.setAdapter(adapter);
        resultListView.setListener(this);
        resultListView.setVisibility(View.VISIBLE);

        // Title & Sub title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.spell4wiktionary));
        }

        refreshLayout.setOnRefreshListener(this::loadDataFromServer);
    }

    /**
     * Getting words from wiktionary without audio
     */
    private void loadDataFromServer() {

        if (nextOffsetObj == null)
            refreshLayout.setRefreshing(true);

        DBHelper dbHelper = DBHelper.getInstance(getApplicationContext());
        WikiLang wikiLang = dbHelper.getAppDatabase().getWikiLangDao().getWikiLanguageWithCode(languageCode);
        String titleOfWordsWithoutAudio = null;
        if (wikiLang != null && !TextUtils.isEmpty(wikiLang.getTitleOfWordsWithoutAudio()))
            titleOfWordsWithoutAudio = wikiLang.getTitleOfWordsWithoutAudio();
        // DB Clear or Sync Issue
        if (titleOfWordsWithoutAudio == null) {
            titleOfWordsWithoutAudio = Constants.DEFAULT_TITLE_FOR_WITHOUT_AUDIO;
            languageCode = Constants.DEFAULT_LANGUAGE_CODE;
            invalidateOptionsMenu();
            pref.setLanguageCodeSpell4Wiki(languageCode);
        }

        //titleOfWordsWithoutAudio= "பகுப்பு:அறுபட்ட_கோப்பு_இணைப்புகள்_உள்ள_பக்கங்கள்";
        ApiInterface api = ApiClient.getWiktionaryApi(getApplicationContext(), languageCode).create(ApiInterface.class);
        Call<WikiWordsWithoutAudio> call = api.fetchUnAudioRecords(titleOfWordsWithoutAudio, nextOffsetObj);

        call.enqueue(new Callback<WikiWordsWithoutAudio>() {
            @Override
            public void onResponse(@NonNull Call<WikiWordsWithoutAudio> call, @NonNull Response<WikiWordsWithoutAudio> response) {
                if (response.isSuccessful() && response.body() != null) {
                    processSearchResultAudio(response.body());
                }else
                    searchFailed(getString(R.string.something_went_wrong));
            }

            @Override
            public void onFailure(@NonNull Call<WikiWordsWithoutAudio> call, @NonNull Throwable t) {
                searchFailed(getString(R.string.something_went_wrong));
            }
        });
    }

    private void processSearchResultAudio(WikiWordsWithoutAudio wikiWordsWithoutAudio) {

        ArrayList<String> titleList = new ArrayList<>();
        if (nextOffsetObj == null)
            resultListView.reset();
        if(resultListView.getVisibility() != View.VISIBLE)
            resultListView.setVisibility(View.VISIBLE);
        if (snackbar.isShown())
            snackbar.dismiss();
        boolean isEmptyResponse;
        if (wikiWordsWithoutAudio != null) {
            if (wikiWordsWithoutAudio.getOffset() != null && wikiWordsWithoutAudio.getOffset().getNextOffset() != null) {
                nextOffsetObj = wikiWordsWithoutAudio.getOffset().getNextOffset();
            } else
                nextOffsetObj = null;

            if (refreshLayout.isRefreshing())
                refreshLayout.setRefreshing(false);

            if (wikiWordsWithoutAudio.getQuery() != null && wikiWordsWithoutAudio.getQuery().getWikiTitleList() != null) {
                for (WikiWordsWithoutAudio.WikiTitle wikiTitle : wikiWordsWithoutAudio.getQuery().getWikiTitleList()) {
                    titleList.add(wikiTitle.getTitle());
                }
                isEmptyResponse = titleList.isEmpty();
            } else
                isEmptyResponse = true;

            if (!isEmptyResponse) {
                titleList.removeAll(wordsHaveAudioDao.getWordsAlreadyHaveAudioByLanguage(languageCode));
                if (titleList.size() == 0) {
                    // TODO : May issue comes
                    loadDataFromServer(); // Get more words if no words without audio
                }
                resultListView.addNewData(titleList);
            } else {
                searchFailed(getString(R.string.something_went_wrong));
            }
        } else
            searchFailed(getString(R.string.something_went_wrong));
    }

    private void searchFailed(String msg) {
        if(resultListView != null){ // Footer loader consume count = 1
            if(resultListView.getAdapter() != null && resultListView.getAdapter().getCount() < 2)
                resultListView.setVisibility(View.INVISIBLE);
            else
                resultListView.loadLaterOnScroll();
        }
        if (GeneralUtils.isNetworkConnected(getApplicationContext())) {
            snackbar.setText(msg);
        } else
            snackbar.setText(getString(resultListView.getVisibility() != View.VISIBLE ?  R.string.check_internet : R.string.record_fetch_fail));
        if (refreshLayout.isRefreshing())
            refreshLayout.setRefreshing(false);
        if (!snackbar.isShown())
            snackbar.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.spell4wiki_view_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return (super.onOptionsItemSelected(item));
    }

    private void loadLanguages() {
        OnLanguageSelectionListener callback = langCode -> {
            languageCode = langCode;
            invalidateOptionsMenu();
            loadDataFromServer();
        };
        LanguageSelectionFragment languageSelectionFragment = new LanguageSelectionFragment();
        languageSelectionFragment.init(callback, ListMode.SPELL_4_WIKI);
        languageSelectionFragment.show(getSupportFragmentManager(), languageSelectionFragment.getTag());
    }

    private void setupLanguageSelectorMenuItem(Menu menu) {
        MenuItem item = menu.findItem(R.id.menu_lang_selector);
        item.setVisible(true);
        View rootView = item.getActionView();
        TextView selectedLang = rootView.findViewById(R.id.txtSelectedLanguage);
        selectedLang.setText(this.languageCode.toUpperCase());
        rootView.setOnClickListener(v -> {
            loadLanguages();
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        setupLanguageSelectorMenuItem(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean loadData() {
        // Triggered only when new data needs to be appended to the list
        // Return true if loading is in progress, false if there is no more data to load
        if (nextOffsetObj != null) {
            loadDataFromServer();
            return true;
        } else
            return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Constants.RC_UPLOAD_DIALOG){
            if(data != null && data.hasExtra(Constants.WORD)){
                if(adapter != null) {
                    adapter.remove(data.getStringExtra(Constants.WORD));
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }
}
