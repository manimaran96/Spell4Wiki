package com.manimarank.spell4wiki.ui.compose.splash

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manimaran.crash_reporter.CrashReporter.showAlertDialogForShareCrash
import com.manimaran.crash_reporter.interfaces.CrashAlertClickListener
import com.manimaran.crash_reporter.utils.CrashUtil.Companion.isHaveCrashData
import com.manimarank.spell4wiki.data.apis.SyncHelper
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.utils.NetworkUtils.isConnected
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for SplashActivity
 * Manages splash screen state and navigation logic
 */
class SplashViewModel(private val context: Context) : ViewModel() {
    
    private val _splashState = MutableStateFlow(SplashState())
    val splashState: StateFlow<SplashState> = _splashState.asStateFlow()
    
    companion object {
        private const val SPLASH_DISPLAY_TIME = 1000L
    }
    
    /**
     * Start the splash screen logic
     */
    fun startSplash() {
        viewModelScope.launch {
            if (isConnected(context)) {
                loadSplash()
            } else {
                _splashState.value = _splashState.value.copy(
                    showRetryButton = true,
                    isNetworkFail = true
                )
            }
        }
    }
    
    /**
     * Check network connection and retry
     */
    fun checkConnection() {
        viewModelScope.launch {
            if (isConnected(context)) {
                _splashState.value = _splashState.value.copy(
                    showRetryButton = false,
                    isNetworkFail = false
                )
                if (_splashState.value.isNetworkFail) {
                    loadSplash()
                } else {
                    callNextScreen()
                }
            }
        }
    }
    
    /**
     * Load splash with delay and crash check
     */
    private suspend fun loadSplash() {
        _splashState.value = _splashState.value.copy(isNetworkFail = false)
        
        // Wait for splash display time
        delay(SPLASH_DISPLAY_TIME)
        
        try {
            if (isHaveCrashData) {
                // Handle crash data - show dialog
                handleCrashData()
            } else {
                callNextScreen()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            callNextScreen()
        }
    }
    
    /**
     * Handle crash data by showing alert dialog
     */
    private fun handleCrashData() {
        val listener = object : CrashAlertClickListener {
            override fun onOkClick() {
                _splashState.value = _splashState.value.copy(showRetryButton = true)
            }
            
            override fun onCancelClick() {
                callNextScreen()
            }
        }
        
        if (context is androidx.activity.ComponentActivity) {
            showAlertDialogForShareCrash(context, listener, true)
        } else {
            // Fallback - just proceed to next screen
            callNextScreen()
        }
    }
    
    /**
     * Navigate to next screen
     */
    private fun callNextScreen() {
        viewModelScope.launch {
            // Sync Wiki Languages
            SyncHelper().syncWikiLanguages()

            val pref = PrefManager(context)

            // Determine navigation destination
            val navigationState = when {
                pref.isFirstTimeLaunch -> SplashState.NavigationDestination.LANGUAGE_SELECTION
                pref.isLoggedIn || pref.isAnonymous == true -> SplashState.NavigationDestination.MAIN
                else -> SplashState.NavigationDestination.LOGIN
            }

            // Set navigation flag
            _splashState.value = _splashState.value.copy(
                shouldNavigate = true,
                navigationDestination = navigationState
            )
        }
    }
}

/**
 * Data class representing splash screen state
 */
data class SplashState(
    val isLoading: Boolean = true,
    val showRetryButton: Boolean = false,
    val isNetworkFail: Boolean = false,
    val shouldNavigate: Boolean = false,
    val navigationDestination: NavigationDestination = NavigationDestination.LOGIN,
    val errorMessage: String? = null
) {
    enum class NavigationDestination {
        LANGUAGE_SELECTION,
        LOGIN,
        MAIN
    }
}
