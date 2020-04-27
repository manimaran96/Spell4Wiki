package com.manimaran.wikiaudio.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.google.android.material.snackbar.Snackbar;
import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.adapters.EndlessAdapter;
import com.manimaran.wikiaudio.apis.ApiClient;
import com.manimaran.wikiaudio.apis.ApiInterface;
import com.manimaran.wikiaudio.constants.AppConstants;
import com.manimaran.wikiaudio.constants.EnumTypeDef.ListMode;
import com.manimaran.wikiaudio.fragments.LanguageSelectionFragment;
import com.manimaran.wikiaudio.listerners.OnLanguageSelectionListener;
import com.manimaran.wikiaudio.models.WikiSearchWords;
import com.manimaran.wikiaudio.utils.GeneralUtils;
import com.manimaran.wikiaudio.utils.PrefManager;
import com.manimaran.wikiaudio.views.EndlessListView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WiktionarySearchActivity extends AppCompatActivity implements EndlessListView.EndlessListener {

    private EndlessListView resultListView;
    private TextView txtNotFound;
    private SearchView searchView;
    private Snackbar snackbar;

    private String queryString;
    private Integer nextOffset;
    private PrefManager pref;
    private ApiInterface api;
    private String languageCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wiktionary_search);
        init();
    }

    private void init() {
        pref = new PrefManager(WiktionarySearchActivity.this);
        languageCode = pref.getLanguageCodeWiktionary();
        api = ApiClient.getWiktionaryApi(getApplicationContext(), languageCode).create(ApiInterface.class);

        // Views
        txtNotFound = findViewById(R.id.txtNotFound);
        searchView = findViewById(R.id.search_bar);
        resultListView = findViewById(R.id.search_result_list);
        snackbar = Snackbar.make(searchView, getString(R.string.something_went_wrong), Snackbar.LENGTH_LONG);

        searchView.requestFocus();
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint(getResources().getString(R.string.search_here));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                submitQuery(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        resultListView.setLoadingView(R.layout.loading_row);
        resultListView.setAdapter(new EndlessAdapter(this, new ArrayList<>(), ListMode.WIKTIONARY));
        resultListView.setListener(this);
        resultListView.setVisibility(View.INVISIBLE);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.wiktionary));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (getIntent() != null && getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey(AppConstants.SEARCH_TEXT)) {
                String text = getIntent().getExtras().getString(AppConstants.SEARCH_TEXT);
                searchView.setQuery(text, true);
            }
        }
    }

    private void submitQuery(String s) {
        queryString = s;
        nextOffset = 0;
        txtNotFound.setVisibility(View.GONE);
        resultListView.setVisibility(View.VISIBLE);
        resultListView.reset();

        search(queryString);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.spell4wiki_view_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        setupLanguageSelectorMenuItem(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    private void loadLanguages() {
        OnLanguageSelectionListener callback = langCode -> {
            languageCode = langCode;
            invalidateOptionsMenu();
            api = ApiClient.getWiktionaryApi(getApplicationContext(), languageCode).create(ApiInterface.class);
            if (queryString != null)
                submitQuery(queryString);
        };
        LanguageSelectionFragment languageSelectionFragment = new LanguageSelectionFragment();
        languageSelectionFragment.init(callback, ListMode.WIKTIONARY);
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

    private void search(String query) {
        Call<WikiSearchWords> call = api.fetchRecords(query, nextOffset);

        call.enqueue(new Callback<WikiSearchWords>() {
            @Override
            public void onResponse(@NotNull Call<WikiSearchWords> call, @NotNull Response<WikiSearchWords> response) {
                if (response.isSuccessful() && response.body() != null) {
                    processSearchResult(response.body());
                } else
                    searchFailed(getString(R.string.something_went_wrong));
            }

            @Override
            public void onFailure(@NotNull Call<WikiSearchWords> call, @NotNull Throwable t) {
                searchFailed(getString(R.string.something_went_wrong));
            }
        });
    }

    private void searchFailed(String msg) {
        if (resultListView != null) { // Footer loader consume count = 1
            if (resultListView.getAdapter() != null && resultListView.getAdapter().getCount() < 2) {
                resultListView.setVisibility(View.INVISIBLE);
                txtNotFound.setText(getString(R.string.result_not_found));
                txtNotFound.setVisibility(View.VISIBLE);
            } else {
                txtNotFound.setVisibility(View.GONE);
                resultListView.loadLaterOnScroll();
            }
        }
        if (GeneralUtils.isNetworkConnected(getApplicationContext())) {
            snackbar.setText(msg);
        } else
            snackbar.setText(getString(resultListView.getVisibility() != View.VISIBLE ? R.string.check_internet : R.string.record_fetch_fail));

        if (!msg.equals(getString(R.string.result_not_found)) && !snackbar.isShown())
            snackbar.show();
    }

    private void processSearchResult(WikiSearchWords wikiSearchWords) {
        ArrayList<String> titleList = new ArrayList<>();

        if (snackbar.isShown())
            snackbar.dismiss();

        boolean isEmptyResponse;
        if (wikiSearchWords != null) {
            if (wikiSearchWords.getOffset() != null && wikiSearchWords.getOffset().getNextOffset() != null) {
                nextOffset = wikiSearchWords.getOffset().getNextOffset();
            } else
                nextOffset = null;

            if (wikiSearchWords.getQuery() != null && wikiSearchWords.getQuery().getWikiTitleList() != null) {
                for (WikiSearchWords.WikiWord wikiWord : wikiSearchWords.getQuery().getWikiTitleList()) {
                    titleList.add(wikiWord.getTitle());
                }
                isEmptyResponse = titleList.isEmpty();
            } else
                isEmptyResponse = true;

            if (isEmptyResponse) {
                searchFailed(getString(R.string.result_not_found));
            } else {
                resultListView.addNewData(titleList);
            }
        } else
            searchFailed(getString(R.string.something_went_wrong));
    }

    @Override
    public boolean loadData() {
        // Triggered only when new data needs to be appended to the list
        // Return true if loading is in progress, false if there is no more data to load
        if (nextOffset != null) {
            search(queryString);
            return true;
        } else
            return false;
    }
}

