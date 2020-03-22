package com.manimaran.wikiaudio.activities;

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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.manimaran.wikiaudio.BuildConfig;
import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.constants.Constants;
import com.manimaran.wikiaudio.constants.UrlType;
import com.manimaran.wikiaudio.utils.DeviceInfoUtil;
import com.manimaran.wikiaudio.utils.GeneralUtils;
import com.manimaran.wikiaudio.utils.PrefManager;


public class AboutActivity extends AppCompatActivity implements View.OnClickListener {
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
        TextView txtFeedback = findViewById(R.id.txtFeedback);
        TextView txtPrivacyPolicy = findViewById(R.id.txtPrivacyPolicy);
        TextView txtTermsOfUse = findViewById(R.id.txtTermsOfUse);


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
        switch (v.getId()) {
            case R.id.txt_rate_app:
                GeneralUtils.openUrl(this, String.format(getString(R.string.link_app_play_store), BuildConfig.APPLICATION_ID), UrlType.EXTERNAL, null);
                break;
            case R.id.txt_share:
                shareApp();
                break;
            case R.id.txt_how_to_contribute:
                GeneralUtils.openUrl(this, getString(R.string.link_how_to_contribute), UrlType.INTERNAL, getString(R.string.how_to_contribute));
                break;
            case R.id.txt_source_code:
                GeneralUtils.openUrl(this, getString(R.string.link_source_code), UrlType.EXTERNAL, null);
                break;
            case R.id.txt_contributors:
                startActivity(new Intent(getApplicationContext(), ContributorsActivity.class));
                break;
            case R.id.txt_third_party_lib:
                Intent intentTPL = new Intent(getApplicationContext(), ListItemActivity.class);
                intentTPL.putExtra(Constants.TITLE, getString(R.string.third_party_libraries));
                startActivity(intentTPL);
                break;
            case R.id.txt_credits:
                Intent intentCredits = new Intent(getApplicationContext(), ListItemActivity.class);
                intentCredits.putExtra(Constants.TITLE, getString(R.string.credits));
                startActivity(intentCredits);
                break;
            case R.id.txt_help_development:
                GeneralUtils.openUrl(this, getString(R.string.link_how_to_contribute), UrlType.INTERNAL, getString(R.string.help_development));
                break;
            case R.id.txtFeedback:
                feedback();
                break;
            case R.id.txtPrivacyPolicy:
                GeneralUtils.openUrl(this, getString(R.string.url_privacy_policy), UrlType.EXTERNAL, getString(R.string.privacy_policy));
                break;
            case R.id.txtTermsOfUse:
                GeneralUtils.openUrl(this, getString(R.string.url_terms_of_use), UrlType.EXTERNAL, getString(R.string.terms_of_use));
                break;
            case R.id.layout_kaniyam:
                GeneralUtils.openUrl(this, getString(R.string.link_kaniyam), UrlType.EXTERNAL, getString(R.string.kaniyam));
                break;
            case R.id.layout_vglug:
                GeneralUtils.openUrl(this, getString(R.string.link_vglug), UrlType.EXTERNAL, getString(R.string.vglug));
                break;
            case R.id.txt_app_version_and_license:
                GeneralUtils.openUrl(this, getString(R.string.url_license_gpl_v3), UrlType.EXTERNAL, "GPLv3 - App License");
                break;
        }

    }

    private void shareApp() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.app_share_message), String.format(getString(R.string.link_app_play_store), BuildConfig.APPLICATION_ID)));
        startActivity(Intent.createChooser(intent, getString(R.string.app_share_title)));
    }

    private void feedback() {

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setType("message/rfc822");
        emailIntent.setData(Uri.parse("mailto:"));
        //emailIntent.setDataAndType(Uri.parse("mailto:"), "message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"manimarankumar96@gmail.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Spell4Wiki App - Feedback");
        emailIntent.putExtra(Intent.EXTRA_TEXT, String.format(
                "\n\n%s\n%s", "-- Basic Information --", getExtraInfo()));

        try {
            startActivity(emailIntent);
        } catch (ActivityNotFoundException ex) {
            GeneralUtils.showSnack(findViewById(R.id.txtFeedback), getString(R.string.no_email_client));
        }
    }

    public String getExtraInfo() {
        StringBuilder builder = new StringBuilder();

        // Getting API Level
        builder.append("API level: ")
                .append(DeviceInfoUtil.getAPILevel())
                .append("\n");

        // Getting Android Version
        builder.append("Android version: ")
                .append(DeviceInfoUtil.getAndroidVersion())
                .append("\n");

        // Getting Device Manufacturer
        builder.append("Device manufacturer: ")
                .append(DeviceInfoUtil.getDeviceManufacturer())
                .append("\n");

        // Getting Device Model
        builder.append("Device model: ")
                .append(DeviceInfoUtil.getDeviceModel())
                .append("\n");

        // Getting Device Name
        builder.append("Device: ")
                .append(DeviceInfoUtil.getDevice())
                .append("\n");

        // Getting App Version
        builder.append("App version name: ")
                .append(BuildConfig.VERSION_NAME)
                .append("\n");

        PrefManager prefManager = new PrefManager(getApplicationContext());
        // Getting Username
        builder.append("User name: ")
                .append(prefManager.getIsAnonymous() ? "Anonymous User" : prefManager.getName())
                .append("\n");


        return builder.toString();
    }

}
