package com.manimaran.wikiaudio.acticity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.util.PrefManager;
import com.manimaran.wikiaudio.util.UrlType;
import com.manimaran.wikiaudio.wiki.MediaWikiClient;
import com.manimaran.wikiaudio.wiki.ServiceGenerator;

import org.billthefarmer.markdown.MarkdownView;


public class AboutActivity extends AppCompatActivity {
    private WebView mWebView;
    private PrefManager pref;
    private MediaWikiClient api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        pref = new PrefManager(getApplicationContext());
        api = ServiceGenerator.createService(MediaWikiClient.class, getApplicationContext(), UrlType.WIKTIONARY);
        ServiceGenerator.checkCookies();
        setTitle(getString(R.string.about));
        MarkdownView markdownView = (MarkdownView) findViewById(R.id.markdown);
        markdownView.loadMarkdownFile("https://raw.githubusercontent.com/manimaran96/Spell4Wiki/master/README.md");
    }
}
