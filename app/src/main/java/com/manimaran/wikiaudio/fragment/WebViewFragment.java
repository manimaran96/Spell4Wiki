package com.manimaran.wikiaudio.fragment;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.constant.Constants;
import com.manimaran.wikiaudio.utils.GeneralUtils;

import java.util.Objects;

public class WebViewFragment extends Fragment {

    private View rootView;
    private WebView webView;
    private ProgressBar progressBar;
    private TextView txtLoading;
    private ImageButton btnRecord;
    private boolean isWitionaryWord = false;
    private String url = "https://manimaran96.wordpress.com/";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.web_view_layout, container, false);

        initUI();


        Bundle bundle = Objects.requireNonNull(getActivity()).getIntent().getExtras();

        if (bundle != null) {
            if (bundle.containsKey(Constants.URL))
                url = bundle.getString(Constants.URL);
            if (bundle.containsKey(Constants.IS_WIKTIONARY_WORD))
                isWitionaryWord = bundle.getBoolean(Constants.IS_WIKTIONARY_WORD);
        }

        loadWebPage(url);

        btnRecord.setVisibility(isWitionaryWord ? View.VISIBLE : View.GONE);

        if (isWitionaryWord) {
            webView.getViewTreeObserver().addOnScrollChangedListener(() -> Log.v("TAG", "+++ scrollchanged " + webView.getScrollY()));
        }
        return rootView;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadWebPage(String url) {

        webView.loadUrl(url);

        // Enable Javascript
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webView.clearHistory();
        webView.setHorizontalScrollBarEnabled(false);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                loadingVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                loadingVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                loadingVisibility(View.GONE);
                GeneralUtils.showToast(getContext(), getString(R.string.error_wepage_load));
            }
        });
    }

    private void loadingVisibility(int visibility) {
        txtLoading.setVisibility(visibility);
        progressBar.setVisibility(visibility);
        webView.setVisibility(visibility == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                Objects.requireNonNull(getActivity()).finish();
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    private void invalidateOptionsMenu() {
        // visible hide
    }

    private void initUI() {
        webView = rootView.findViewById(R.id.webView);
        progressBar = rootView.findViewById(R.id.progressBar);
        txtLoading = rootView.findViewById(R.id.txtLoading);
        btnRecord = rootView.findViewById(R.id.btnRecord);
    }

    public void backwardWebPage() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else
            GeneralUtils.showSnack(webView, "Backward nothing");
    }

    public void forwardWebPage() {
        if (webView.canGoForward()) {
            webView.goForward();
        } else
            GeneralUtils.showSnack(webView, "Forward nothing");
    }

    public void refreshWebPage() {
        webView.reload();
    }

    public void openInAppBrowser() {
        GeneralUtils.openUrlInBrowser(getContext(), webView.getUrl());
    }

    public void copyLink() {
        ClipboardManager clipboardManager = (ClipboardManager) Objects.requireNonNull(getActivity()).getSystemService(Context.CLIPBOARD_SERVICE);
        // Create a new ClipData.
        ClipData clipData = ClipData.newPlainText("Source Text", webView.getUrl());
        // Set it as primary clip data to copy text to system clipboard.
        clipboardManager.setPrimaryClip(clipData);
        // Popup a snack bar.
        GeneralUtils.showSnack(webView, "Link copied");
    }

    public void shareLink() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.link_share_message));
        startActivity(Intent.createChooser(intent, getString(R.string.app_share_title)));
    }
}