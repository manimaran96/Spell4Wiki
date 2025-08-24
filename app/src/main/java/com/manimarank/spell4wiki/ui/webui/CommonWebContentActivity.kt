package com.manimarank.spell4wiki.ui.webui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.webkit.*
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.db.DBHelper
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.ui.common.BaseActivity
import com.manimarank.spell4wiki.utils.GeneralUtils.showRecordDialog
import com.manimarank.spell4wiki.utils.EdgeToEdgeUtils.setupEdgeToEdgeForWebView
import com.manimarank.spell4wiki.utils.NetworkUtils
import com.manimarank.spell4wiki.utils.NetworkUtils.isConnected
import com.manimarank.spell4wiki.utils.SnackBarUtils
import com.manimarank.spell4wiki.utils.SnackBarUtils.showLong
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.utils.makeGone
import com.manimarank.spell4wiki.utils.makeVisible
import com.manimarank.spell4wiki.databinding.ActivityWebViewContentBinding
import java.lang.Exception


class CommonWebContentActivity : BaseActivity() {

    private lateinit var binding: ActivityWebViewContentBinding
    private var url: String? = null
    private var word: String? = null
    private var languageCode: String? = null
    private var isWiktionaryWord = false
    private var pref: PrefManager? = null
    private var fabShow = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewContentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize preferences
        pref = PrefManager(this)

        // Setup edge-to-edge display for WebView content
        setupEdgeToEdgeForWebView(
            rootView = binding.root,
            webViewContainer = binding.webView
        )
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(AppConstants.TITLE)) {
                title = bundle.getString(AppConstants.TITLE)
                word = bundle.getString(AppConstants.TITLE)
            }
            if (bundle.containsKey(AppConstants.IS_WIKTIONARY_WORD)) {
                isWiktionaryWord = bundle.getBoolean(AppConstants.IS_WIKTIONARY_WORD)
            }
            if (bundle.containsKey(AppConstants.LANGUAGE_CODE)) {
                languageCode = bundle.getString(AppConstants.LANGUAGE_CODE)
            }
            if (bundle.containsKey(AppConstants.URL)) {
                url = bundle.getString(AppConstants.URL)
                if (!TextUtils.isEmpty(url))
                    if (NetworkUtils.isConnected(applicationContext))
                        loadWebPageContent()
                    else
                        SnackBarUtils.showLong(binding.webView, getString(R.string.check_internet))
                else
                    SnackBarUtils.showNormal(binding.webView, getString(R.string.something_went_wrong))
            }
        }

        // Initialize record button
        recordButtonInit()
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun loadWebPageContent() {
        var isWebPageNotFound = false
        binding.webView.loadUrl(url.toString())
        // Enable Javascript
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.isHorizontalScrollBarEnabled = false
        binding.webView.settings.useWideViewPort = false
        binding.webView.settings.loadWithOverviewMode = true
        binding.webView.settings.setSupportZoom(false)
        binding.webView.settings.builtInZoomControls = false
        binding.webView.settings.displayZoomControls = false
        binding.webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        binding.webView.settings.domStorageEnabled = true
        binding.webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                try {
                    val deepLinkUri: Uri = Uri.parse(url)
                    val intent = Intent(Intent.ACTION_VIEW, deepLinkUri)
                    startActivity(intent)
                }catch (e : Exception){
                    SnackBarUtils.showNormal(binding.webView, getString(R.string.something_went_wrong))
                }
                return true
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                isWebPageNotFound = false
                (binding.webView as View).makeGone()
                (binding.loadingProgress as View).makeVisible()
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                if (!isWebPageNotFound) {
                    (binding.webView as View).makeVisible()
                    (binding.loadingProgress as View).makeGone()
                }
            }

            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                super.onReceivedError(view, request, error)
                isWebPageNotFound = true
                (binding.layoutWebPageNotFound as View).makeVisible()
                (binding.loadingProgress as View).makeGone()
            }
        }
    }

    private val isAllowRecord: Boolean
        get() {
            var isValid = false
            try {
                if (isWiktionaryWord && pref?.isAnonymous != true && !TextUtils.isEmpty(word)) {
                    val wordsHaveAudioDao = DBHelper.getInstance(this).appDatabase.wordsHaveAudioDao
                    val wordsAlreadyHaveAudio = wordsHaveAudioDao?.getWordsAlreadyHaveAudioByLanguage(languageCode)
                    isValid = wordsAlreadyHaveAudio?.contains(word) != true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return isValid
        }

    private fun recordButtonInit() {
        if (isAllowRecord) {
            binding.fabRecord.show()
            fabShow = true
            binding.fabRecord.setOnClickListener {
                if (word != null) {
                    if (isConnected(this))
                        showRecordDialog(this, word?.trim(), languageCode)
                    else
                        showLong(binding.fabRecord, getString(R.string.check_internet))
                } else
                    showLong(binding.fabRecord, getString(R.string.provide_valid_word))
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                binding.webView.setOnScrollChangeListener { _: View?, _: Int, scrollY: Int, _: Int, _: Int ->
                    if (scrollY > 0) {
                        binding.fabRecord.hide()
                        Handler().postDelayed({ if (fabShow) binding.fabRecord.show() }, 1500)
                    }
                    if (scrollY < 0 && fabShow) {
                        binding.fabRecord.show()
                    }
                }
            }
        } else
            binding.fabRecord.hide()
    }

    fun updateList(word: String?) {
        if (word == this.word) {
            fabShow = false
            binding.fabRecord.hide()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppConstants.RC_UPLOAD_DIALOG) {
            if (data != null && data.hasExtra(AppConstants.WORD)) {
                updateList(data.getStringExtra(AppConstants.WORD))
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