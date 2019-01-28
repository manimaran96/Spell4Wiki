package com.manimaran.wikiaudio.acticity;

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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.fragment.BottomSheetFragment;
import com.manimaran.wikiaudio.util.GeneralUtils;
import com.manimaran.wikiaudio.util.PrefManager;
import com.manimaran.wikiaudio.view.EndlessAdapter;
import com.manimaran.wikiaudio.view.EndlessListView;
import com.manimaran.wikiaudio.wiki.MediaWikiClient;
import com.manimaran.wikiaudio.wiki.ServiceGenerator;

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

public class MainActivity extends AppCompatActivity implements EndlessListView.EndlessListener{

    private EndlessListView resultListView;
    private EndlessAdapter adapter;
    private ProgressBar progressBar;
    private Integer nextOffset;
    private JSONObject nextOffsetObj;
    private boolean doubleBackToExitPressedOnce = false;
    private SwipeRefreshLayout refreshLayout = null;
    private PrefManager pref;
    private MediaWikiClient api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        api = ServiceGenerator.createService(MediaWikiClient.class, getApplicationContext());
        pref = new PrefManager(getApplicationContext());

        progressBar = (ProgressBar) findViewById(R.id.pb);
        resultListView = (EndlessListView) findViewById(R.id.search_result_list);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.layout_swipe);
        resultListView.setLoadingView(R.layout.loading_row);
        adapter = new EndlessAdapter(this, new ArrayList<String>(), R.layout.search_result_row, true);
        resultListView.setAdapter(adapter);
        resultListView.setListener(this);
        resultListView.setVisibility(View.VISIBLE);

        loadDataFromServer();

        setTitle("Wiki Audio - " + pref.getName());

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadDataFromServer();
            }
        });

    }

    private void loadDataFromServer() {
        progressBar.setVisibility(View.VISIBLE);
        Call<ResponseBody> call = api.fetchUnAudioRecords();

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() !=null) {
                    try {
                        String responseStr = response.body().string();
                        try {
                            processSearchResultAudio(responseStr);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            searchFailed("Server misbehaved! Please try again later.");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        searchFailed("Please check your connection!\nScroll to try again!");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                searchFailed("Please check your connection!\nScroll to try again!");
            }
        });
    }

    private void processSearchResultAudio(String responseStr) throws JSONException {
        try {
            JSONObject reader = new JSONObject(responseStr);
            ArrayList<String> titleList = new ArrayList<>();
            JSONArray searchResults = reader.getJSONObject("query").optJSONArray("categorymembers");
            if(nextOffsetObj == null)
                resultListView.reset();
            for (int ii = 0; ii < searchResults.length(); ii++) {
                titleList.add(
                        searchResults.getJSONObject(ii).getString("title")
                        //+ " --> " + searchResults.getJSONObject(ii).getString("pageid")
                );
            }
            if (reader.has("continue")&& false)
                nextOffsetObj = reader.getJSONObject("continue");
            else
                nextOffsetObj = null;
            progressBar.setVisibility(View.GONE);
            if(refreshLayout.isRefreshing())
                refreshLayout.setRefreshing(false);
            resultListView.addNewData(titleList);
        }catch (Exception e)
        {
            e.printStackTrace();
            resultListView.addNewData(Collections.singletonList("Error accrued"));
        }
    }

    private void searchFailed(String s) {
        resultListView.loadLaterOnScroll();
        progressBar.setVisibility(View.GONE);
        if(refreshLayout.isRefreshing())
            refreshLayout.setRefreshing(false);
        Toast.makeText(this, "Search failed!\n" + s, Toast.LENGTH_LONG).show();
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
                GeneralUtils.logoutAlert(MainActivity.this);
                break;
            // action with ID action_settings was selected
            case R.id.action_settings:
                break;
            case R.id.action_lang_change:
                BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
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
        if (nextOffset != null) {
            loadDataFromServer();
            return true;
        } else
            return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(adapter != null)
            adapter.destroyView();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce)
            super.onBackPressed();
        else
        {
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
