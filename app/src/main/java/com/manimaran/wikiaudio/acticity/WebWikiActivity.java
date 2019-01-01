package com.manimaran.wikiaudio.acticity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.manimaran.wikiaudio.R;

public class WebWikiActivity extends AppCompatActivity {
    private WebView mWebView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_wiki);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null)
        {
            String str = bundle.getString("word");
            setTitle(str);
            mWebView = findViewById(R.id.webview);
            String printUrl =  String.format("https://ta.wiktionary.org/w/index.php?title=%s&printable=yes#p-search",str);
            String wikiUrl = "https://ta.wiktionary.org/wiki/" + str;
            mWebView.loadUrl(wikiUrl);

            // Enable Javascript
            WebSettings webSettings = mWebView.getSettings();
            //webSettings.setJavaScriptEnabled(true);

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

}
