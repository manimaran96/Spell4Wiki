package com.manimarank.spell4wiki.ui.compose.webview

import android.content.Context
import android.webkit.WebView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manimarank.spell4wiki.data.db.DBHelper
import com.manimarank.spell4wiki.data.prefs.PrefManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing WebView state and business logic
 */
class WebViewViewModel(private val context: Context) : ViewModel() {
    
    private val pref = PrefManager(context)
    private val dbHelper = DBHelper.getInstance(context)
    
    private val _webViewState = MutableStateFlow(WebViewState())
    val webViewState: StateFlow<WebViewState> = _webViewState.asStateFlow()
    
    private val _showRecordButton = MutableStateFlow(false)
    val showRecordButton: StateFlow<Boolean> = _showRecordButton.asStateFlow()
    
    private var currentWebView: WebView? = null
    
    /**
     * Initialize WebView with URL and parameters
     */
    fun initializeWebView(
        url: String,
        title: String? = null,
        word: String? = null,
        languageCode: String? = null,
        isWiktionaryWord: Boolean = false
    ) {
        _webViewState.value = WebViewState(
            isLoading = true,
            hasError = false,
            url = url,
            title = title ?: ""
        )
        
        // Check if record button should be shown
        checkRecordButtonVisibility(word, languageCode, isWiktionaryWord)
    }
    
    /**
     * Handle page started loading
     */
    fun onPageStarted(url: String) {
        _webViewState.value = _webViewState.value.copy(
            isLoading = true,
            hasError = false,
            url = url
        )
    }
    
    /**
     * Handle page finished loading
     */
    fun onPageFinished(webView: WebView, url: String) {
        currentWebView = webView
        _webViewState.value = _webViewState.value.copy(
            isLoading = false,
            hasError = false,
            url = url,
            canGoBack = webView.canGoBack(),
            canGoForward = webView.canGoForward()
        )
    }
    
    /**
     * Handle page load error
     */
    fun onPageError() {
        _webViewState.value = _webViewState.value.copy(
            isLoading = false,
            hasError = true
        )
        _showRecordButton.value = false
    }
    
    /**
     * Perform WebView action
     */
    fun performAction(action: WebViewAction) {
        when (action) {
            is WebViewAction.GoBack -> {
                currentWebView?.goBack()
            }
            is WebViewAction.GoForward -> {
                currentWebView?.goForward()
            }
            is WebViewAction.Refresh -> {
                currentWebView?.reload()
                _webViewState.value = _webViewState.value.copy(
                    isLoading = true,
                    hasError = false
                )
            }
            is WebViewAction.Stop -> {
                currentWebView?.stopLoading()
                _webViewState.value = _webViewState.value.copy(isLoading = false)
            }
            is WebViewAction.LoadUrl -> {
                currentWebView?.loadUrl(action.url)
                _webViewState.value = _webViewState.value.copy(
                    isLoading = true,
                    hasError = false,
                    url = action.url
                )
            }
            is WebViewAction.EvaluateJavaScript -> {
                currentWebView?.evaluateJavascript(action.script, null)
            }
        }
    }
    
    /**
     * Check if record button should be visible
     */
    private fun checkRecordButtonVisibility(
        word: String?,
        languageCode: String?,
        isWiktionaryWord: Boolean
    ) {
        viewModelScope.launch {
            try {
                val shouldShow = isWiktionaryWord && 
                    pref.isAnonymous != true && 
                    !word.isNullOrEmpty() &&
                    !hasWordAlreadyAudio(word, languageCode)
                
                _showRecordButton.value = shouldShow
            } catch (e: Exception) {
                _showRecordButton.value = false
            }
        }
    }
    
    /**
     * Check if word already has audio
     */
    private suspend fun hasWordAlreadyAudio(word: String?, languageCode: String?): Boolean {
        return try {
            val wordsHaveAudioDao = dbHelper.appDatabase.wordsHaveAudioDao
            val wordsAlreadyHaveAudio = wordsHaveAudioDao?.getWordsAlreadyHaveAudioByLanguage(languageCode)
            wordsAlreadyHaveAudio?.contains(word) == true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Hide record button for specific word
     */
    fun hideRecordButton(wordDone: String, currentWord: String?) {
        if (currentWord == wordDone) {
            _showRecordButton.value = false
        }
    }
    
    /**
     * Load word with different language
     */
    fun loadWordWithOtherLang(langCode: String?, word: String?, isWiktionaryWord: Boolean) {
        if (isWiktionaryWord && word != null) {
            val url = String.format("https://%s.wiktionary.org/wiki/%s", langCode, word)
            performAction(WebViewAction.LoadUrl(url))
            checkRecordButtonVisibility(word, langCode, isWiktionaryWord)
        }
    }
    
    /**
     * Get current WebView instance
     */
    fun getCurrentWebView(): WebView? = currentWebView
    
    /**
     * Clean up resources
     */
    override fun onCleared() {
        super.onCleared()
        currentWebView?.stopLoading()
        currentWebView = null
    }
}
