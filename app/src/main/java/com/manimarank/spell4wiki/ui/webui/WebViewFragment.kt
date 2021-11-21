package com.manimarank.spell4wiki.ui.webui

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.fragment.app.Fragment
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.db.DBHelper
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.utils.GeneralUtils.openUrlInBrowser
import com.manimarank.spell4wiki.utils.GeneralUtils.showRecordDialog
import com.manimarank.spell4wiki.utils.NetworkUtils.isConnected
import com.manimarank.spell4wiki.utils.SnackBarUtils.showLong
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.utils.constants.Urls
import com.manimarank.spell4wiki.utils.makeInVisible
import com.manimarank.spell4wiki.utils.makeVisible
import kotlinx.android.synthetic.main.web_view_layout.*

class WebViewFragment : Fragment() {
    private var isWiktionaryWord = false
    private var isWebPageNotFound = false
    private var url: String? = null
    private var word: String? = null
    private var languageCode: String? = null
    private var pref: PrefManager? = null
    private var fabShow = false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.web_view_layout, container, false)
        pref = PrefManager(activity)

        val bundle = activity?.intent?.extras
        if (bundle != null) {
            if (bundle.containsKey(AppConstants.URL)) url = bundle.getString(AppConstants.URL)
            if (bundle.containsKey(AppConstants.IS_WIKTIONARY_WORD)) isWiktionaryWord = bundle.getBoolean(AppConstants.IS_WIKTIONARY_WORD)
            if (bundle.containsKey(AppConstants.TITLE)) word = bundle.getString(AppConstants.TITLE)
            if (bundle.containsKey(AppConstants.LANGUAGE_CODE)) languageCode = bundle.getString(AppConstants.LANGUAGE_CODE)
        }

        return rootView
    }

    private val isAllowRecord: Boolean
        get() {
            var isValid = false
            try {
                if (isWiktionaryWord && pref?.isAnonymous != true && !TextUtils.isEmpty(word)) {
                    val wordsHaveAudioDao = DBHelper.getInstance(requireContext()).appDatabase.wordsHaveAudioDao
                    val wordsAlreadyHaveAudio = wordsHaveAudioDao?.getWordsAlreadyHaveAudioByLanguage(languageCode)
                    isValid = wordsAlreadyHaveAudio?.contains(word) != true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return isValid
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadWebPage(url)
        recordButtonInit()
    }

    private fun recordButtonInit() {
        if (isAllowRecord) {
            fabRecord.show()
            fabShow = true
            fabRecord.setOnClickListener {
                if (word != null) {
                    if (isConnected(requireActivity()))
                        showRecordDialog(requireActivity(), word?.trim(), languageCode)
                    else
                        showLong(fabRecord, getString(R.string.check_internet))
                } else
                    showLong(fabRecord, getString(R.string.provide_valid_word))
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                webView.setOnScrollChangeListener { _: View?, _: Int, scrollY: Int, _: Int, _: Int ->
                    if (scrollY > 0) {
                        fabRecord.hide()
                        Handler().postDelayed({ if (isAdded && fabShow) fabRecord.show() }, 1500)
                    }
                    if (scrollY < 0 && fabShow) {
                        fabRecord.show()
                    }
                }
            }
        } else
            fabRecord.hide()
    }

    // Unwanted div tag in Wiktionary page
    private val hideDivListForWiktionaryWebPage = listOf("header-container", "mw-footer", "page-actions-menu", "mw-editsection", "pre-content heading-holder", "disambig-see-also")

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadWebPage(url: String?) {
        webView.loadUrl(url.toString())

        // Enable Javascript
        webView.settings.javaScriptEnabled = true
        webView.isHorizontalScrollBarEnabled = false
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.setSupportZoom(true)
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView.settings.domStorageEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                isWebPageNotFound = false
                loadingVisibility(View.VISIBLE)
                activity?.invalidateOptionsMenu()
            }

            override fun onPageFinished(view: WebView, url: String) {
                // super.onPageFinished(view, url)
                if (isWiktionaryWord) {
                    hideDivListForWiktionaryWebPage.forEach { divClass ->
                        webView.loadUrl("javascript:(function() { document.getElementsByClassName('${divClass}')[0].style.display='none'; })()")
                    }
                }
                if (!isWebPageNotFound)
                    loadingVisibility(View.GONE)
                activity?.invalidateOptionsMenu()
            }

            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                super.onReceivedError(view, request, error)
                isWebPageNotFound = true
                showPageNotFound()
                activity?.invalidateOptionsMenu()
            }
        }
    }

    private fun showPageNotFound() {
        if (isAdded) {
            webView.makeInVisible()
            txtLoading.makeInVisible()
            progressBar.makeInVisible()
            layoutWebPageNotFound.makeVisible()
            if (!isConnected(requireContext())) showLong(fabRecord, getString(R.string.check_internet))
        }
    }

    private fun loadingVisibility(visibility: Int) {
        if (isAdded) {
            txtLoading.visibility = visibility
            progressBar.visibility = visibility
            webView.visibility = if (visibility == View.VISIBLE) View.INVISIBLE else View.VISIBLE
            layoutWebPageNotFound.makeInVisible()
        }
    }

    fun backwardWebPage() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else
            showLong(webView, getString(R.string.backward_nothing))
    }

    fun forwardWebPage() {
        if (webView.canGoForward()) {
            webView.goForward()
        } else
            showLong(webView, getString(R.string.forward_nothing))
    }

    fun canGoForward(): Boolean {
        return webView.canGoForward()
    }

    fun canGoBackward(): Boolean {
        return webView.canGoBack()
    }

    fun refreshWebPage() {
        isWebPageNotFound = false
        webView.reload()
    }

    fun openInAppBrowser() {
        openUrlInBrowser(requireContext(), webView.url)
    }

    fun copyLink() {
        if (isAdded) {
            val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            // Create a new ClipData.
            val clipData = ClipData.newPlainText(AppConstants.URL, Uri.decode(webView.url))
            // Set it as primary clip data to copy text to system clipboard.
            clipboardManager?.setPrimaryClip(clipData)
            // Popup a snack bar.
            showLong(webView, getString(R.string.link_copied))
        }
    }

    fun shareLink() {
        try {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            val appInfo = """
                ${getString(R.string.app_description)}
                
                ${String.format(getString(R.string.app_share_link), Urls.APP_LINK)}
                """.trimIndent()
            val shareMsg = """
                ${String.format(getString(R.string.link_share_message), Uri.decode(webView.url))}
                
                $appInfo
                """.trimIndent()
            intent.putExtra(Intent.EXTRA_TEXT, shareMsg)
            startActivity(Intent.createChooser(intent, getString(R.string.link_share_title)))
        } catch (e: Exception) {
            showLong(webView, getString(R.string.something_went_wrong))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webView?.stopLoading()
    }

    fun loadWordWithOtherLang(langCode: String?) {
        if (isWiktionaryWord && word != null) {
            webView.loadUrl(String.format(Urls.WIKTIONARY_WEB, langCode, word))
            recordButtonInit()
        }
    }

    fun hideRecordButton(wordDone: String) {
        if (word == wordDone) {
            if (isAdded) {
                fabShow = false
                fabRecord.hide()
            }
        }
    }
}