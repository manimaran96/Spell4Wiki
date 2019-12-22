package com.manimaran.wikiaudio.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.manimaran.wikiaudio.BuildConfig;
import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.constant.UrlType;
import com.manimaran.wikiaudio.utils.GeneralUtils;

import br.tiagohm.markdownview.css.styles.Github;


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
        TextView txtContactUs = findViewById(R.id.txt_contact_us);


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
        txtContactUs.setOnClickListener(this);
        layoutKaniyam.setOnClickListener(this);
        layoutVGLUG.setOnClickListener(this);


        txtAppVersionLicense.setMovementMethod(LinkMovementMethod.getInstance());
        String appVersionLicense = getString(R.string.version) + " : " + BuildConfig.VERSION_NAME + " & " +
                                    getString(R.string.license) +" : <a href=" + getString(R.string.link_gplv3) + ">" + "GPLv3" + "</a>";
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
        switch (v.getId()){
            case R.id.txt_rate_app : GeneralUtils.openUrl(this, getString(R.string.link_app_play_store), UrlType.EXTERNAL); break;
            case R.id.txt_share : shareApp(); break;
            case R.id.txt_how_to_contribute : GeneralUtils.openUrl(this, getString(R.string.link_how_to_contribute), UrlType.INTERNAL); break;
            case R.id.txt_source_code : GeneralUtils.openUrl(this, getString(R.string.link_source_code), UrlType.EXTERNAL); break;
            case R.id.txt_contributors : GeneralUtils.openUrl(this, getString(R.string.link_contributors), UrlType.INTERNAL); break;
            case R.id.txt_third_party_lib :
                Intent intentTPL = new Intent(getApplicationContext(), ListItemActivity.class);
                intentTPL.putExtra("title", getString(R.string.third_party_libraries));
                startActivity(intentTPL);
                break;
            case R.id.txt_credits :
                Intent intentCredits = new Intent(getApplicationContext(), ListItemActivity.class);
                intentCredits.putExtra("title", getString(R.string.credits));
                startActivity(intentCredits);
                break;
            case R.id.txt_help_development : GeneralUtils.openUrl(this, getString(R.string.link_how_to_contribute), UrlType.INTERNAL); break;
            case R.id.txt_contact_us : contactUs(); break;
            case R.id.layout_kaniyam : GeneralUtils.openUrl(this, getString(R.string.link_kaniyam), UrlType.INTERNAL); break;
            case R.id.layout_vglug : GeneralUtils.openUrl(this, getString(R.string.link_vglug), UrlType.INTERNAL); break;
        }

    }

    private void shareApp(){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.app_share_message));
        startActivity(Intent.createChooser(intent, getString(R.string.app_share_title)));
    }

    private void contactUs()
    {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.contact_email)});
        try {
            startActivity(Intent.createChooser(i, getString(R.string.contact_via_mail)));
        } catch (ActivityNotFoundException ex) {
            GeneralUtils.showSnack(findViewById(R.id.txt_contact_us), getString(R.string.no_email_client));
        }
    }

}
