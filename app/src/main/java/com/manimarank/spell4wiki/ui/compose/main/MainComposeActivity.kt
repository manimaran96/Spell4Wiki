package com.manimarank.spell4wiki.ui.compose.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.prefs.AppPref.INSTANCE.checkAppUpdateAvailable
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.ui.about.AboutActivity
import com.manimarank.spell4wiki.ui.compose.theme.Spell4WikiTheme
import com.manimarank.spell4wiki.ui.compose.dialogs.LogoutDialog
import com.manimarank.spell4wiki.ui.dialogs.AppLanguageDialog
import com.manimarank.spell4wiki.ui.dialogs.RateAppDialog
import com.manimarank.spell4wiki.ui.dialogs.UpdateAppDialog
import com.manimarank.spell4wiki.ui.settings.SettingsActivity
import com.manimarank.spell4wiki.ui.spell4wiktionary.Spell4Wiktionary
import com.manimarank.spell4wiki.ui.spell4wiktionary.Spell4WordActivity
import com.manimarank.spell4wiki.ui.spell4wiktionary.Spell4WordListActivity
import com.manimarank.spell4wiki.utils.GeneralUtils.hideKeyboard
import com.manimarank.spell4wiki.utils.GeneralUtils.openUrlInBrowser
import com.manimarank.spell4wiki.utils.NetworkUtils.isConnected
import com.manimarank.spell4wiki.utils.SnackBarUtils.showLong
import com.manimarank.spell4wiki.utils.SnackBarUtils.showNormal
import com.manimarank.spell4wiki.utils.constants.Urls

class MainComposeActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var pref: PrefManager
    private var doubleBackToExitPressedOnce = false

    private var languageChangeReceiver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!isDestroyed && !isFinishing && intent.extras != null) {
                val value = intent.extras?.getString(AppLanguageDialog.SELECTED_LANGUAGE, "")
                if (value != null) {
                    recreate()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pref = PrefManager(this)

        // Register language change receiver
        languageChangeReceiver?.let {
            LocalBroadcastManager.getInstance(this).registerReceiver(
                it, IntentFilter(AppLanguageDialog.SELECTED_LANGUAGE)
            )
        }

        setContent {
            Spell4WikiTheme {
                MainScreen(
                    viewModel = viewModel,
                    pref = pref,
                    onSearchSubmit = { query ->
                        viewModel.performSearch(query, this@MainComposeActivity)
                    },
                    onNavigateToSpell4Wiki = {
                        if (checkNetworkAndLogin()) {
                            startActivity(Intent(this@MainComposeActivity, Spell4Wiktionary::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                            })
                        }
                    },
                    onNavigateToSpell4WordList = {
                        if (checkNetworkAndLogin()) {
                            startActivity(Intent(this@MainComposeActivity, Spell4WordListActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                            })
                        }
                    },
                    onNavigateToSpell4Word = {
                        if (checkNetworkAndLogin()) {
                            startActivity(Intent(this@MainComposeActivity, Spell4WordActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                            })
                        }
                    },
                    onNavigateToAbout = {
                        startActivity(Intent(this@MainComposeActivity, com.manimarank.spell4wiki.ui.compose.about.AboutComposeActivity::class.java))
                    },
                    onNavigateToSettings = {
                        startActivity(Intent(this@MainComposeActivity, com.manimarank.spell4wiki.ui.compose.settings.SettingsComposeActivity::class.java))
                    },
                    onLogout = {
                        viewModel.showLogoutDialog(this@MainComposeActivity)
                    },
                    onJoinTelegram = {
                        if (isConnected(applicationContext)) {
                            openUrlInBrowser(this@MainComposeActivity, Urls.TELEGRAM_CHANNEL)
                        } else {
                            // Show network error
                        }
                    },
                    onViewContribution = {
                        viewModel.openContributionUrl(this@MainComposeActivity, pref)
                    },
                    onLogin = {
                        if (isConnected(applicationContext)) {
                            pref.logoutUser()
                        }
                    }
                )
            }
        }

        // Hide keyboard and check for updates
        Handler(Looper.getMainLooper()).post { hideKeyboard(this) }
        
        // Update and Rate the app
        if (checkAppUpdateAvailable(this)) {
            UpdateAppDialog.show(this)
        } else {
            RateAppDialog.show(this)
        }
    }

    private fun checkNetworkAndLogin(): Boolean {
        if (!isConnected(applicationContext)) {
            // Show network error
            return false
        }
        if (pref.isAnonymous == true) {
            // Show login required message
            return false
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        hideKeyboard(this)
        if (!isConnected(applicationContext)) {
            // Show network error
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
        } else {
            doubleBackToExitPressedOnce = true
            // Show exit message
        }
        Handler(Looper.getMainLooper()).postDelayed({ 
            doubleBackToExitPressedOnce = false 
        }, 2000)
    }

    override fun onDestroy() {
        super.onDestroy()
        languageChangeReceiver?.let {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(it)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    pref: PrefManager,
    onSearchSubmit: (String) -> Unit,
    onNavigateToSpell4Wiki: () -> Unit,
    onNavigateToSpell4WordList: () -> Unit,
    onNavigateToSpell4Word: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
    onJoinTelegram: () -> Unit,
    onViewContribution: () -> Unit,
    onLogin: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val context = LocalContext.current

    // Show error message if any
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            // TODO: Show snackbar or toast
            viewModel.clearError()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header Section
            HeaderSection(
                userName = pref.name ?: "",
                isAnonymous = pref.isAnonymous ?: false,
                modifier = Modifier.padding(vertical = 24.dp)
            )

            // Search Section
            SearchSection(
                query = searchQuery,
                onQueryChange = viewModel::updateSearchQuery,
                onSearchSubmit = onSearchSubmit,
                isLoading = isLoading,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Main Options Section
            MainOptionsSection(
                onNavigateToSpell4Wiki = onNavigateToSpell4Wiki,
                onNavigateToSpell4WordList = onNavigateToSpell4WordList,
                onNavigateToSpell4Word = onNavigateToSpell4Word,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // User Actions Section
            UserActionsSection(
                isAnonymous = pref.isAnonymous ?: false,
                onViewContribution = onViewContribution,
                onLogin = onLogin,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Bottom Navigation Section
            BottomNavigationSection(
                onNavigateToAbout = onNavigateToAbout,
                onNavigateToSettings = onNavigateToSettings,
                onLogout = onLogout,
                isAnonymous = pref.isAnonymous ?: false,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Join Telegram Section
            JoinTelegramSection(
                onJoinTelegram = onJoinTelegram,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }

    // Show logout dialog if needed
    val showLogoutDialog by viewModel.showLogoutDialog.collectAsState()
    if (showLogoutDialog) {
        LogoutDialog(
            onConfirm = {
                viewModel.confirmLogout(pref)
            },
            onCancel = {
                viewModel.hideLogoutDialog()
            }
        )
    }
}


