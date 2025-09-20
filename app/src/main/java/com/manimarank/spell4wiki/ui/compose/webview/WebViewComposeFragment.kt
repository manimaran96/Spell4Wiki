package com.manimarank.spell4wiki.ui.compose.webview

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.ui.compose.theme.Spell4WikiTheme
import com.manimarank.spell4wiki.utils.GeneralUtils
import com.manimarank.spell4wiki.utils.SnackBarUtils
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.utils.constants.Urls

/**
 * Compose-based WebView Fragment that can be used within existing Activities
 * This provides a bridge between traditional Fragment-based architecture and Compose
 */
class WebViewComposeFragment : Fragment() {
    
    private lateinit var pref: PrefManager
    private var isWiktionaryWord = false
    private var url: String? = null
    private var word: String? = null
    private var languageCode: String? = null
    
    private val viewModel: WebViewViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return WebViewViewModel(requireContext()) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pref = PrefManager(activity)
        extractArguments()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Spell4WikiTheme {
                    val webViewState by viewModel.webViewState.collectAsState()
                    val showRecordButton by viewModel.showRecordButton.collectAsState()
                    
                    WebViewScreen(
                        url = url ?: "",
                        title = word,
                        word = word,
                        languageCode = languageCode,
                        isWiktionaryWord = isWiktionaryWord,
                        showRecordButton = showRecordButton,
                        onRecordClick = { wordToRecord, langCode ->
                            GeneralUtils.showRecordDialog(
                                requireActivity(),
                                wordToRecord,
                                langCode
                            )
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeWebView()
    }

    private fun extractArguments() {
        val bundle = activity?.intent?.extras
        if (bundle != null) {
            if (bundle.containsKey(AppConstants.URL)) url = bundle.getString(AppConstants.URL)
            if (bundle.containsKey(AppConstants.IS_WIKTIONARY_WORD)) {
                isWiktionaryWord = bundle.getBoolean(AppConstants.IS_WIKTIONARY_WORD)
            }
            if (bundle.containsKey(AppConstants.TITLE)) word = bundle.getString(AppConstants.TITLE)
            if (bundle.containsKey(AppConstants.LANGUAGE_CODE)) {
                languageCode = bundle.getString(AppConstants.LANGUAGE_CODE)
            }
        }
    }

    private fun initializeWebView() {
        viewModel.initializeWebView(
            url = url ?: "",
            title = word,
            word = word,
            languageCode = languageCode,
            isWiktionaryWord = isWiktionaryWord
        )
    }

    /**
     * Navigation methods for compatibility with existing code
     */
    fun backwardWebPage() {
        viewModel.performAction(WebViewAction.GoBack)
    }

    fun forwardWebPage() {
        viewModel.performAction(WebViewAction.GoForward)
    }

    fun canGoForward(): Boolean {
        return viewModel.webViewState.value.canGoForward
    }

    fun canGoBackward(): Boolean {
        return viewModel.webViewState.value.canGoBack
    }

    fun refreshWebPage() {
        viewModel.performAction(WebViewAction.Refresh)
    }

    fun openInAppBrowser() {
        val currentUrl = viewModel.webViewState.value.url
        if (currentUrl.isNotEmpty()) {
            GeneralUtils.openUrlInBrowser(requireContext(), currentUrl)
        }
    }

    fun copyLink() {
        val currentUrl = viewModel.webViewState.value.url
        if (currentUrl.isNotEmpty()) {
            val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clipData = ClipData.newPlainText(AppConstants.URL, Uri.decode(currentUrl))
            clipboardManager?.setPrimaryClip(clipData)
            SnackBarUtils.showLong(requireView(), getString(R.string.link_copied))
        }
    }

    fun shareLink() {
        try {
            val currentUrl = viewModel.webViewState.value.url
            if (currentUrl.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                val appInfo = """
                    ${getString(R.string.app_description)}
                    
                    ${String.format(getString(R.string.app_share_link), Urls.APP_LINK)}
                    """.trimIndent()
                val shareMsg = """
                    ${String.format(getString(R.string.link_share_message), Uri.decode(currentUrl))}

                    $appInfo
                    """.trimIndent()
                intent.putExtra(Intent.EXTRA_TEXT, shareMsg)
                startActivity(Intent.createChooser(intent, getString(R.string.link_share_title)))
            }
        } catch (e: Exception) {
            SnackBarUtils.showLong(requireView(), getString(R.string.something_went_wrong))
        }
    }

    fun loadWordWithOtherLang(langCode: String?) {
        viewModel.loadWordWithOtherLang(langCode, word, isWiktionaryWord)
    }

    fun hideRecordButton(wordDone: String) {
        viewModel.hideRecordButton(wordDone, word)
    }

    /**
     * Get current WebView instance for compatibility
     */
    fun getCurrentWebView(): WebView? {
        return viewModel.getCurrentWebView()
    }

    override fun onDestroy() {
        super.onDestroy()
        // ViewModel will handle cleanup automatically
    }

    companion object {
        /**
         * Factory method to create a new instance of WebViewComposeFragment
         */
        fun newInstance(
            url: String,
            title: String? = null,
            isWiktionaryWord: Boolean = false,
            languageCode: String? = null
        ): WebViewComposeFragment {
            return WebViewComposeFragment().apply {
                arguments = Bundle().apply {
                    putString(AppConstants.URL, url)
                    title?.let { putString(AppConstants.TITLE, it) }
                    putBoolean(AppConstants.IS_WIKTIONARY_WORD, isWiktionaryWord)
                    languageCode?.let { putString(AppConstants.LANGUAGE_CODE, it) }
                }
            }
        }
    }
}
