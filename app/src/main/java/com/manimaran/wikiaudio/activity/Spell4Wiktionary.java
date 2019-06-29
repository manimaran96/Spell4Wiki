package com.manimaran.wikiaudio.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.adapter.EndlessAdapter;
import com.manimaran.wikiaudio.constant.UrlType;
import com.manimaran.wikiaudio.fragment.BottomSheetFragment;
import com.manimaran.wikiaudio.listerner.CallBackListener;
import com.manimaran.wikiaudio.utils.GeneralUtils;
import com.manimaran.wikiaudio.utils.PrefManager;
import com.manimaran.wikiaudio.view.EndlessListView;
import com.manimaran.wikiaudio.wiki_api.MediaWikiClient;
import com.manimaran.wikiaudio.wiki_api.ServiceGenerator;

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
    private boolean doubleBackToExitPressedOnce = false;
    private PrefManager pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spell4_wiktionary);

        pref = new PrefManager(getApplicationContext());

        init();

        adapter = new EndlessAdapter(this, new ArrayList<String>(), R.layout.search_result_row, true);
        CallBackListener listener = new CallBackListener() {
            @Override
            public void OnCallBackListener() {

            }
        };
        adapter.setCallbackListener(listener);
        resultListView.setAdapter(adapter);
        resultListView.setListener(this);
        resultListView.setVisibility(View.VISIBLE);

        loadDataFromServer();

        // Title & Sub title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
            getSupportActionBar().setSubtitle(String.format(getString(R.string.welcome_user), pref.getName()));
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

        MediaWikiClient api = ServiceGenerator.createService(MediaWikiClient.class, getApplicationContext(), UrlType.WIKTIONARY_CONTRIBUTION);
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
                GeneralUtils.logoutAlert(Spell4Wiktionary.this);
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
                CallBackListener callback = new CallBackListener() {
                    @Override
                    public void OnCallBackListener() {
                        loadDataFromServer();
                    }
                };
                bottomSheetFragment.setCalBack(callback);
                bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
                bottomSheetFragment.setCancelable(false);
                break;
            default:
                break;
        }

        return true;
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
