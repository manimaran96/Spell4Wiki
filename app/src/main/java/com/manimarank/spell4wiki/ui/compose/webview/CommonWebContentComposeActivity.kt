package com.manimarank.spell4wiki.ui.compose.webview

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.ui.compose.theme.Spell4WikiTheme
import com.manimarank.spell4wiki.utils.EdgeToEdgeUtils
import com.manimarank.spell4wiki.utils.GeneralUtils
import com.manimarank.spell4wiki.utils.NetworkUtils
import com.manimarank.spell4wiki.utils.SnackBarUtils
import com.manimarank.spell4wiki.utils.constants.AppConstants

/**
 * Compose version of CommonWebContentActivity
 * Provides a modern UI implementation using Jetpack Compose
 */
class CommonWebContentComposeActivity : ComponentActivity() {
    
    private lateinit var pref: PrefManager
    private var url: String? = null
    private var word: String? = null
    private var languageCode: String? = null
    private var isWiktionaryWord = false
    private var title: String? = null
    
    private val viewModel: WebViewViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return WebViewViewModel(this@CommonWebContentComposeActivity) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize preferences
        pref = PrefManager(this)
        
        // Extract intent data
        extractIntentData()
        
        // Check network connectivity and load content
        if (!TextUtils.isEmpty(url)) {
            if (NetworkUtils.isConnected(applicationContext)) {
                initializeWebView()
            } else {
                // Handle no network case
                finish()
                return
            }
        } else {
            // Handle invalid URL case
            finish()
            return
        }
        
        setContent {
            Spell4WikiTheme {
                CommonWebContentScreen(
                    viewModel = viewModel,
                    title = title ?: "",
                    url = url ?: "",
                    word = word,
                    languageCode = languageCode,
                    isWiktionaryWord = isWiktionaryWord,
                    onBackPressed = { finish() },
                    onRecordClick = { wordToRecord, langCode ->
                        GeneralUtils.showRecordDialog(
                            this@CommonWebContentComposeActivity,
                            wordToRecord,
                            langCode
                        )
                    }
                )
            }
        }
    }
    
    private fun extractIntentData() {
        intent.extras?.let { bundle ->
            if (bundle.containsKey(AppConstants.URL)) {
                url = bundle.getString(AppConstants.URL)
            }
            if (bundle.containsKey(AppConstants.IS_WIKTIONARY_WORD)) {
                isWiktionaryWord = bundle.getBoolean(AppConstants.IS_WIKTIONARY_WORD)
            }
            if (bundle.containsKey(AppConstants.TITLE)) {
                word = bundle.getString(AppConstants.TITLE)
                title = word
            }
            if (bundle.containsKey(AppConstants.LANGUAGE_CODE)) {
                languageCode = bundle.getString(AppConstants.LANGUAGE_CODE)
            }
        }
    }
    
    private fun initializeWebView() {
        viewModel.initializeWebView(
            url = url ?: "",
            title = title,
            word = word,
            languageCode = languageCode,
            isWiktionaryWord = isWiktionaryWord
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonWebContentScreen(
    viewModel: WebViewViewModel,
    title: String,
    url: String,
    word: String?,
    languageCode: String?,
    isWiktionaryWord: Boolean,
    onBackPressed: () -> Unit,
    onRecordClick: (String?, String?) -> Unit
) {
    val context = LocalContext.current
    val webViewState by viewModel.webViewState.collectAsState()
    val showRecordButton by viewModel.showRecordButton.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            WebViewScreen(
                url = url,
                title = title,
                word = word,
                languageCode = languageCode,
                isWiktionaryWord = isWiktionaryWord,
                showRecordButton = showRecordButton,
                onRecordClick = onRecordClick
            )
        }
    }
}

/**
 * Preview function for development
 */
@Composable
fun CommonWebContentScreenPreview() {
    Spell4WikiTheme {
        // Preview implementation would go here
    }
}
