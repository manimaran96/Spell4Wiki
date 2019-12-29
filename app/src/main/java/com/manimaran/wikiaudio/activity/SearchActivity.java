package com.manimaran.wikiaudio.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import com.manimaran.wikiaudio.wiki_api.ApiInterface;
import com.manimaran.wikiaudio.wiki_api.ApiClient;

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

public class SearchActivity extends AppCompatActivity implements EndlessListView.EndlessListener {

    private EndlessListView resultListView;
    private SearchView searchBar;

    private String queryString;
    private Integer nextOffset;
    private PrefManager pref;
    private ApiInterface api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        pref = new PrefManager(getApplicationContext());
        api = ApiClient.getWiktionaryApi(getApplicationContext()).create(ApiInterface.class);

        searchBar = findViewById(R.id.search_bar);
        searchBar.requestFocus();
        searchBar.setIconifiedByDefault(false);
        searchBar.setQueryHint(getResources().getString(R.string.search_here));
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

        resultListView = findViewById(R.id.search_result_list);
        resultListView.setLoadingView(R.layout.loading_row);
        resultListView.setAdapter(new EndlessAdapter(this, new ArrayList<String>(), R.layout.search_result_row, false));
        //
        resultListView.setListener(this);
        resultListView.setVisibility(View.INVISIBLE);

        setTitle();

        if(getIntent() != null && getIntent().getExtras() != null)
        {
            if(getIntent().getExtras().containsKey("search_text"))
            {
                String text = getIntent().getExtras().getString("search_text");
                searchBar.setQuery(text, true);
            }
        }
    }

    private void submitQuery(String s) {
        queryString = s;
        nextOffset = 0;
        resultListView.reset();

        ImageView wiktionaryLogo = findViewById(R.id.wiktionary_logo);
        wiktionaryLogo.setVisibility(View.GONE);
        resultListView.setVisibility(View.VISIBLE);

        search(queryString);
    }

    private void setTitle() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.wiktionary));
            getSupportActionBar().setSubtitle(ApiClient.getUrl(UrlType.WIKTIONARY_PAGE, getApplicationContext()));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.action_settings).setIcon(R.drawable.ic_cancel);
        if (pref.getIsAnonymous()) {
            menu.findItem(R.id.action_logout).setVisible(false);
            menu.findItem(R.id.action_login).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_logout:
                GeneralUtils.logoutAlert(SearchActivity.this);
                return true;
            case R.id.action_about:
                startActivity(new Intent(getApplicationContext(), AboutActivity.class));
                return true;
            case R.id.action_lang_change:
                BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
                CallBackListener callback = langCode -> {
                    resultListView.reset();
                    setTitle();
                };
                bottomSheetFragment.setCalBack(callback);
                bottomSheetFragment.setIsWiktionaryMode(true);
                bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
                bottomSheetFragment.setCancelable(false);
                return true;
            case R.id.action_login:
                pref.logoutUser();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void search(String query) {
        Call<ResponseBody> call = api.fetchRecords(query, nextOffset);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseStr = response.body().string();
                        try {
                            processSearchResult(responseStr);
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
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                searchFailed("Please check your connection!\nScroll to try again!");
            }
        });
    }

    private void searchFailed(String msg) {
        resultListView.loadLaterOnScroll();
        Toast.makeText(this, "Search failed!\n" + msg, Toast.LENGTH_LONG).show();
    }

    private void processSearchResult(String responseStr) throws JSONException {
        JSONObject reader = new JSONObject(responseStr);

        try {
            ArrayList<String> titleList = new ArrayList<>();
            JSONArray searchResults = reader.getJSONObject("query").optJSONArray("search");
            for (int ii = 0; ii < searchResults.length(); ii++) {
                titleList.add(
                        searchResults.getJSONObject(ii).getString("title")  //+ " --> " + searchResults.getJSONObject(ii).getString("pageid")
                );
            }
            if (reader.has("continue"))
                nextOffset = reader.getJSONObject("continue").getInt("sroffset");
            else
                nextOffset = null;
            resultListView.addNewData(titleList);
        } catch (Exception e) {
            e.printStackTrace();
            resultListView.addNewData(Collections.singletonList("Error accrued"));
        }
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

