package com.manimarank.spell4wiki.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.webkit.WebView
import androidx.appcompat.app.AppCompatDelegate
import com.manimarank.spell4wiki.data.prefs.PrefManager

/**
 * Utility class for managing app-wide theme changes and ensuring consistency
 * across all screens including WebView components
 */
object ThemeUtils {

    /**
     * Apply theme globally across the entire app
     * This ensures immediate theme application without requiring activity recreation
     */
    fun applyThemeGlobally(context: Context, themeMode: String) {
        val nightMode = when (themeMode) {
            PrefManager.ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            PrefManager.ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            PrefManager.ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        
        // Apply theme globally
        AppCompatDelegate.setDefaultNightMode(nightMode)
        
        // Save preference
        val pref = PrefManager(context)
        pref.themeMode = themeMode
    }

    /**
     * Get current theme mode from preferences
     */
    fun getCurrentThemeMode(context: Context): String {
        val pref = PrefManager(context)
        return pref.themeMode
    }

    /**
     * Check if current theme is dark mode
     */
    fun isDarkTheme(context: Context): Boolean {
        val pref = PrefManager(context)
        return when (pref.themeMode) {
            PrefManager.ThemeMode.LIGHT -> false
            PrefManager.ThemeMode.DARK -> true
            PrefManager.ThemeMode.SYSTEM -> {
                val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                nightModeFlags == Configuration.UI_MODE_NIGHT_YES
            }
            else -> {
                val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                nightModeFlags == Configuration.UI_MODE_NIGHT_YES
            }
        }
    }

    /**
     * Apply theme to WebView components
     * This ensures WebView content respects the selected theme
     */
    fun applyThemeToWebView(webView: WebView, context: Context) {
        val isDark = isDarkTheme(context)
        
        // Set background color based on theme
        val backgroundColor = if (isDark) {
            android.graphics.Color.parseColor("#121212") // Dark theme background
        } else {
            android.graphics.Color.WHITE // Light theme background
        }
        webView.setBackgroundColor(backgroundColor)

        // Apply dark mode to WebView content if supported (Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            webView.settings.forceDark = if (isDark) {
                android.webkit.WebSettings.FORCE_DARK_ON
            } else {
                android.webkit.WebSettings.FORCE_DARK_OFF
            }
        }
        
        // For older Android versions, inject CSS for dark mode
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && isDark) {
            val darkModeCSS = """
                javascript:(function() {
                    var style = document.createElement('style');
                    style.innerHTML = `
                        body { 
                            background-color: #121212 !important; 
                            color: #ffffff !important; 
                        }
                        div, p, span, h1, h2, h3, h4, h5, h6 { 
                            color: #ffffff !important; 
                        }
                        a { 
                            color: #bb86fc !important; 
                        }
                        table { 
                            background-color: #1e1e1e !important; 
                            color: #ffffff !important; 
                        }
                        th, td { 
                            background-color: #1e1e1e !important; 
                            color: #ffffff !important; 
                        }
                    `;
                    document.head.appendChild(style);
                })()
            """.trimIndent()
            
            webView.post {
                webView.evaluateJavascript(darkModeCSS, null)
            }
        }
    }

    /**
     * Recreate activity to apply theme changes immediately
     * This is used when theme changes need to be applied immediately
     */
    fun recreateActivityForThemeChange(activity: Activity) {
        activity.recreate()
    }

    /**
     * Get theme display name for UI
     */
    fun getThemeDisplayName(context: Context, themeMode: String): String {
        return when (themeMode) {
            PrefManager.ThemeMode.LIGHT -> "Light"
            PrefManager.ThemeMode.DARK -> "Dark"
            PrefManager.ThemeMode.SYSTEM -> "System Default"
            else -> "System Default"
        }
    }

    /**
     * Get all available theme modes
     */
    fun getAvailableThemeModes(): List<String> {
        return listOf(
            PrefManager.ThemeMode.LIGHT,
            PrefManager.ThemeMode.DARK,
            PrefManager.ThemeMode.SYSTEM
        )
    }

    /**
     * Apply theme to status bar and navigation bar
     */
    fun applyThemeToSystemBars(activity: Activity, isDark: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val window = activity.window
            val decorView = window.decorView
            
            if (isDark) {
                // Dark theme - light content on dark background
                decorView.systemUiVisibility = decorView.systemUiVisibility and 
                    android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            } else {
                // Light theme - dark content on light background
                decorView.systemUiVisibility = decorView.systemUiVisibility or 
                    android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

    /**
     * Initialize theme on app startup
     * This ensures the correct theme is applied when the app starts
     */
    fun initializeTheme(context: Context) {
        val currentTheme = getCurrentThemeMode(context)
        applyThemeGlobally(context, currentTheme)
    }

    /**
     * Handle theme change with immediate application
     * This method ensures theme changes are applied immediately across all components
     */
    fun changeTheme(context: Context, newThemeMode: String, recreateActivity: Boolean = true) {
        // Apply theme globally first
        applyThemeGlobally(context, newThemeMode)
        
        // Recreate current activity if requested
        if (recreateActivity && context is Activity) {
            recreateActivityForThemeChange(context)
        }
    }

    /**
     * Check if theme change requires activity recreation
     * Some theme changes can be applied without recreation
     */
    fun requiresActivityRecreation(oldTheme: String, newTheme: String): Boolean {
        // Always require recreation for theme changes to ensure consistency
        return oldTheme != newTheme
    }
}
