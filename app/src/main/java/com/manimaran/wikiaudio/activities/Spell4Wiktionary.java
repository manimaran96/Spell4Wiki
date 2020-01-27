package com.manimaran.wikiaudio.activities;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.adapters.EndlessAdapter;
import com.manimaran.wikiaudio.fragments.BottomSheetFragment;
import com.manimaran.wikiaudio.listerners.CallBackListener;
import com.manimaran.wikiaudio.utils.GeneralUtils;
import com.manimaran.wikiaudio.utils.PrefManager;
import com.manimaran.wikiaudio.views.EndlessListView;
import com.manimaran.wikiaudio.apis.ApiInterface;
import com.manimaran.wikiaudio.apis.ApiClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.ResponseBody;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spell_4_wiktionary);

        pref = new PrefManager(getApplicationContext());
        languageCode = pref.getContributionLangCode();
        init();

        adapter = new EndlessAdapter(this, new ArrayList<>(), R.layout.search_result_row, true);
        CallBackListener listener = langCode -> { };
        adapter.setCallbackListener(listener);
        resultListView.setAdapter(adapter);
        resultListView.setListener(this);
        resultListView.setVisibility(View.VISIBLE);

        loadDataFromServer();

        // Title & Sub title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.spell4wiktionary));
        }

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadDataFromServer();
            }
        });

    }

    /**
     * Init views
     */
    private void init() {
        resultListView = findViewById(R.id.search_result_list);
        refreshLayout = findViewById(R.id.layout_swipe);
        resultListView.setLoadingView(R.layout.loading_row);
    }

    /**
     * Getting words from wiktionary without audio
     */
    private void loadDataFromServer() {

        if (nextOffsetObj == null)
            refreshLayout.setRefreshing(true);

        ApiInterface api = ApiClient.getWiktionaryApi(getApplicationContext()).create(ApiInterface.class);
        String noAudioTitle = pref.getTitleWordsWithoutAudio();
        Call<ResponseBody> call = api.fetchUnAudioRecords(noAudioTitle, nextOffsetObj);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseStr = response.body().string();
                        processSearchResultAudio(responseStr);
                    } catch (IOException e) {
                        e.printStackTrace();
                        searchFailed();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                searchFailed();
            }
        });
    }

    private void processSearchResultAudio(String responseStr) {
        try {
            JSONObject reader = new JSONObject(responseStr);
            ArrayList<String> titleList = new ArrayList<>();
            JSONArray searchResults = reader.getJSONObject("query").optJSONArray("categorymembers");
            if (nextOffsetObj == null)
                resultListView.reset();
            for (int ii = 0; ii < searchResults.length(); ii++) {
                titleList.add(
                        searchResults.getJSONObject(ii).getString("title")//+ " --> " + searchResults.getJSONObject(ii).getString("pageid")
                );
            }
            if (reader.has("continue")) {
                JSONObject jsonObject = reader.getJSONObject("continue");
                if (jsonObject.has("cmcontinue"))
                    nextOffsetObj = jsonObject.getString("cmcontinue");
            } else
                nextOffsetObj = null;
            if (refreshLayout.isRefreshing())
                refreshLayout.setRefreshing(false);
            List<String> wordsList = GeneralUtils.getWordsWithoutAudioListOnly(String.format(getString(R.string.format_file_name_words_already_have_audio), pref.getContributionLangCode()), titleList);
            resultListView.addNewData(wordsList);
            if (wordsList.size() == 0) {
                loadDataFromServer(); // Get more words if no words without audio
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultListView.addNewData(Collections.singletonList("Error accrued"));
        }
    }

    private void searchFailed() {
        resultListView.loadLaterOnScroll();
        if (refreshLayout.isRefreshing())
            refreshLayout.setRefreshing(false);
        Toast.makeText(this, "Please check your connection!\nScroll to try again!", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.spell4wiki_view_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_lang_change:
                loadLanguages();
                break;
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }

        return (super.onOptionsItemSelected(item));
    }

    private void loadLanguages() {
        BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
        CallBackListener callback = langCode -> {
            languageCode = langCode;
            invalidateOptionsMenu();
            loadDataFromServer();
        };
        bottomSheetFragment.setCalBack(callback);
        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
        bottomSheetFragment.setCancelable(false);
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
        if (adapter != null)
            adapter.destroyView();
    }
}
