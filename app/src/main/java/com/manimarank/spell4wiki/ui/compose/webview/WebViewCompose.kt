package com.manimarank.spell4wiki.ui.compose.webview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.utils.NetworkUtils
import com.manimarank.spell4wiki.utils.Print
import com.manimarank.spell4wiki.utils.ThemeUtils

/**
 * Composable WebView component that wraps Android WebView with Compose integration
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewCompose(
    url: String,
    modifier: Modifier = Modifier,
    isWiktionaryWord: Boolean = false,
    onPageStarted: (WebView, String, Bitmap?) -> Unit = { _, _, _ -> },
    onPageFinished: (WebView, String) -> Unit = { _, _ -> },
    onReceivedError: (WebView, WebResourceRequest, WebResourceError) -> Unit = { _, _, _ -> },
    onReceivedHttpError: (WebView, WebResourceRequest?, WebResourceResponse?) -> Unit = { _, _, _ -> },
    onLoadingStateChanged: (Boolean) -> Unit = {},
    onErrorStateChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val pref = remember { PrefManager(context) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    webView = this
                    setupWebView(this, context, pref, isWiktionaryWord)
                    setupWebViewClient(
                        this,
                        onPageStarted,
                        onPageFinished,
                        onReceivedError,
                        onReceivedHttpError,
                        onLoadingStateChanged,
                        onErrorStateChanged,
                        isWiktionaryWord,
                        pref
                    )
                    loadUrl(url)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            webView?.stopLoading()
            webView?.destroy()
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun setupWebView(
    webView: WebView,
    context: android.content.Context,
    pref: PrefManager,
    isWiktionaryWord: Boolean
) {
    webView.apply {
        settings.javaScriptEnabled = true
        isHorizontalScrollBarEnabled = false
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.cacheMode = if (NetworkUtils.isConnected(context)) {
            WebSettings.LOAD_DEFAULT
        } else {
            WebSettings.LOAD_CACHE_ELSE_NETWORK
        }
        settings.domStorageEnabled = true

        // Apply theme to WebView using enhanced theme utilities
        ThemeUtils.applyThemeToWebView(this, context)
    }
}

private fun setupWebViewClient(
    webView: WebView,
    onPageStarted: (WebView, String, Bitmap?) -> Unit,
    onPageFinished: (WebView, String) -> Unit,
    onReceivedError: (WebView, WebResourceRequest, WebResourceError) -> Unit,
    onReceivedHttpError: (WebView, WebResourceRequest?, WebResourceResponse?) -> Unit,
    onLoadingStateChanged: (Boolean) -> Unit,
    onErrorStateChanged: (Boolean) -> Unit,
    isWiktionaryWord: Boolean,
    pref: PrefManager
) {
    webView.webViewClient = object : WebViewClient() {
        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            onLoadingStateChanged(true)
            onErrorStateChanged(false)
            onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            
            if (isWiktionaryWord) {
                // Apply Wiktionary cleanup based on user preference
                if (pref.isWiktionaryCleanupEnabled) {
                    view.applyWiktionaryReadabilityEnhancement()
                } else {
                    view.applyBasicWiktionaryCleanup()
                }
            }
            
            onLoadingStateChanged(false)
            onPageFinished(view, url)
        }

        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
            super.onReceivedError(view, request, error)
            Print.error("WebView error: ${error.description} for URL: ${request.url}")
            onErrorStateChanged(true)
            onLoadingStateChanged(false)
            onReceivedError(view, request, error)
        }

        override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
            super.onReceivedHttpError(view, request, errorResponse)
            if (request?.isForMainFrame == true) {
                Print.error("WebView HTTP error: ${errorResponse?.statusCode} for URL: ${request.url}")
                onErrorStateChanged(true)
                onLoadingStateChanged(false)
                if (view != null) {
                    onReceivedHttpError(view, request, errorResponse)
                }
            }
        }
    }
}

/**
 * Extension function to apply enhanced Wiktionary readability improvements
 */
private fun WebView.applyWiktionaryReadabilityEnhancement(timeoutMs: Long = 3000) {
    val jsCode = """
        (function() {
          function cleanUp() {
            // 1) Hide unwanted elements
            const hideClasses = [
              "header-container",
              "banner-container", 
              "page-actions-menu",
              "was-wotd",
              "mw-editsection",
              "mw-footer",
              "mw-notification-area"
            ];
            const hideIds = ["page-secondary-actions"];

            hideClasses.forEach(cls => {
              document.querySelectorAll("." + cls).forEach(el => {
                el.style.display = "none";
              });
            });
            hideIds.forEach(id => {
              const el = document.getElementById(id);
              if (el) el.style.display = "none";
            });

            // 2) Expand all collapsible accordions
            document.querySelectorAll(".mf-collapsible-content").forEach(el => {
              el.removeAttribute("hidden");   // remove hidden attr
              el.style.display = "block";     // force visible
            });

            // 3) Update expand → collapse icons
            document.querySelectorAll(".mf-icon-expand").forEach(icon => {
              icon.classList.remove("mf-icon-expand");
              icon.classList.add("mf-icon-collapse");
            });
          }

          // Run once immediately
          cleanUp();

          // Observe DOM changes for lazy content
          const observer = new MutationObserver(() => { cleanUp(); });
          observer.observe(document.body, { childList: true, subtree: true });

          // Stop observing after timeout
          setTimeout(() => { observer.disconnect(); }, ${timeoutMs});
        })();
     """.trimIndent()

    this.post {
        this.evaluateJavascript(jsCode, null)
    }
}

/**
 * Extension function to apply basic Wiktionary cleanup (legacy implementation)
 */
private fun WebView.applyBasicWiktionaryCleanup() {
    val hideDivListForWiktionaryWebPage = listOf(
        "header-container", "banner-container", "page-actions-menu", 
        "was-wotd", "mw-editsection", "mw-footer", "mw-notification-area"
    )
    val hideDivIdListForWiktionaryWebPage = listOf("page-secondary-actions")

    hideDivListForWiktionaryWebPage.forEach { divClass ->
        this.loadUrl("javascript:(function() { document.getElementsByClassName('${divClass}')[0].style.display='none'; })()")
    }
    hideDivIdListForWiktionaryWebPage.forEach { divClass ->
        this.loadUrl("javascript:(function() { document.getElementById('$divClass').style.display='none'; })()")
    }
}
