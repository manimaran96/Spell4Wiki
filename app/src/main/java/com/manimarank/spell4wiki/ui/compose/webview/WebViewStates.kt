package com.manimarank.spell4wiki.ui.compose.webview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.manimarank.spell4wiki.R

/**
 * Loading state component for WebView
 */
@Composable
fun WebViewLoadingState(
    modifier: Modifier = Modifier,
    loadingText: String = stringResource(R.string.loading)
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(48.dp)
                    .testTag("loading_indicator"),
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = loadingText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

/**
 * Error state component for WebView with Lottie animation
 */
@Composable
fun WebViewErrorState(
    modifier: Modifier = Modifier,
    errorText: String = stringResource(R.string.web_page_not_found),
    onRetry: (() -> Unit)? = null
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.web_page_load_error)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        speed = 2f
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(200.dp)
            )
            
            // Optional retry button can be added here if needed
            onRetry?.let { retryAction ->
                // Add retry button implementation if needed
            }
        }
    }
}

/**
 * Combined WebView state management
 */
@Composable
fun WebViewStateOverlay(
    isLoading: Boolean,
    hasError: Boolean,
    modifier: Modifier = Modifier,
    loadingText: String = stringResource(R.string.loading),
    errorText: String = stringResource(R.string.web_page_not_found),
    onRetry: (() -> Unit)? = null
) {
    when {
        hasError -> {
            WebViewErrorState(
                modifier = modifier,
                errorText = errorText,
                onRetry = onRetry
            )
        }
        isLoading -> {
            WebViewLoadingState(
                modifier = modifier,
                loadingText = loadingText
            )
        }
    }
}

/**
 * Data class to represent WebView state
 */
data class WebViewState(
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val url: String = "",
    val title: String = "",
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false
)

/**
 * WebView actions that can be performed
 */
sealed class WebViewAction {
    object GoBack : WebViewAction()
    object GoForward : WebViewAction()
    object Refresh : WebViewAction()
    object Stop : WebViewAction()
    data class LoadUrl(val url: String) : WebViewAction()
    data class EvaluateJavaScript(val script: String) : WebViewAction()
}
