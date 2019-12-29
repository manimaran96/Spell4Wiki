package com.manimaran.wikiaudio.fragment;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
    private FloatingActionButton fabRecord;
    private boolean isWitionaryWord = false, isContributionMode = false;
    private String url = null;
    private String word = null;


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
            if (bundle.containsKey(Constants.IS_CONTRIBUTION_MODE))
                isContributionMode = bundle.getBoolean(Constants.IS_CONTRIBUTION_MODE);
            if (bundle.containsKey(Constants.TITLE))
                word = bundle.getString(Constants.TITLE);
        }

        if (GeneralUtils.isNetworkConnected(getActivity()))
            loadWebPage(url);
        else
            GeneralUtils.showSnack(webView, getString(R.string.check_internet));

        if (isWitionaryWord && isContributionMode)
            fabRecord.show();
        else
            fabRecord.hide();

        fabRecord.setOnClickListener(v -> {
            if (word != null)
                GeneralUtils.showRecordDialog(getActivity(), word.trim());
            else
                GeneralUtils.showSnack(fabRecord, "Give valid word");
        });

        if (isWitionaryWord && isContributionMode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            webView.setOnScrollChangeListener((webView, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                if (scrollY > 0) {
                    fabRecord.hide();
                    new Handler().postDelayed(() -> {
                        if (fabRecord != null && isAdded())
                            fabRecord.show();
                    }, 1500);
                }
                if (scrollY < 0) {
                    fabRecord.show();
                }
            });
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
                if (getActivity() != null)
                    getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                loadingVisibility(View.GONE);
                if (getActivity() != null)
                    getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                loadingVisibility(View.GONE);
                GeneralUtils.showToast(getContext(), getString(R.string.error_wepage_load));
                if (getActivity() != null)
                    getActivity().invalidateOptionsMenu();
            }
        });
    }

    private void loadingVisibility(int visibility) {
        txtLoading.setVisibility(visibility);
        progressBar.setVisibility(visibility);
        webView.setVisibility(visibility == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
    }

    private void initUI() {
        webView = rootView.findViewById(R.id.webView);
        progressBar = rootView.findViewById(R.id.progressBar);
        txtLoading = rootView.findViewById(R.id.txtLoading);
        fabRecord = rootView.findViewById(R.id.fabRecord);
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

    public boolean canGoForward() {
        return webView != null && webView.canGoForward();
    }

    public boolean canGoBackward() {
        return webView != null && webView.canGoBack();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webView != null)
            webView.stopLoading();
    }

    public void loadWordWithOtherLang(String langCode) {
        if(isWitionaryWord && word != null)
            webView.loadUrl(String.format(getString(R.string.url_wiktionary_web), langCode, word));
    }
}