package com.manimarank.spell4wiki.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.manimarank.spell4wiki.R;
import com.manimarank.spell4wiki.adapters.EndlessRecyclerAdapter;
import com.manimarank.spell4wiki.apis.ApiClient;
import com.manimarank.spell4wiki.apis.ApiInterface;
import com.manimarank.spell4wiki.databases.DBHelper;
import com.manimarank.spell4wiki.databases.dao.WikiLangDao;
import com.manimarank.spell4wiki.databases.dao.WordsHaveAudioDao;
import com.manimarank.spell4wiki.databases.entities.WikiLang;
import com.manimarank.spell4wiki.databases.entities.WordsHaveAudio;
import com.manimarank.spell4wiki.fragments.LanguageSelectionFragment;
import com.manimarank.spell4wiki.listerners.OnLanguageSelectionListener;
import com.manimarank.spell4wiki.models.WikiWordsWithoutAudio;
import com.manimarank.spell4wiki.utils.NetworkUtils;
import com.manimarank.spell4wiki.utils.PrefManager;
import com.manimarank.spell4wiki.utils.ShowCasePref;
import com.manimarank.spell4wiki.utils.constants.AppConstants;
import com.manimarank.spell4wiki.utils.constants.EnumTypeDef.ListMode;
import com.manimarank.spell4wiki.views.EndlessRecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetSequence;
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal;

public class Spell4Wiktionary extends AppCompatActivity implements EndlessRecyclerView.EndlessListener {

    WordsHaveAudioDao wordsHaveAudioDao;
    List<String> wordsListAlreadyHaveAudio = new ArrayList<>();
    // Views
    private EndlessRecyclerView recyclerView;
    private EndlessRecyclerAdapter adapter;
    private SwipeRefreshLayout refreshLayout = null;
    private String nextOffsetObj;
    private PrefManager pref;
    private String languageCode = "";
    private Snackbar snackbar = null;
    private View layoutEmpty;

