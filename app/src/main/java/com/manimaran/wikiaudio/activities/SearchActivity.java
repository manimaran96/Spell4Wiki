package com.manimaran.wikiaudio.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.adapters.EndlessAdapter;
import com.manimaran.wikiaudio.fragments.BottomSheetFragment;
import com.manimaran.wikiaudio.listerners.CallBackListener;
import com.manimaran.wikiaudio.utils.PrefManager;
import com.manimaran.wikiaudio.views.EndlessListView;
import com.manimaran.wikiaudio.apis.ApiClient;
import com.manimaran.wikiaudio.apis.ApiInterface;

import org.jetbrains.annotations.NotNull;
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
    private TextView txtNotFound;

    private String queryString;
    private Integer nextOffset;
    private PrefManager pref;
    private ApiInterface api;
    private String languageCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        pref = new PrefManager(SearchActivity.this);
        languageCode = pref.getContributionLangCode();
        api = ApiClient.getWiktionaryApi(getApplicationContext(), languageCode).create(ApiInterface.class);

        txtNotFound = findViewById(R.id.txtNotFound);

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

        if (getIntent() != null && getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey("search_text")) {
                String text = getIntent().getExtras().getString("search_text");
                searchBar.setQuery(text, true);
            }
        }
    }

    private void submitQuery(String s) {
        queryString = s;
        nextOffset = 0;
        txtNotFound.setVisibility(View.GONE);
        resultListView.setVisibility(View.VISIBLE);
        resultListView.reset();

        ImageView wiktionaryLogo = findViewById(R.id.wiktionary_logo);
        wiktionaryLogo.setVisibility(View.GONE);
        search(queryString);
    }

    private void setTitle() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.wiktionary));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            //getSupportActionBar().setSubtitle(ApiClient.getUrl(UrlType.WIKTIONARY_PAGE, getApplicationContext()));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_view_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        setupLanguageSelectorMenuItem(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    private void loadLanguages() {
        BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
        CallBackListener callback = langCode -> {
            languageCode = langCode;
            invalidateOptionsMenu();
            api = ApiClient.getWiktionaryApi(getApplicationContext(), languageCode).create(ApiInterface.class);
            if(queryString != null)
                submitQuery(queryString);
        };
        bottomSheetFragment.setCalBack(callback);
        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
        bottomSheetFragment.setCancelable(false);
        bottomSheetFragment.setIsWiktionaryMode(true);
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
        Call<ResponseBody> call = api.fetchRecords(query, nextOffset);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                Log.i("TAG", "RES SEARCH " + response.toString());
                if (response.isSuccessful() && response.body() != null) {
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
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
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
            for (int i = 0; i < searchResults.length(); i++) {
                titleList.add(
                        searchResults.getJSONObject(i).getString("title")  //+ " --> " + searchResults.getJSONObject(ii).getString("pageid")
                );
            }
            if (reader.has("continue"))
                nextOffset = reader.getJSONObject("continue").getInt("sroffset");
            else
                nextOffset = null;

            resultListView.addNewData(titleList);

            if(nextOffset == null && titleList.size() == 0)
                txtNotFound.setVisibility(View.VISIBLE);
            else
                txtNotFound.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
            txtNotFound.setVisibility(View.VISIBLE);
            //resultListView.addNewData(Collections.singletonList("Error accrued"));
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

