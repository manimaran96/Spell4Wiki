package com.manimaran.wikiaudio.acticity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.util.PrefManager;
import com.manimaran.wikiaudio.util.UrlType;
import com.manimaran.wikiaudio.wiki.MediaWikiClient;
import com.manimaran.wikiaudio.wiki.ServiceGenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import okhttp3.internal.http.StatusLine;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WebWikiActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_wiki);

        PrefManager pref = new PrefManager(getApplicationContext());

        Bundle bundle = getIntent().getExtras();
        if(bundle != null)
        {
            String word = bundle.getString("word");
            boolean isContributionMode = false;
            if(bundle.containsKey("is_contribution_mode"))
            {
                isContributionMode = bundle.getBoolean("is_contribution_mode");
            }
            if(getSupportActionBar() != null)
            {
                getSupportActionBar().setTitle(word);
                getSupportActionBar().setSubtitle(ServiceGenerator.getUrl(UrlType.WIKTIONARY_PAGE, getApplicationContext()));
            }
            String wikiUrl = String.format("https://%s.m.wiktionary.org//wiki/%s", isContributionMode ? pref.getContributionLangCode() : pref.getWiktionaryLangCode(), word);
            loadPage(wikiUrl);
        }

    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadPage(String wikiUrl) {
        WebView mWebView = findViewById(R.id.webview);

        mWebView.loadUrl(wikiUrl);

        // Enable Javascript
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);

        mWebView.setWebViewClient(new WebViewClient());
        // Force links and redirects to open in the WebView instead of in a browser
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                findViewById(R.id.pb).setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                findViewById(R.id.pb).setVisibility(View.GONE);
            }
        });
    }

}
