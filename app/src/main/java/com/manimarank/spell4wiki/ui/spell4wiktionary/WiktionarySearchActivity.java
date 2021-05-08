package com.manimarank.spell4wiki.ui.spell4wiktionary;

import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.manimarank.spell4wiki.R;
import com.manimarank.spell4wiki.ui.common.BaseActivity;
import com.manimarank.spell4wiki.data.apis.ApiClient;
import com.manimarank.spell4wiki.data.apis.ApiInterface;
import com.manimarank.spell4wiki.data.model.WikiWord;
import com.manimarank.spell4wiki.data.db.DBHelper;
import com.manimarank.spell4wiki.data.db.dao.WikiLangDao;
import com.manimarank.spell4wiki.ui.languageselector.LanguageSelectionFragment;
import com.manimarank.spell4wiki.ui.listerners.OnLanguageSelectionListener;
import com.manimarank.spell4wiki.data.model.WikiSearchWords;
import com.manimarank.spell4wiki.utils.NetworkUtils;
import com.manimarank.spell4wiki.data.prefs.PrefManager;
import com.manimarank.spell4wiki.data.prefs.ShowCasePref;
import com.manimarank.spell4wiki.utils.constants.AppConstants;
import com.manimarank.spell4wiki.utils.constants.ListMode;
import com.manimarank.spell4wiki.ui.custom.EndlessRecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetSequence;
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal;

public class WiktionarySearchActivity extends BaseActivity implements EndlessRecyclerView.EndlessListener {

    private EndlessRecyclerView recyclerView;
    private EndlessRecyclerAdapter adapter;
    private TextView txtNotFound;
    private View layoutProgress;
    private Snackbar snackbar;

