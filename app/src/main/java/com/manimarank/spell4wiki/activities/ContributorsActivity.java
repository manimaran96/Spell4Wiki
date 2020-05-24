package com.manimarank.spell4wiki.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.manimarank.spell4wiki.R;
import com.manimarank.spell4wiki.adapters.ContributorsAdapter;
import com.manimarank.spell4wiki.models.Contributors;
import com.manimarank.spell4wiki.utils.GeneralUtils;
import com.manimarank.spell4wiki.apis.ApiClient;
import com.manimarank.spell4wiki.apis.ApiInterface;

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
