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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.manimarank.spell4wiki.R;
import com.manimarank.spell4wiki.adapters.EndlessAdapter;
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
import com.manimarank.spell4wiki.utils.GeneralUtils;
import com.manimarank.spell4wiki.utils.PrefManager;
import com.manimarank.spell4wiki.utils.ShowCasePref;
import com.manimarank.spell4wiki.utils.constants.AppConstants;
import com.manimarank.spell4wiki.utils.constants.EnumTypeDef.ListMode;
import com.manimarank.spell4wiki.views.EndlessListView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetSequence;
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal;

public class Spell4Wiktionary extends AppCompatActivity implements EndlessListView.EndlessListener {

    WordsHaveAudioDao wordsHaveAudioDao;
    List<String> wordsListAlreadyHaveAudio = new ArrayList<>();
    // Views
    private EndlessListView resultListView;
    private EndlessAdapter adapter;
    private SwipeRefreshLayout refreshLayout = null;
    private String nextOffsetObj;
    private PrefManager pref;
    private String languageCode = "";
    private Snackbar snackbar = null;
    private View layoutEmpty;
    private Long apiResultTime = 0L;

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
        resultListView = findViewById(R.id.search_result_list);
        refreshLayout = findViewById(R.id.layout_swipe);
        resultListView.setLoadingView(R.layout.loading_row);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        snackbar = Snackbar.make(resultListView, getString(R.string.record_fetch_fail), Snackbar.LENGTH_LONG);


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

    private void resetApiResultTime() {
        apiResultTime = System.currentTimeMillis();
    }

    /**
     * Getting words from wiktionary without audio
     */
    private void loadDataFromServer() {
        if (!isFinishing() && !isDestroyed()) {
            String wiktionaryTitleOfWordsWithoutAudio = null;
            if (nextOffsetObj == null) {
                if (!refreshLayout.isRefreshing())
                    refreshLayout.setRefreshing(true);
                resetApiResultTime();
                if (resultListView != null)
                    resultListView.reset();
                DBHelper dbHelper = DBHelper.getInstance(getApplicationContext());
                WikiLang wikiLang = dbHelper.getAppDatabase().getWikiLangDao().getWikiLanguageWithCode(languageCode);
                if (wikiLang != null && !TextUtils.isEmpty(wikiLang.getTitleOfWordsWithoutAudio()))
                    wiktionaryTitleOfWordsWithoutAudio = wikiLang.getTitleOfWordsWithoutAudio();

                wordsHaveAudioDao = dbHelper.getAppDatabase().getWordsHaveAudioDao();
                wordsListAlreadyHaveAudio = wordsHaveAudioDao.getWordsAlreadyHaveAudioByLanguage(languageCode);
            } else {
                long duration = System.currentTimeMillis() - apiResultTime;
                if (TimeUnit.MILLISECONDS.toSeconds(duration) > 30) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.do_you_want_continue);
                    builder.setMessage(R.string.spell4wiktionary_load_more_confirmation);
                    builder.setCancelable(false);
                    builder.setPositiveButton(R.string.yes_continue, (dialog, which) -> {
                        resetApiResultTime();
                        loadDataFromServer();
                    });
                    builder.setNegativeButton(R.string.not_now, ((dialog, which) -> {
                        resultListView.removeLoader();
                        dialog.dismiss();
                    }));
                    AlertDialog dialog = builder.create();
                    resultListView.removeLoader();
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
        }
    }

    private void processSearchResultAudio(WikiWordsWithoutAudio wikiWordsWithoutAudio) {

        if (!isDestroyed() && !isFinishing()) {

            ArrayList<String> titleList = new ArrayList<>();

            if (resultListView.getVisibility() != View.VISIBLE)
                resultListView.setVisibility(View.VISIBLE);

            if (layoutEmpty.getVisibility() == View.VISIBLE)
                layoutEmpty.setVisibility(View.GONE);

            if (snackbar.isShown())
                snackbar.dismiss();
            boolean isEmptyResponse;
            if (wikiWordsWithoutAudio != null) {
                if (wikiWordsWithoutAudio.getOffset() != null && wikiWordsWithoutAudio.getOffset().getNextOffset() != null) {
                    nextOffsetObj = wikiWordsWithoutAudio.getOffset().getNextOffset();
                } else
                    nextOffsetObj = null;

                if (wikiWordsWithoutAudio.getQuery() != null && wikiWordsWithoutAudio.getQuery().getWikiTitleList() != null) {
                    for (WikiWordsWithoutAudio.WikiTitle wikiTitle : wikiWordsWithoutAudio.getQuery().getWikiTitleList()) {
                        titleList.add(wikiTitle.getTitle());
                    }
                    isEmptyResponse = titleList.isEmpty();
                } else
                    isEmptyResponse = true;


                if (!isEmptyResponse) {
                    titleList.removeAll(wordsListAlreadyHaveAudio);
                    if (titleList.size() > 0) {
                        if (refreshLayout.isRefreshing())
                            refreshLayout.setRefreshing(false);
                        resetApiResultTime();
                        adapter.setWordsHaveAudioList(wordsListAlreadyHaveAudio);
                        resultListView.addNewData(titleList);
                        new Handler().post(this::callShowCaseUI);
                    } else {
                        if (!refreshLayout.isRefreshing())
                            refreshLayout.setRefreshing(true);
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
            if (resultListView != null) { // Footer loader consume count = 1
                if (adapter != null && adapter.getCount() < 1 && resultListView.isLoading()) {
                    resultListView.setVisibility(View.INVISIBLE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                } else
                    resultListView.loadLaterOnScroll();
            }
            if (GeneralUtils.isNetworkConnected(getApplicationContext())) {
                snackbar.setText(msg);
            } else
                snackbar.setText(getString(resultListView.getVisibility() != View.VISIBLE ? R.string.check_internet : R.string.record_fetch_fail));
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
    protected void onDestroy() {
        super.onDestroy();
    }

    public void updateList(String word) {
        if (adapter != null) {
            wordsHaveAudioDao.insert(new WordsHaveAudio(word, languageCode));
            adapter.addWordInWordsHaveAudioList(word);
            adapter.remove(word);
            adapter.notifyDataSetChanged();
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
                    adapter.notifyDataSetChanged();
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
                if (ShowCasePref.INSTANCE.isNotShowed(ShowCasePref.LIST_ITEM_SPELL_4_WIKI) && resultListView != null && resultListView.getChildAt(0) != null) {
                    sequence.addPrompt(getPromptBuilder()
                            .setTarget(resultListView.getChildAt(0))
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirmation);
        builder.setMessage(R.string.confirm_to_back);
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> super.onBackPressed());
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
