package com.manimaran.wikiaudio.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.adapter.ContributorsAdapter;
import com.manimaran.wikiaudio.model.Contributors;
import com.manimaran.wikiaudio.utils.GeneralUtils;
import com.manimaran.wikiaudio.wiki_api.ApiClient;
import com.manimaran.wikiaudio.wiki_api.ApiInterface;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ContributorsActivity extends AppCompatActivity {

    private List<Contributors> contributorsList = new ArrayList<>();
    private RecyclerView recyclerView;
    private LinearLayout layoutProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contributors);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.contributors));
        }

        recyclerView = findViewById(R.id.recyclerView);
        layoutProgress = findViewById(R.id.layoutProgress);

        ContributorsAdapter adapter = new ContributorsAdapter(this, contributorsList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        if(GeneralUtils.isNetworkConnected(getApplicationContext()))
            loadContributorsFromApi();
        else{
            GeneralUtils.showSnack(recyclerView, getString(R.string.check_internet));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    private void loadContributorsFromApi() {

        layoutProgress.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        ApiInterface api = ApiClient.getApi().create(ApiInterface.class);
        Call<List<Contributors>> call = api.fetchContributorsList();

        call.enqueue(new Callback<List<Contributors>>() {
            @Override
            public void onResponse(@NotNull Call<List<Contributors>> call, @NotNull Response<List<Contributors>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    contributorsList.clear();
                    contributorsList.addAll(response.body());
                    if (recyclerView != null && recyclerView.getAdapter() != null)
                        recyclerView.getAdapter().notifyDataSetChanged();
                }

                if (layoutProgress != null)
                    layoutProgress.setVisibility(View.GONE);
                if (recyclerView != null)
                    recyclerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(@NotNull Call<List<Contributors>> call, @NotNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

}
