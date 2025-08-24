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
import com.manimarank.spell4wiki.databinding.WebViewLayoutBinding

class WebViewFragment : Fragment() {

    private var _binding: WebViewLayoutBinding? = null
    private val binding get() = _binding!!

    private var isWiktionaryWord = false
    private var isWebPageNotFound = false
    private var url: String? = null
    private var word: String? = null
    private var languageCode: String? = null
    private var pref: PrefManager? = null
    private var fabShow = false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = WebViewLayoutBinding.inflate(inflater, container, false)
        pref = PrefManager(activity)

        val bundle = activity?.intent?.extras
        if (bundle != null) {
            if (bundle.containsKey(AppConstants.URL)) url = bundle.getString(AppConstants.URL)
            if (bundle.containsKey(AppConstants.IS_WIKTIONARY_WORD)) isWiktionaryWord = bundle.getBoolean(AppConstants.IS_WIKTIONARY_WORD)
            if (bundle.containsKey(AppConstants.TITLE)) word = bundle.getString(AppConstants.TITLE)
            if (bundle.containsKey(AppConstants.LANGUAGE_CODE)) languageCode = bundle.getString(AppConstants.LANGUAGE_CODE)
        }

        return binding.root
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
            binding.fabRecord.show()
            fabShow = true
            binding.fabRecord.setOnClickListener {
                if (word != null) {
                    if (isConnected(requireActivity()))
                        showRecordDialog(requireActivity(), word?.trim(), languageCode)
                    else
                        showLong(binding.fabRecord, getString(R.string.check_internet))
                } else
                    showLong(binding.fabRecord, getString(R.string.provide_valid_word))
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                binding.webView.setOnScrollChangeListener { _: View?, _: Int, scrollY: Int, _: Int, _: Int ->
                    if (scrollY > 0) {
                        binding.fabRecord.hide()
                        Handler().postDelayed({ if (isAdded && fabShow) binding.fabRecord.show() }, 1500)
                    }
                    if (scrollY < 0 && fabShow) {
                        binding.fabRecord.show()
                    }
                }
            }
        } else
            binding.fabRecord.hide()
    }

    // Unwanted div tag in Wiktionary page
    private val hideDivListForWiktionaryWebPage = listOf("header-container", "banner-container", "page-actions-menu", "was-wotd", "mw-editsection", "mw-footer", "mw-notification-area")
    private val hideDivIdListForWiktionaryWebPage = listOf("page-secondary-actions")

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadWebPage(url: String?) {
        binding.webView.loadUrl(url.toString())

        // Enable Javascript
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.isHorizontalScrollBarEnabled = false
        binding.webView.settings.useWideViewPort = true
        binding.webView.settings.loadWithOverviewMode = true
        binding.webView.settings.setSupportZoom(true)
        binding.webView.settings.builtInZoomControls = true
        binding.webView.settings.displayZoomControls = false
        binding.webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        binding.webView.settings.domStorageEnabled = true
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                isWebPageNotFound = false
                if (isAdded) {
                    loadingVisibility(View.VISIBLE)
                    activity?.invalidateOptionsMenu()
                }
            }

            override fun onPageFinished(view: WebView, url: String) {
                // super.onPageFinished(view, url)
                if (isAdded) {
                    if (isWiktionaryWord) {
                        hideDivListForWiktionaryWebPage.forEach { divClass ->
                            binding.webView.loadUrl("javascript:(function() { document.getElementsByClassName('${divClass}')[0].style.display='none'; })()")
                        }
                        hideDivIdListForWiktionaryWebPage.forEach { divClass ->
                            binding.webView.loadUrl("javascript:(function() { document.getElementById('$divClass').style.display='none'; })()")
                        }

                    }
                    if (!isWebPageNotFound)
                        loadingVisibility(View.GONE)
                    activity?.invalidateOptionsMenu()
                }
            }

            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                super.onReceivedError(view, request, error)
                isWebPageNotFound = true
                if (isAdded) {
                    showPageNotFound()
                    activity?.invalidateOptionsMenu()
                }
            }
        }
    }

    private fun showPageNotFound() {
        if (isAdded) {
            binding.webView.makeInVisible()
            binding.txtLoading.makeInVisible()
            binding.progressBar.makeInVisible()
            binding.layoutWebPageNotFound.makeVisible()
            if (!isConnected(requireContext())) showLong(binding.fabRecord, getString(R.string.check_internet))
        }
    }

    private fun loadingVisibility(visibility: Int) {
        if (isAdded) {
            binding.txtLoading.visibility = visibility
            binding.progressBar.visibility = visibility
            binding.webView.visibility = if (visibility == View.VISIBLE) View.INVISIBLE else View.VISIBLE
            binding.layoutWebPageNotFound.makeInVisible()
        }
    }

    fun backwardWebPage() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else
            showLong(binding.webView, getString(R.string.backward_nothing))
    }

    fun forwardWebPage() {
        if (binding.webView.canGoForward()) {
            binding.webView.goForward()
        } else
            showLong(binding.webView, getString(R.string.forward_nothing))
    }

    fun canGoForward(): Boolean {
        return binding.webView.canGoForward()
    }

    fun canGoBackward(): Boolean {
        return binding.webView.canGoBack()
    }

    fun refreshWebPage() {
        isWebPageNotFound = false
        binding.webView.reload()
    }

    fun openInAppBrowser() {
        openUrlInBrowser(requireContext(), binding.webView.url)
    }

    fun copyLink() {
        if (isAdded) {
            val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            // Create a new ClipData.
            val clipData = ClipData.newPlainText(AppConstants.URL, Uri.decode(binding.webView.url))
            // Set it as primary clip data to copy text to system clipboard.
            clipboardManager?.setPrimaryClip(clipData)
            // Popup a snack bar.
            showLong(binding.webView, getString(R.string.link_copied))
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
                ${String.format(getString(R.string.link_share_message), Uri.decode(binding.webView.url))}

                $appInfo
                """.trimIndent()
            intent.putExtra(Intent.EXTRA_TEXT, shareMsg)
            startActivity(Intent.createChooser(intent, getString(R.string.link_share_title)))
        } catch (e: Exception) {
            showLong(binding.webView, getString(R.string.something_went_wrong))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            _binding?.webView?.stopLoading()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun loadWordWithOtherLang(langCode: String?) {
        if (isWiktionaryWord && word != null) {
            binding.webView.loadUrl(String.format(Urls.WIKTIONARY_WEB, langCode, word))
            recordButtonInit()
        }
    }

    fun hideRecordButton(wordDone: String) {
        if (word == wordDone) {
            if (isAdded) {
                fabShow = false
                binding.fabRecord.hide()
            }
        }
    }
}