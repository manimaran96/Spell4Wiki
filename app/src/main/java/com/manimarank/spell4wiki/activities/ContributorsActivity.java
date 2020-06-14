package com.manimarank.spell4wiki.activities;

import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.manimarank.spell4wiki.R;
import com.manimarank.spell4wiki.adapters.ContributorsAdapter;
import com.manimarank.spell4wiki.adapters.CoreContributorsAdapter;
import com.manimarank.spell4wiki.apis.ApiClient;
import com.manimarank.spell4wiki.apis.ApiInterface;
import com.manimarank.spell4wiki.models.ContributorData;
import com.manimarank.spell4wiki.models.Contributors;
import com.manimarank.spell4wiki.models.CoreContributors;
import com.manimarank.spell4wiki.utils.NetworkUtils;
import com.manimarank.spell4wiki.utils.ShowCasePref;
import com.manimarank.spell4wiki.utils.SnackBarUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetSequence;
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal;


public class ContributorsActivity extends AppCompatActivity {

    private List<Contributors> contributorsList = new ArrayList<>();
    private List<CoreContributors> coreContributorsList = new ArrayList<>();
    private RecyclerView recyclerViewCodeContributors, recyclerViewCoreContributors;
    private AppCompatTextView txtHelpers;
    private View loadingContributors, layoutCoreContributors;
    private CoreContributorsAdapter coreContributorsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contributors);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.contributors));
        }

        recyclerViewCoreContributors = findViewById(R.id.recyclerViewCoreContributors);
        recyclerViewCodeContributors = findViewById(R.id.recyclerViewCodeContributors);
        txtHelpers = findViewById(R.id.txtHelpers);
        loadingContributors = findViewById(R.id.loadingContributors);
        layoutCoreContributors = findViewById(R.id.layoutCoreContributors);

        ContributorsAdapter contributorsAdapter = new ContributorsAdapter(this, contributorsList);
        recyclerViewCodeContributors.setAdapter(contributorsAdapter);
        recyclerViewCodeContributors.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        coreContributorsAdapter = new CoreContributorsAdapter(this, coreContributorsList);
        recyclerViewCoreContributors.setAdapter(coreContributorsAdapter);
        recyclerViewCoreContributors.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        loadCoreContributorsAndHelpersFromApi();
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1)
                    loadCodeContributorsFromApi();
                else
                    loadCoreContributorsAndHelpersFromApi();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void loadCoreContributorsAndHelpersFromApi() {

        loadingContributors.setVisibility(View.VISIBLE);
        layoutCoreContributors.setVisibility(View.GONE);
        recyclerViewCodeContributors.setVisibility(View.GONE);

        if (NetworkUtils.INSTANCE.isConnected(getApplicationContext())) {

            ApiInterface api = ApiClient.getApi().create(ApiInterface.class);
            Call<ContributorData> call = api.fetchContributorData();

            call.enqueue(new Callback<ContributorData>() {
                @Override
                public void onResponse(@NotNull Call<ContributorData> call, @NotNull Response<ContributorData> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        coreContributorsList.clear();
                        coreContributorsList.addAll(response.body().getCore_contributors());
                        if (coreContributorsAdapter != null)
                            coreContributorsAdapter.notifyDataSetChanged();

                        StringBuilder wikiTechHelpers = new StringBuilder();
                        response.body().getWiki_tech_helpers();
                        if (response.body().getWiki_tech_helpers().size() > 0) {
                            for (String helper : response.body().getWiki_tech_helpers()) {
                                wikiTechHelpers.append(helper).append("\n");
                            }
                            txtHelpers.setText(wikiTechHelpers.toString());
                        }

                        if (loadingContributors != null)
                            loadingContributors.setVisibility(View.GONE);
                        if (layoutCoreContributors != null)
                            layoutCoreContributors.setVisibility(View.VISIBLE);

                        new Handler().postDelayed(() -> callShowCaseUI(), 1000);
                    }

                }

                @Override
                public void onFailure(@NotNull Call<ContributorData> call, @NotNull Throwable t) {
                    t.printStackTrace();
                }
            });
        } else {
            SnackBarUtils.INSTANCE.showLong(recyclerViewCodeContributors, getString(R.string.check_internet));
        }
    }

    private void loadCodeContributorsFromApi() {
        loadingContributors.setVisibility(View.VISIBLE);
        recyclerViewCodeContributors.setVisibility(View.GONE);
        layoutCoreContributors.setVisibility(View.GONE);

        ApiInterface api = ApiClient.getApi().create(ApiInterface.class);
        Call<List<Contributors>> call = api.fetchCodeContributorsList();

        call.enqueue(new Callback<List<Contributors>>() {
            @Override
            public void onResponse(@NotNull Call<List<Contributors>> call, @NotNull Response<List<Contributors>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    contributorsList.clear();
                    contributorsList.addAll(response.body());
                    if (recyclerViewCodeContributors != null && recyclerViewCodeContributors.getAdapter() != null)
                        recyclerViewCodeContributors.getAdapter().notifyDataSetChanged();
                }

                if (loadingContributors != null)
                    loadingContributors.setVisibility(View.GONE);
                if (recyclerViewCodeContributors != null)
                    recyclerViewCodeContributors.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(@NotNull Call<List<Contributors>> call, @NotNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    private void callShowCaseUI() {
        if (!isFinishing() && !isDestroyed() && ShowCasePref.INSTANCE.isNotShowed(ShowCasePref.CORE_CONTRIBUTORS_LIST_ITEM) && recyclerViewCoreContributors != null && recyclerViewCoreContributors.getVisibility() == View.VISIBLE && recyclerViewCoreContributors.getChildAt(0) != null) {
            MaterialTapTargetSequence sequence = new MaterialTapTargetSequence().setSequenceCompleteListener(() -> ShowCasePref.INSTANCE.showed(ShowCasePref.CORE_CONTRIBUTORS_LIST_ITEM));
            sequence.addPrompt(new MaterialTapTargetPrompt.Builder(ContributorsActivity.this)
                    .setPromptFocal(new RectanglePromptFocal())
                    .setAnimationInterpolator(new FastOutSlowInInterpolator())
                    .setFocalPadding(R.dimen.show_case_focal_padding)
                    .setTarget(recyclerViewCoreContributors.getChildAt(0))
                    .setPrimaryText(R.string.sc_t_core_contributors_list_item)
                    .setSecondaryText(R.string.sc_d_core_contributors_list_item));
            sequence.show();
        }
    }

}
