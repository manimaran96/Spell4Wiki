package com.manimarank.spell4wiki.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.utils.GeneralUtils
import com.manimarank.spell4wiki.utils.SnackBarUtils
import com.manimarank.spell4wiki.utils.constants.AppConstants
import kotlinx.android.synthetic.main.activity_web_view_content.*


class CommonWebContentActivity : AppCompatActivity() {
    private var url: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view_content)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        val bundle = intent.extras
        if (bundle != null) {
            var title: String? = ""
            if (bundle.containsKey(AppConstants.TITLE)) {
                title = bundle.getString(AppConstants.TITLE)
                setTitle(title)
            }
            if (bundle.containsKey(AppConstants.URL)) {
                url = bundle.getString(AppConstants.URL)
                if (!TextUtils.isEmpty(url))
                    if (GeneralUtils.isNetworkConnected(applicationContext))
                        loadWebPageContent()
                    else
                        SnackBarUtils.showLong(webView, getString(R.string.check_internet))
                else
                    SnackBarUtils.showNormal(webView, getString(R.string.something_went_wrong))
            }
        }
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun loadWebPageContent() {
        var isWebPageNotFound = false
        webView.loadUrl(url)
        // Enable Javascript
        webView.settings.javaScriptEnabled = true
        webView.isHorizontalScrollBarEnabled = false
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.setSupportZoom(true)
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE;
        webView.settings.domStorageEnabled = true;
        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                val deepLinkUri: Uri = Uri.parse(url)
                val intent = Intent(Intent.ACTION_VIEW, deepLinkUri)
                startActivity(intent)
                return true
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                isWebPageNotFound = false
                webView.visibility = View.GONE
                loadingProgress.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                if (!isWebPageNotFound) {
                    webView.visibility = View.VISIBLE
                    loadingProgress.visibility = View.GONE
                }
            }

            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                super.onReceivedError(view, request, error)
                isWebPageNotFound = true
                layoutWebPageNotFound.visibility = View.VISIBLE
                loadingProgress.visibility = View.GONE
            }
        }
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(menuItem)
    }
}