    private String queryString;
    private Integer nextOffset;
    private ApiInterface api;
    private String languageCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wiktionary_search);
        init();
    }

    private void init() {
        PrefManager pref = new PrefManager(WiktionarySearchActivity.this);
        languageCode = pref.getLanguageCodeWiktionary();
        api = ApiClient.getWiktionaryApi(getApplicationContext(), languageCode).create(ApiInterface.class);

        // Views
        txtNotFound = findViewById(R.id.txtNotFound);
        layoutProgress = findViewById(R.id.layoutProgress);
        SearchView searchView = findViewById(R.id.search_bar);
        recyclerView = findViewById(R.id.recyclerView);
        snackbar = Snackbar.make(searchView, getString(R.string.something_went_wrong), Snackbar.LENGTH_LONG);

        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint(getResources().getString(R.string.search));
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

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new EndlessRecyclerAdapter(this, new ArrayList<>(), ListMode.WIKTIONARY);
        recyclerView.setAdapter(adapter, layoutManager);
        recyclerView.setListener(this);
        recyclerView.setVisibility(View.INVISIBLE);

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
        if (!isDestroyed() && !isFinishing()) {
            queryString = s;
            nextOffset = 0;
            txtNotFound.setVisibility(View.GONE);
            layoutProgress.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.reset();

            search(queryString);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.spell4wiki_view_menu, menu);
        new Handler().post(this::callShowCaseUI);
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
            if (!languageCode.equals(langCode)) {
                languageCode = langCode;
                invalidateOptionsMenu();
                api = ApiClient.getWiktionaryApi(getApplicationContext(), languageCode).create(ApiInterface.class);
                if (queryString != null)
                    submitQuery(queryString);
            }
        };
        LanguageSelectionFragment languageSelectionFragment = new LanguageSelectionFragment(this);
        languageSelectionFragment.init(callback, ListMode.WIKTIONARY);
        languageSelectionFragment.show(getSupportFragmentManager());
    }

    private void setupLanguageSelectorMenuItem(Menu menu) {
        MenuItem item = menu.findItem(R.id.menu_lang_selector);
        item.setVisible(true);
        View rootView = item.getActionView();
        TextView selectedLang = rootView.findViewById(R.id.txtSelectedLanguage);
        selectedLang.setText(this.languageCode.toUpperCase());
        rootView.setOnClickListener(v -> {
            if (ShowCasePref.INSTANCE.isNotShowed(ShowCasePref.WIKTIONARY_PAGE))
                return;
            loadLanguages();
        });

    }

    private void search(String query) {
        if (!isDestroyed() && !isFinishing()) {
            if (NetworkUtils.INSTANCE.isConnected(getApplicationContext())) {
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
            } else
                searchFailed(getString(R.string.check_internet));
        }
    }

    private void searchFailed(String msg) {
        if (!isDestroyed() && !isFinishing()) {
            if (NetworkUtils.INSTANCE.isConnected(getApplicationContext())) {
                snackbar.setText(msg);
                if(layoutProgress.getVisibility() == View.VISIBLE)
                    layoutProgress.setVisibility(View.GONE);
                if (recyclerView != null && adapter != null && adapter.getItemCount() < 1) {
                    recyclerView.setVisibility(View.INVISIBLE);
                    txtNotFound.setText(getString(R.string.result_not_found));
                    txtNotFound.setVisibility(View.VISIBLE);
                } else
                    txtNotFound.setVisibility(View.GONE);
            } else
                snackbar.setText(getString(R.string.check_internet));

            if (!msg.equals(getString(R.string.result_not_found)) && !snackbar.isShown())
                snackbar.show();
        }
    }

    private void processSearchResult(WikiSearchWords wikiSearchWords) {
        if (!isDestroyed() && !isFinishing()) {

            ArrayList<String> titleList = new ArrayList<>();

            if(layoutProgress.getVisibility() == View.VISIBLE)
                layoutProgress.setVisibility(View.GONE);

            if (snackbar.isShown())
                snackbar.dismiss();

            boolean isEmptyResponse;
            if (wikiSearchWords != null) {
                if (wikiSearchWords.getOffset() != null && wikiSearchWords.getOffset().getNextOffset() != null) {
                    nextOffset = wikiSearchWords.getOffset().getNextOffset();
                } else {
                    recyclerView.setLastPage();
                    nextOffset = null;
                }

                if (wikiSearchWords.getQuery() != null && wikiSearchWords.getQuery().getWikiTitleList() != null) {
                    for (WikiWord wikiWord : wikiSearchWords.getQuery().getWikiTitleList()) {
                        titleList.add(wikiWord.getTitle());
                    }
                    isEmptyResponse = titleList.isEmpty();
                } else
                    isEmptyResponse = true;

                if (isEmptyResponse) {
                    searchFailed(getString(R.string.result_not_found));
                } else {
                    recyclerView.addNewData(titleList);
                }
            } else
                searchFailed(getString(R.string.something_went_wrong));
        }
    }

    @Override
    public boolean loadData() {
        /*
         * Triggered only when new data needs to be appended to the list
         * Return true if loading is in progress, false if there is no more data to load
         */
        if (nextOffset != null) {
            search(queryString);
            return true;
        } else
            return false;
    }

    @Override
    public void loadFail() {
        if (!NetworkUtils.INSTANCE.isConnected(getApplicationContext()))
            searchFailed(getString(R.string.check_internet));
        else if (recyclerView != null && recyclerView.isLastPage() && adapter != null && adapter.getItemCount() > 10)
            searchFailed(getString(R.string.no_more_data_found));
    }

    private void callShowCaseUI() {
        if (!isFinishing() && !isDestroyed() && ShowCasePref.INSTANCE.isNotShowed(ShowCasePref.WIKTIONARY_PAGE)) {
            WikiLangDao wikiLangDao = DBHelper.getInstance(getApplicationContext()).getAppDatabase().getWikiLangDao();
            MaterialTapTargetSequence sequence = new MaterialTapTargetSequence().setSequenceCompleteListener(() -> ShowCasePref.INSTANCE.showed(ShowCasePref.WIKTIONARY_PAGE));
            sequence.addPrompt(new MaterialTapTargetPrompt.Builder(WiktionarySearchActivity.this)
                    .setPromptFocal(new RectanglePromptFocal())
                    .setAnimationInterpolator(new FastOutSlowInInterpolator())
                    .setFocalPadding(R.dimen.show_case_focal_padding)
                    .setTarget(R.id.layoutSelectLanguage)
                    .setPrimaryText(R.string.sc_t_wiktionary_page_language)
                    .setSecondaryText(String.format(getString(R.string.sc_d_wiktionary_page_language), wikiLangDao.getWikiLanguageWithCode(languageCode).getName())));
            sequence.show();
        }
    }
}

