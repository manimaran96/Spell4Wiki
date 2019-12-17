package com.manimaran.wikiaudio.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.constant.Constants;
import com.manimaran.wikiaudio.fragment.WebViewFragment;
import com.manimaran.wikiaudio.utils.PrefManager;

public class CommonWebActivity extends AppCompatActivity {

    private boolean isContributionMode = false;
    private WebViewFragment fragment = new WebViewFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_web_view);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        PrefManager pref = new PrefManager(getApplicationContext());

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            String title = "";
            if (bundle.containsKey(Constants.TITLE)) {
                title = bundle.getString(Constants.TITLE);
                setTitle(title);
            }

            if (bundle.containsKey(Constants.IS_CONTRIBUTION_MODE))
                isContributionMode = bundle.getBoolean(Constants.IS_CONTRIBUTION_MODE);

            loadFragment(fragment);
        }

    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.web_view_menu, menu);
        //menu.findItem(R.id.action_favorite).setIcon(R.drawable.ic_cancel);
        /*if (isContributionMode) {
            menu.findItem(R.id.action_logout).setVisible(false);
            menu.findItem(R.id.action_login).setVisible(true);
        }*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_favorite:
                return true;
            case R.id.action_share:
                fragment.shareLink();
                return true;
            case R.id.action_refresh:
                fragment.refreshWebPage();
                return true;
            case R.id.action_forward:
                fragment.forwardWebPage();
                return true;
            case R.id.action_backward:
                fragment.backwardWebPage();
                return true;
            case R.id.action_open_in_browser:
                fragment.openInAppBrowser();
                return true;
            case R.id.action_copy_link:
                fragment.copyLink();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
