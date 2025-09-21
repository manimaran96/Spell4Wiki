package com.manimarank.spell4wiki.ui.compose.settings

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.ui.compose.theme.Spell4WikiTheme
import com.manimarank.spell4wiki.ui.compose.dialogs.ThemeSelectionDialog
import com.manimarank.spell4wiki.utils.EdgeToEdgeUtils

/**
 * Compose version of SettingsActivity
 * Provides a modern settings implementation using Jetpack Compose
 */
class SettingsComposeActivity : FragmentActivity() {
    
    private lateinit var pref: PrefManager
    
    private val viewModel: SettingsViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(this@SettingsComposeActivity) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize preferences
        pref = PrefManager(this)
        
        // Setup edge-to-edge display
        // EdgeToEdgeUtils.enableEdgeToEdge() // Not available in this version
        
        setContent {
            Spell4WikiTheme {
                SettingsScreen(
                    viewModel = viewModel,
                    onBackPressed = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settingsState by viewModel.settingsState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings),
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
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                // Language Settings Section
                if (!settingsState.isAnonymous) {
                    LanguageSettingsSection(
                        spell4WikiLanguage = settingsState.spell4WikiLanguage,
                        onSpell4WikiLanguageClick = { viewModel.showSpell4WikiLanguageDialog(context) }
                    )
                }
                
                // License Settings Section
                if (!settingsState.isAnonymous) {
                    LicenseSettingsSection(
                        licenseName = settingsState.licenseName,
                        legalCode = settingsState.legalCode,
                        onLicenseClick = { viewModel.showLicenseDialog(context) }
                    )
                }
                
                // App Settings Section
                AppSettingsSection(
                    appLanguage = settingsState.appLanguage,
                    currentTheme = settingsState.currentTheme,
                    isWiktionaryCleanupEnabled = settingsState.isWiktionaryCleanupEnabled,
                    onAppLanguageClick = { viewModel.showAppLanguageDialog(context) },
                    onThemeClick = { viewModel.showThemeDialog() },
                    onWiktionaryCleanupToggle = { enabled ->
                        viewModel.updateWiktionaryCleanup(enabled)
                    }
                )
                
                // Run Filter Settings Section (only for non-anonymous users)
                if (!settingsState.isAnonymous) {
                    RunFilterSettingsSection(
                        currentCount = settingsState.runFilterCount,
                        onCountChange = { count ->
                            viewModel.updateRunFilterCount(count)
                        }
                    )
                }
            }
        }
    }

    // Show theme selection dialog if needed
    if (settingsState.showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = settingsState.currentTheme,
            onThemeSelected = { theme ->
                viewModel.selectTheme(theme, context)
            },
            onDismiss = {
                viewModel.hideThemeDialog()
            }
        )
    }
}

/**
 * Preview function for development
 */
@Composable
fun SettingsScreenPreview() {
    Spell4WikiTheme {
        // Preview implementation would go here
    }
}