    // For track multiple api calls based on continues time, success but no valid data and fail retry
    private Long apiResultTime = 0L;
    private int apiRetryCount = 0;
    private int apiFailRetryCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spell_4_wiktionary);

        pref = new PrefManager(getApplicationContext());
        languageCode = pref.getLanguageCodeSpell4Wiki();

        init();
        loadDataFromServer();
    }

    /**
     * Init views
     */
    private void init() {
        // Title & Sub title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.spell4wiktionary));
        }

        recyclerView = findViewById(R.id.recyclerView);
        refreshLayout = findViewById(R.id.layout_swipe);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        snackbar = Snackbar.make(recyclerView, getString(R.string.record_fetch_fail), Snackbar.LENGTH_LONG);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new EndlessRecyclerAdapter(Spell4Wiktionary.this, new ArrayList<>(), ListMode.SPELL_4_WIKI);
        recyclerView.setAdapter(adapter, layoutManager);
        recyclerView.setListener(this);
        recyclerView.setVisibility(View.VISIBLE);

        refreshLayout.setOnRefreshListener(this::loadDataFromServer);
    }

    private void resetApiResultTime() {
        apiResultTime = System.currentTimeMillis();
        apiRetryCount = 0;
    }

    /**
     * Getting words from wiktionary without audio
     */
    private void loadDataFromServer() {
        if (!isFinishing() && !isDestroyed()) {
            if (NetworkUtils.INSTANCE.isConnected(getApplicationContext())) {
                /**
                 * Check if user reach the end of api data.
                 * Show empty UI If no data exist. Otherwise show message.
                 */
                if (recyclerView != null && recyclerView.isLastPage()) {
                    searchFailed(getString(R.string.no_more_data_found));
                    return;
                }

                /**
                 * Set basic information on both very first time and after language change
                 */
                String wiktionaryTitleOfWordsWithoutAudio = null;
                if (nextOffsetObj == null) {
                    if (!refreshLayout.isRefreshing())
                        refreshLayout.setRefreshing(true);
                    resetApiResultTime();
                    apiFailRetryCount = 0;
                    if (recyclerView != null)
                        recyclerView.reset();
                    DBHelper dbHelper = DBHelper.getInstance(getApplicationContext());
                    WikiLang wikiLang = dbHelper.getAppDatabase().getWikiLangDao().getWikiLanguageWithCode(languageCode);
                    if (wikiLang != null && !TextUtils.isEmpty(wikiLang.getTitleOfWordsWithoutAudio()))
                        wiktionaryTitleOfWordsWithoutAudio = wikiLang.getTitleOfWordsWithoutAudio();

                    wordsHaveAudioDao = dbHelper.getAppDatabase().getWordsHaveAudioDao();
                    wordsListAlreadyHaveAudio = wordsHaveAudioDao.getWordsAlreadyHaveAudioByLanguage(languageCode);
                }


                /**
                 * For Avoiding multiple api calls
                 * Time limit for loop api call max 30 to 40 secs.
                 * Checked cases
                 *      1. Words count below the view port(approximate 15) & apiRetryCount reach 3 and more -> Lot of Words already have audios
                 *      2. apiRetryCount only 3 and above -> After getting some data may fail, Some user recorded words between records limits
                 *      3. apiFailRetryCount only 3 and above -> Continous api fail
                */
                long duration = System.currentTimeMillis() - apiResultTime;
                if (TimeUnit.MILLISECONDS.toSeconds(duration) > AppConstants.API_LOOP_MAX_SECS || apiFailRetryCount >= AppConstants.API_MAX_FAIL_RETRY) { // TODO update time
                    if ((adapter.getItemCount() < AppConstants.API_LOOP_MINIMUM_COUNT_IN_LIST && apiRetryCount >= AppConstants.API_MAX_RETRY) || apiRetryCount >= AppConstants.API_MAX_RETRY || apiFailRetryCount >= AppConstants.API_MAX_FAIL_RETRY) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle(R.string.do_you_want_continue);
                        boolean isFail = apiFailRetryCount >= AppConstants.API_MAX_FAIL_RETRY;
                        builder.setMessage(isFail ? R.string.spell4wiktionary_load_more_failed_confirmation : R.string.spell4wiktionary_load_more_confirmation);
                        builder.setCancelable(false);
                        builder.setPositiveButton(R.string.retry_now, (dialog, which) -> {
                            recyclerView.enableLoadMore();
                            resetApiResultTime();
                            apiFailRetryCount = 0;
                            loadDataFromServer();
                        });
                        builder.setNegativeButton(R.string.not_now, ((dialog, which) -> {
                            recyclerView.disableLoadMore();
                            dialog.dismiss();
                        }));
                        AlertDialog dialog = builder.create();

                        recyclerView.disableLoadMore();
                        if (refreshLayout.isRefreshing())
                            refreshLayout.setRefreshing(false);
                        dialog.show();
                        return;
                    }
                }


                // DB Clear or Sync Issue
                if (wiktionaryTitleOfWordsWithoutAudio == null) {
                    wiktionaryTitleOfWordsWithoutAudio = AppConstants.DEFAULT_TITLE_FOR_WITHOUT_AUDIO;
                    languageCode = AppConstants.DEFAULT_LANGUAGE_CODE;
                    invalidateOptionsMenu();
                    pref.setLanguageCodeSpell4Wiki(languageCode);
                }

                //wiktionaryTitleOfWordsWithoutAudio = "பகுப்பு:அரிசமய. உள்ள பக்கங்கள்"; // https://ta.wiktionary.org/wiki/பகுப்பு:சென்னைப்_பேரகரமுதலியின்_சொற்சுருக்கப்_பகுப்புகள்-தமிழ்

                ApiInterface api = ApiClient.getWiktionaryApi(getApplicationContext(), languageCode).create(ApiInterface.class);
                Call<WikiWordsWithoutAudio> call = api.fetchUnAudioRecords(wiktionaryTitleOfWordsWithoutAudio, nextOffsetObj);

                call.enqueue(new Callback<WikiWordsWithoutAudio>() {
                    @Override
                    public void onResponse(@NonNull Call<WikiWordsWithoutAudio> call, @NonNull Response<WikiWordsWithoutAudio> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            processSearchResultAudio(response.body());
                        } else
                            searchFailed(getString(R.string.something_went_wrong));
                    }

                    @Override
                    public void onFailure(@NonNull Call<WikiWordsWithoutAudio> call, @NonNull Throwable t) {
                        searchFailed(getString(R.string.something_went_wrong));
                    }
                });
            } else {
                searchFailed(getString(R.string.check_internet));
            }
        }
    }

    private void processSearchResultAudio(WikiWordsWithoutAudio wikiWordsWithoutAudio) {

        if (!isDestroyed() && !isFinishing()) {

            ArrayList<String> titleList = new ArrayList<>();

            if (recyclerView.getVisibility() != View.VISIBLE)
                recyclerView.setVisibility(View.VISIBLE);

            if (layoutEmpty.getVisibility() == View.VISIBLE)
                layoutEmpty.setVisibility(View.GONE);

            if (snackbar.isShown())
                snackbar.dismiss();
            boolean isEmptyResponse;
            if (wikiWordsWithoutAudio != null) {
                if (wikiWordsWithoutAudio.getOffset() != null && wikiWordsWithoutAudio.getOffset().getNextOffset() != null) {
                    nextOffsetObj = wikiWordsWithoutAudio.getOffset().getNextOffset();
                } else {
                    recyclerView.setLastPage();
                    nextOffsetObj = null;
                }

                if (wikiWordsWithoutAudio.getQuery() != null && wikiWordsWithoutAudio.getQuery().getWikiTitleList() != null) {
                    for (WikiWordsWithoutAudio.WikiTitle wikiTitle : wikiWordsWithoutAudio.getQuery().getWikiTitleList()) {
                        titleList.add(wikiTitle.getTitle());
                    }
                    isEmptyResponse = titleList.isEmpty();
                } else
                    isEmptyResponse = true;


                if (!isEmptyResponse) {
                    apiFailRetryCount = 0;
                    // Remove already recorded words
                    titleList.removeAll(wordsListAlreadyHaveAudio);
                    if (titleList.size() > 0) {
                        if (refreshLayout.isRefreshing())
                            refreshLayout.setRefreshing(false);
                        resetApiResultTime();
                        adapter.setWordsHaveAudioList(wordsListAlreadyHaveAudio);
                        recyclerView.addNewData(titleList);
                        if (ShowCasePref.INSTANCE.isNotShowed(ShowCasePref.LIST_ITEM_SPELL_4_WIKI) || ShowCasePref.INSTANCE.isNotShowed(ShowCasePref.SPELL_4_WIKI_PAGE))
                            new Handler().post(this::callShowCaseUI);
                    } else {
                        if (!refreshLayout.isRefreshing())
                            refreshLayout.setRefreshing(true);
                        apiRetryCount += 1;
                        loadDataFromServer();
                    }
                } else {
                    searchFailed(getString(R.string.something_went_wrong));
                }
            } else
                searchFailed(getString(R.string.something_went_wrong));
        }
    }

    private void searchFailed(String msg) {
        if (!isDestroyed() && !isFinishing()) {
            resetApiResultTime();

            if (NetworkUtils.INSTANCE.isConnected(getApplicationContext())) {
                apiFailRetryCount += 1;
                snackbar.setText(msg);
                if (recyclerView != null && adapter != null && adapter.getItemCount() < 1) {
                    recyclerView.setVisibility(View.INVISIBLE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                }
            } else {
                snackbar.setText(getString(recyclerView.getVisibility() != View.VISIBLE ? R.string.check_internet : R.string.record_fetch_fail));
            }
            if (recyclerView != null)
                recyclerView.removeLoader();
            if (refreshLayout.isRefreshing())
                refreshLayout.setRefreshing(false);
            if (!snackbar.isShown())
                snackbar.show();
        }
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
            callBackPress();
        }
        return (super.onOptionsItemSelected(item));
    }

    private void loadLanguages() {
        OnLanguageSelectionListener callback = langCode -> {
            if (!languageCode.equals(langCode)) {
                languageCode = langCode;
                invalidateOptionsMenu();
                nextOffsetObj = null;
                loadDataFromServer();
            }
        };
        LanguageSelectionFragment languageSelectionFragment = new LanguageSelectionFragment(this);
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
            if (ShowCasePref.INSTANCE.isNotShowed(ShowCasePref.SPELL_4_WIKI_PAGE))
                return;
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
    public void loadFail() {
        if (!NetworkUtils.INSTANCE.isConnected(getApplicationContext()))
            searchFailed(getString(R.string.check_internet));
        else if (recyclerView != null && recyclerView.isLastPage())
            searchFailed(getString(R.string.no_more_data_found));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void updateList(String word) {
        if (adapter != null) {
            wordsHaveAudioDao.insert(new WordsHaveAudio(word, languageCode));
            adapter.addWordInWordsHaveAudioList(word);
            adapter.remove(word);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstants.RC_UPLOAD_DIALOG) {
            if (data != null && data.hasExtra(AppConstants.WORD)) {
                if (adapter != null) {
                    adapter.addWordInWordsHaveAudioList(data.getStringExtra(AppConstants.WORD));
                    adapter.remove(data.getStringExtra(AppConstants.WORD));
                }
            }
        }
    }

    private void callShowCaseUI() {
        if (!isFinishing() && !isDestroyed()) {
            if (ShowCasePref.INSTANCE.isNotShowed(ShowCasePref.LIST_ITEM_SPELL_4_WIKI) || ShowCasePref.INSTANCE.isNotShowed(ShowCasePref.SPELL_4_WIKI_PAGE)) {
                MaterialTapTargetSequence sequence = new MaterialTapTargetSequence().setSequenceCompleteListener(() -> {
                    ShowCasePref.INSTANCE.showed(ShowCasePref.LIST_ITEM_SPELL_4_WIKI);
                    ShowCasePref.INSTANCE.showed(ShowCasePref.SPELL_4_WIKI_PAGE);
                });

                if (ShowCasePref.INSTANCE.isNotShowed(ShowCasePref.SPELL_4_WIKI_PAGE)) {
                    WikiLangDao wikiLangDao = DBHelper.getInstance(getApplicationContext()).getAppDatabase().getWikiLangDao();
                    sequence.addPrompt(
                            getPromptBuilder()
                                    .setTarget(R.id.layoutSelectLanguage)
                                    .setPrimaryText(R.string.sc_t_spell4wiki_page_language)
                                    .setSecondaryText(String.format(getString(R.string.sc_d_spell4wiki_page_language), wikiLangDao.getWikiLanguageWithCode(languageCode).getName())));
                }
                if (ShowCasePref.INSTANCE.isNotShowed(ShowCasePref.LIST_ITEM_SPELL_4_WIKI) && recyclerView != null && recyclerView.getChildAt(0) != null) {
                    sequence.addPrompt(getPromptBuilder()
                            .setTarget(recyclerView.getChildAt(0))
                            .setPrimaryText(R.string.sc_t_spell4wiki_list_item)
                            .setSecondaryText(R.string.sc_d_spell4wiki_list_item));

                }
                sequence.show();
            }
        }
    }

    private MaterialTapTargetPrompt.Builder getPromptBuilder() {
        return new MaterialTapTargetPrompt.Builder(Spell4Wiktionary.this)
                .setPromptFocal(new RectanglePromptFocal())
                .setAnimationInterpolator(new FastOutSlowInInterpolator())
                .setFocalPadding(R.dimen.show_case_focal_padding);
    }

    @Override
    public void onBackPressed() {
        callBackPress();
    }

    private void callBackPress() {
        if (adapter != null && adapter.getItemCount() > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.confirmation);
            builder.setMessage(R.string.confirm_to_back);
            builder.setCancelable(false);
            builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> super.onBackPressed());
            builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
        } else
            super.onBackPressed();
    }
}
