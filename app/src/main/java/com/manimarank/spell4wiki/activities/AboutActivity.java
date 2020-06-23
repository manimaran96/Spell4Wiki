package com.manimarank.spell4wiki.activities;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.manimaran.crash_reporter.utils.AppUtils;
import com.manimarank.spell4wiki.BuildConfig;
import com.manimarank.spell4wiki.R;
import com.manimarank.spell4wiki.activities.base.BaseActivity;
import com.manimarank.spell4wiki.utils.GeneralUtils;
import com.manimarank.spell4wiki.utils.NetworkUtils;
import com.manimarank.spell4wiki.utils.PrefManager;
import com.manimarank.spell4wiki.utils.SnackBarUtils;
import com.manimarank.spell4wiki.utils.constants.AppConstants;
import com.manimarank.spell4wiki.utils.constants.Urls;


public class AboutActivity extends BaseActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setTitle(getString(R.string.about));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TextView txtAppVersionLicense = findViewById(R.id.txt_app_version_and_license);
        TextView txtRateApp = findViewById(R.id.txt_rate_app);
        TextView txtShareApp = findViewById(R.id.txt_share);
        TextView txtHowToContribute = findViewById(R.id.txt_how_to_contribute);
        TextView txtSourceCode = findViewById(R.id.txt_source_code);
        TextView txtContributors = findViewById(R.id.txt_contributors);
        TextView txtThirdPartyLib = findViewById(R.id.txt_third_party_lib);
        TextView txtCredits = findViewById(R.id.txt_credits);
        TextView txtHelpDevelopment = findViewById(R.id.txt_help_development);
        TextView txtTelegram = findViewById(R.id.txtTelegram);
        TextView txtFeedback = findViewById(R.id.txtFeedback);
        TextView txtPrivacyPolicy = findViewById(R.id.txtPrivacyPolicy);
        TextView txtTermsOfUse = findViewById(R.id.txtHelpTranslate);
        TextView txtPoweredByLink = findViewById(R.id.txtPoweredByLink);
        TextView txtInitiatedByLink = findViewById(R.id.txtInitiatedByLink);

        txtPoweredByLink.setText(Urls.KANIYAM);
        txtInitiatedByLink.setText(Urls.VGLUG);


        LinearLayout layoutKaniyam = findViewById(R.id.layout_kaniyam);
        LinearLayout layoutVGLUG = findViewById(R.id.layout_vglug);


        txtAppVersionLicense.setOnClickListener(this);
        txtRateApp.setOnClickListener(this);
        txtShareApp.setOnClickListener(this);
        txtHowToContribute.setOnClickListener(this);
        txtSourceCode.setOnClickListener(this);
        txtContributors.setOnClickListener(this);
        txtThirdPartyLib.setOnClickListener(this);
        txtCredits.setOnClickListener(this);
        txtHelpDevelopment.setOnClickListener(this);
        txtFeedback.setOnClickListener(this);
        txtTelegram.setOnClickListener(this);
        txtPrivacyPolicy.setOnClickListener(this);
        txtTermsOfUse.setOnClickListener(this);
        layoutKaniyam.setOnClickListener(this);
        layoutVGLUG.setOnClickListener(this);


        txtAppVersionLicense.setMovementMethod(LinkMovementMethod.getInstance());
        String appVersionLicense = getString(R.string.version) + " : " + BuildConfig.VERSION_NAME + " & " +
                getString(R.string.license) + " : <u><font color='" + ContextCompat.getColor(getApplicationContext(), R.color.w_green) + "'>GPLv3</font></u>";
        txtAppVersionLicense.setText(Html.fromHtml(appVersionLicense));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    @Override
    public void onClick(View v) {
        if (!NetworkUtils.INSTANCE.isConnected(getApplicationContext())) {
            SnackBarUtils.INSTANCE.showLong(findViewById(R.id.txt_rate_app), getString(R.string.check_internet));
            return;
        }
        switch (v.getId()) {
            case R.id.txt_rate_app:
                GeneralUtils.openUrlInBrowser(this, Urls.APP_LINK);
                break;
            case R.id.txt_share:
                shareApp();
                break;
            case R.id.txt_how_to_contribute:
                GeneralUtils.openUrl(this, Urls.HOW_TO_CONTRIBUTE, getString(R.string.how_to_contribute));
                break;
            case R.id.txt_source_code:
                GeneralUtils.openUrlInBrowser(this, Urls.SOURCE_CODE);
                break;
            case R.id.txt_contributors:
                startActivity(new Intent(getApplicationContext(), ContributorsActivity.class));
                break;
            case R.id.txt_third_party_lib:
                Intent intentTPL = new Intent(getApplicationContext(), ListItemActivity.class);
                intentTPL.putExtra(AppConstants.TITLE, getString(R.string.third_party_libraries));
                startActivity(intentTPL);
                break;
            case R.id.txt_credits:
                Intent intentCredits = new Intent(getApplicationContext(), ListItemActivity.class);
                intentCredits.putExtra(AppConstants.TITLE, getString(R.string.credits));
                startActivity(intentCredits);
                break;
            case R.id.txt_help_development:
                GeneralUtils.openUrl(this, Urls.HELP_DEVELOPMENT, getString(R.string.help_development));
                break;
            case R.id.txtFeedback:
                feedback();
                break;
            case R.id.txtTelegram:
                GeneralUtils.openUrlInBrowser(this, Urls.TELEGRAM_CHANNEL);
                break;
            case R.id.txtPrivacyPolicy:
                GeneralUtils.openUrlInBrowser(this, Urls.PRIVACY_POLICY);
                break;
            case R.id.txtHelpTranslate:
                GeneralUtils.openUrlInBrowser(this, Urls.HELP_US_TRANSLATE);
                break;
            case R.id.layout_kaniyam:
                GeneralUtils.openUrlInBrowser(this, Urls.KANIYAM);
                break;
            case R.id.layout_vglug:
                GeneralUtils.openUrlInBrowser(this, Urls.VGLUG);
                break;
            case R.id.txt_app_version_and_license:
                GeneralUtils.openUrlInBrowser(this, Urls.GPL_V3);
                break;
        }

    }

    private void shareApp() {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            String appInfo = String.format(getString(R.string.app_share_invite_message), getString(R.string.app_description)) + "\n\n" + String.format(getString(R.string.app_share_link), Urls.APP_LINK);
            intent.putExtra(Intent.EXTRA_TEXT, appInfo);
            startActivity(Intent.createChooser(intent, getString(R.string.app_share_title)));
        } catch (Exception e) {
            SnackBarUtils.INSTANCE.showLong(findViewById(R.id.txt_share), getString(R.string.something_went_wrong));
        }

    }

    @SuppressLint("IntentReset")
    private void feedback() {

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setType("message/rfc822");
        emailIntent.setData(Uri.parse("mailto:"));
        //emailIntent.setDataAndType(Uri.parse("mailto:"), "message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{AppConstants.CONTACT_MAIL});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " App - Feedback");
        emailIntent.putExtra(Intent.EXTRA_TEXT, String.format(
                "\n\n%s\n%s", "-- Basic Information --", getExtraInfo()));

        try {
            startActivity(emailIntent);
        } catch (ActivityNotFoundException ex) {
            SnackBarUtils.INSTANCE.showLong(findViewById(R.id.txtFeedback), getString(R.string.no_email_client));
        }
    }

    public String getExtraInfo() {
        StringBuilder builder = new StringBuilder();

        builder.append(AppUtils.getDeviceDetails(getApplicationContext())).append("\n");

        PrefManager prefManager = new PrefManager(getApplicationContext());
        // Getting Username
        builder.append("User name: ")
                .append(prefManager.getIsAnonymous() ? "Anonymous User" : prefManager.getName())
                .append("\n");


        return builder.toString();
    }

}
