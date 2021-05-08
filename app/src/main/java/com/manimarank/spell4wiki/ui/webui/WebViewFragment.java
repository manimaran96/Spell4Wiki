package com.manimarank.spell4wiki.ui.webui;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.manimarank.spell4wiki.R;
import com.manimarank.spell4wiki.databases.DBHelper;
import com.manimarank.spell4wiki.databases.dao.WordsHaveAudioDao;
import com.manimarank.spell4wiki.utils.GeneralUtils;
import com.manimarank.spell4wiki.utils.NetworkUtils;
import com.manimarank.spell4wiki.utils.pref.PrefManager;
import com.manimarank.spell4wiki.utils.SnackBarUtils;
import com.manimarank.spell4wiki.utils.constants.AppConstants;
import com.manimarank.spell4wiki.utils.constants.Urls;

import java.util.List;

public class WebViewFragment extends Fragment {

    private View rootView, layoutWebPageNotFound;
    private WebView webView;
    private ProgressBar progressBar;
    private TextView txtLoading;
    private FloatingActionButton fabRecord;
    private boolean isWitionaryWord = false, isWebPageNotFound = false;
    private String url = null;
    private String word = null;
    private String languageCode;
    private PrefManager pref;
    private boolean fabShow = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.web_view_layout, container, false);
        pref = new PrefManager(getActivity());

        initUI();

        if (getActivity() != null) {
            Bundle bundle = getActivity().getIntent().getExtras();
            if (bundle != null) {
                if (bundle.containsKey(AppConstants.URL))
                    url = bundle.getString(AppConstants.URL);
                if (bundle.containsKey(AppConstants.IS_WIKTIONARY_WORD))
                    isWitionaryWord = bundle.getBoolean(AppConstants.IS_WIKTIONARY_WORD);
                if (bundle.containsKey(AppConstants.TITLE))
                    word = bundle.getString(AppConstants.TITLE);

                if (bundle.containsKey(AppConstants.LANGUAGE_CODE))
                    languageCode = bundle.getString(AppConstants.LANGUAGE_CODE);
            }
        }
        return rootView;
    }

    private Boolean isAllowRecord() {
        boolean isValid = false;
        try {
            if (isWitionaryWord && !pref.isAnonymous() && !TextUtils.isEmpty(word)) {
                WordsHaveAudioDao wordsHaveAudioDao = DBHelper.getInstance(getContext()).getAppDatabase().getWordsHaveAudioDao();
                List<String> wordsAlreadyHaveAudio = wordsHaveAudioDao.getWordsAlreadyHaveAudioByLanguage(languageCode);
                isValid = !wordsAlreadyHaveAudio.contains(word);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isValid;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadWebPage(url);
        recordButtonInit();

    }

    private void recordButtonInit() {
        if (isAllowRecord()) {
            fabRecord.show();
            fabShow = true;

            fabRecord.setOnClickListener(v -> {
                if (word != null) {
                    if (getActivity() != null) {
                        if (NetworkUtils.INSTANCE.isConnected(getActivity()))
                            GeneralUtils.showRecordDialog(getActivity(), word.trim(), languageCode);
                        else
                            SnackBarUtils.INSTANCE.showLong(fabRecord, getString(R.string.check_internet));
                    }
                } else
                    SnackBarUtils.INSTANCE.showLong(fabRecord, getString(R.string.provide_valid_word));
            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                webView.setOnScrollChangeListener((webView, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                    if (scrollY > 0) {
                        fabRecord.hide();
                        new Handler().postDelayed(() -> {
                            if (fabRecord != null && isAdded() && fabShow)
                                fabRecord.show();
                        }, 1500);
                    }
                    if (scrollY < 0 && fabShow) {
                        fabRecord.show();
                    }
                });
            }
        } else
            fabRecord.hide();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadWebPage(String url) {

        webView.loadUrl(url);

        // Enable Javascript
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setHorizontalScrollBarEnabled(false);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                isWebPageNotFound = false;
                loadingVisibility(View.VISIBLE);
                if (getActivity() != null)
                    getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (!isWebPageNotFound)
                    loadingVisibility(View.GONE);
                if (getActivity() != null)
                    getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                isWebPageNotFound = true;
                showPageNotFound();
                if (getActivity() != null)
                    getActivity().invalidateOptionsMenu();
            }
        });
    }

    private void showPageNotFound() {
        webView.setVisibility(View.INVISIBLE);
        txtLoading.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        layoutWebPageNotFound.setVisibility(View.VISIBLE);
        if (getActivity() != null && !NetworkUtils.INSTANCE.isConnected(getActivity()))
            SnackBarUtils.INSTANCE.showLong(fabRecord, getString(R.string.check_internet));
    }

    private void loadingVisibility(int visibility) {
        txtLoading.setVisibility(visibility);
        progressBar.setVisibility(visibility);
        webView.setVisibility(visibility == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
        layoutWebPageNotFound.setVisibility(View.INVISIBLE);
    }

    private void initUI() {
        webView = rootView.findViewById(R.id.webView);
        layoutWebPageNotFound = rootView.findViewById(R.id.layoutWebPageNotFound);
        progressBar = rootView.findViewById(R.id.progressBar);
        txtLoading = rootView.findViewById(R.id.txtLoading);
        fabRecord = rootView.findViewById(R.id.fabRecord);
    }

    public void backwardWebPage() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else
            SnackBarUtils.INSTANCE.showLong(webView, getString(R.string.backward_nothing));
    }

    public void forwardWebPage() {
        if (webView.canGoForward()) {
            webView.goForward();
        } else
            SnackBarUtils.INSTANCE.showLong(webView, getString(R.string.forward_nothing));
    }

    public boolean canGoForward() {
        return webView != null && webView.canGoForward();
    }

    public boolean canGoBackward() {
        return webView != null && webView.canGoBack();
    }

    public void refreshWebPage() {
        isWebPageNotFound = false;
        webView.reload();
    }

    public void openInAppBrowser() {
        GeneralUtils.openUrlInBrowser(getActivity(), webView.getUrl());
    }

    public void copyLink() {
        if (isAdded() && getActivity() != null) {
            ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            // Create a new ClipData.
            ClipData clipData = ClipData.newPlainText(AppConstants.URL, Uri.decode(webView.getUrl()));
            // Set it as primary clip data to copy text to system clipboard.
            if (clipboardManager != null)
                clipboardManager.setPrimaryClip(clipData);
            // Popup a snack bar.
            SnackBarUtils.INSTANCE.showLong(webView, getString(R.string.link_copied));
        }
    }

    public void shareLink() {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            String appInfo = getString(R.string.app_description) + "\n\n" + String.format(getString(R.string.app_share_link), Urls.APP_LINK);
            String shareMsg = String.format(getString(R.string.link_share_message), Uri.decode(webView.getUrl())) + "\n\n" + appInfo;
            intent.putExtra(Intent.EXTRA_TEXT, shareMsg);
            startActivity(Intent.createChooser(intent, getString(R.string.link_share_title)));
        }catch (Exception e){
            SnackBarUtils.INSTANCE.showLong(webView, getString(R.string.something_went_wrong));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webView != null)
            webView.stopLoading();
    }

    public void loadWordWithOtherLang(String langCode) {
        if (isWitionaryWord && word != null) {
            webView.loadUrl(String.format(Urls.WIKTIONARY_WEB, langCode, word));
            recordButtonInit();
        }
    }

    public void hideRecordButton(String wordDone) {
        if (word.equals(wordDone)) {
            if (isAdded()) {
                fabShow = false;
                if (fabRecord != null) {
                    fabRecord.hide();
                }
            }
        }
    }
}