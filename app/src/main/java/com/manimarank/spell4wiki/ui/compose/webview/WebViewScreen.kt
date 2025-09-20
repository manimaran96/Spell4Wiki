package com.manimarank.spell4wiki.ui.compose.webview

import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.utils.GeneralUtils
import com.manimarank.spell4wiki.utils.NetworkUtils

/**
 * Complete WebView screen with loading states, error handling, and FAB
 */
@Composable
fun WebViewScreen(
    url: String,
    title: String? = null,
    word: String? = null,
    languageCode: String? = null,
    isWiktionaryWord: Boolean = false,
    showRecordButton: Boolean = false,
    onRecordClick: ((String?, String?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pref = remember { PrefManager(context) }
    
    var webViewState by remember {
        mutableStateOf(
            WebViewState(
                isLoading = true,
                hasError = false,
                url = url,
                title = title ?: ""
            )
        )
    }
    
    var webView by remember { mutableStateOf<WebView?>(null) }
    var showFab by remember { mutableStateOf(false) }

    // Check if record button should be shown
    val isAllowRecord = remember(word, languageCode, isWiktionaryWord) {
        showRecordButton && isWiktionaryWord && 
        pref.isAnonymous != true && 
        !word.isNullOrEmpty() &&
        // Add logic to check if word already has audio
        true // Simplified for now
    }

    Scaffold(
        floatingActionButton = {
            if (isAllowRecord && showFab) {
                FloatingActionButton(
                    onClick = {
                        if (NetworkUtils.isConnected(context)) {
                            onRecordClick?.invoke(word?.trim(), languageCode)
                        } else {
                            // Show network error
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .testTag("record_fab")
                        .size(54.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_record),
                        contentDescription = "Record Audio",
                        tint = androidx.compose.ui.graphics.Color.White
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // WebView
            WebViewCompose(
                url = url,
                isWiktionaryWord = isWiktionaryWord,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("webview"),
                onPageStarted = { _, _, _ ->
                    webViewState = webViewState.copy(isLoading = true, hasError = false)
                },
                onPageFinished = { view, loadedUrl ->
                    webView = view
                    webViewState = webViewState.copy(
                        isLoading = false,
                        hasError = false,
                        url = loadedUrl,
                        canGoBack = view.canGoBack(),
                        canGoForward = view.canGoForward()
                    )
                    showFab = isAllowRecord
                },
                onReceivedError = { _, _, _ ->
                    webViewState = webViewState.copy(isLoading = false, hasError = true)
                    showFab = false
                },
                onReceivedHttpError = { _, _, _ ->
                    webViewState = webViewState.copy(isLoading = false, hasError = true)
                    showFab = false
                }
            )

            // Loading and Error States Overlay
            WebViewStateOverlay(
                isLoading = webViewState.isLoading,
                hasError = webViewState.hasError,
                onRetry = {
                    webView?.reload()
                    webViewState = webViewState.copy(isLoading = true, hasError = false)
                },
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

/**
 * Simplified WebView screen for basic usage
 */
@Composable
fun SimpleWebViewScreen(
    url: String,
    modifier: Modifier = Modifier
) {
    var webViewState by remember {
        mutableStateOf(
            WebViewState(
                isLoading = true,
                hasError = false,
                url = url
            )
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        WebViewCompose(
            url = url,
            onPageStarted = { _, _, _ ->
                webViewState = webViewState.copy(isLoading = true, hasError = false)
            },
            onPageFinished = { _, loadedUrl ->
                webViewState = webViewState.copy(
                    isLoading = false,
                    hasError = false,
                    url = loadedUrl
                )
            },
            onReceivedError = { _, _, _ ->
                webViewState = webViewState.copy(isLoading = false, hasError = true)
            },
            onReceivedHttpError = { _, _, _ ->
                webViewState = webViewState.copy(isLoading = false, hasError = true)
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading and Error States Overlay
        WebViewStateOverlay(
            isLoading = webViewState.isLoading,
            hasError = webViewState.hasError,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

/**
 * WebView screen with custom toolbar actions
 */
@Composable
fun WebViewScreenWithActions(
    url: String,
    title: String? = null,
    isWiktionaryWord: Boolean = false,
    onBackPressed: () -> Unit = {},
    onForwardPressed: () -> Unit = {},
    onRefreshPressed: () -> Unit = {},
    onSharePressed: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var webViewState by remember {
        mutableStateOf(
            WebViewState(
                isLoading = true,
                hasError = false,
                url = url,
                title = title ?: ""
            )
        )
    }
    
    var webView by remember { mutableStateOf<WebView?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        WebViewCompose(
            url = url,
            isWiktionaryWord = isWiktionaryWord,
            onPageStarted = { _, _, _ ->
                webViewState = webViewState.copy(isLoading = true, hasError = false)
            },
            onPageFinished = { view, loadedUrl ->
                webView = view
                webViewState = webViewState.copy(
                    isLoading = false,
                    hasError = false,
                    url = loadedUrl,
                    canGoBack = view.canGoBack(),
                    canGoForward = view.canGoForward()
                )
            },
            onReceivedError = { _, _, _ ->
                webViewState = webViewState.copy(isLoading = false, hasError = true)
            },
            onReceivedHttpError = { _, _, _ ->
                webViewState = webViewState.copy(isLoading = false, hasError = true)
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading and Error States Overlay
        WebViewStateOverlay(
            isLoading = webViewState.isLoading,
            hasError = webViewState.hasError,
            onRetry = {
                webView?.reload()
                webViewState = webViewState.copy(isLoading = true, hasError = false)
            },
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
