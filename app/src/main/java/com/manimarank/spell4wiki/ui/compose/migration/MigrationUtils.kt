package com.manimarank.spell4wiki.ui.compose.migration

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.ui.compose.webview.WebContentActivity
import com.manimarank.spell4wiki.ui.compose.webview.WebViewComposeFragment
import com.manimarank.spell4wiki.ui.webui.CommonWebActivity
import com.manimarank.spell4wiki.ui.webui.CommonWebContentActivity
import com.manimarank.spell4wiki.ui.webui.WebViewFragment
import com.manimarank.spell4wiki.utils.constants.AppConstants

/**
 * Utility object to help with migration between View-based and Compose-based WebView components
 */
object MigrationUtils {
    
    // Feature flag key for enabling Compose WebView
    private const val COMPOSE_WEBVIEW_ENABLED = "compose_webview_enabled"
    
    /**
     * Check if Compose WebView should be used based on user preference or feature flag
     */
    fun shouldUseComposeWebView(context: Context): Boolean {
        // Always use Compose version now that migration is complete
        return true
    }

    /**
     * Check if Compose activities should be used (always true after migration completion)
     */
    fun shouldUseComposeActivities(context: Context): Boolean {
        return true
    }


    
    /**
     * Launch appropriate WebView activity based on migration settings
     */
    fun launchWebViewActivity(
        context: Context,
        url: String,
        title: String? = null,
        isWiktionaryWord: Boolean = false,
        languageCode: String? = null
    ) {
        val intent = if (shouldUseComposeWebView(context)) {
            Intent(context, WebContentActivity::class.java)
        } else {
            Intent(context, CommonWebContentActivity::class.java)
        }
        
        intent.apply {
            putExtra(AppConstants.URL, url)
            title?.let { putExtra(AppConstants.TITLE, it) }
            putExtra(AppConstants.IS_WIKTIONARY_WORD, isWiktionaryWord)
            languageCode?.let { putExtra(AppConstants.LANGUAGE_CODE, it) }
        }
        
        context.startActivity(intent)
    }

    /**
     * Launch Login activity - uses Compose version by default
     */
    fun launchLoginActivity(context: Context) {
        val intent = Intent(context, com.manimarank.spell4wiki.ui.compose.auth.LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /**
     * Launch Main activity - uses Compose version by default
     */
    fun launchMainActivity(context: Context) {
        val intent = Intent(context, com.manimarank.spell4wiki.ui.compose.main.MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
    }

    /**
     * Launch AppIntro activity - uses Compose version by default
     */
    fun launchAppIntroActivity(context: Context) {
        val intent = Intent(context, com.manimarank.spell4wiki.ui.compose.intro.AppIntroActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }

    /**
     * Launch Spell4Wiktionary activity - uses Compose version by default
     */
    fun launchSpell4WiktionaryActivity(context: Context) {
        val intent = Intent(context, com.manimarank.spell4wiki.ui.compose.spell4wiktionary.Spell4WiktionaryActivity::class.java)
        context.startActivity(intent)
    }

    /**
     * Launch Spell4Word activity - uses Compose version by default
     */
    fun launchSpell4WordActivity(context: Context) {
        val intent = Intent(context, com.manimarank.spell4wiki.ui.compose.spell4word.Spell4WordActivity::class.java)
        context.startActivity(intent)
    }

    /**
     * Launch Spell4WordList activity - uses Compose version by default
     */
    fun launchSpell4WordListActivity(context: Context) {
        val intent = Intent(context, com.manimarank.spell4wiki.ui.compose.spell4wordlist.Spell4WordListActivity::class.java)
        context.startActivity(intent)
    }

    /**
     * Launch WiktionarySearch activity - uses Compose version by default
     */
    fun launchWiktionarySearchActivity(context: Context, searchText: String? = null) {
        val intent = Intent(context, com.manimarank.spell4wiki.ui.compose.search.SearchActivity::class.java)
        searchText?.let { intent.putExtra(com.manimarank.spell4wiki.utils.constants.AppConstants.SEARCH_TEXT, it) }
        context.startActivity(intent)
    }

    /**
     * Launch Settings activity - uses Compose version by default
     */
    fun launchSettingsActivity(context: Context) {
        val intent = Intent(context, com.manimarank.spell4wiki.ui.compose.settings.SettingsActivity::class.java)
        context.startActivity(intent)
    }

    /**
     * Launch About activity - uses Compose version by default
     */
    fun launchAboutActivity(context: Context) {
        val intent = Intent(context, com.manimarank.spell4wiki.ui.compose.about.AboutActivity::class.java)
        context.startActivity(intent)
    }

    /**
     * Launch Contributors activity - uses Compose version by default
     */
    fun launchContributorsActivity(context: Context) {
        val intent = Intent(context, com.manimarank.spell4wiki.ui.compose.contributors.ContributorsActivity::class.java)
        context.startActivity(intent)
    }

    /**
     * Launch Language Selection activity - uses Compose version by default
     */
    fun launchLanguageSelectionActivity(context: Context) {
        val intent = Intent(context, com.manimarank.spell4wiki.ui.compose.languageselection.LanguageSelectionActivity::class.java)
        context.startActivity(intent)
    }

    /**
     * Create appropriate WebView fragment based on migration settings
     */
    fun createWebViewFragment(
        context: Context,
        url: String,
        title: String? = null,
        isWiktionaryWord: Boolean = false,
        languageCode: String? = null
    ): Fragment {
        return if (shouldUseComposeWebView(context)) {
            WebViewComposeFragment.newInstance(url, title, isWiktionaryWord, languageCode)
        } else {
            WebViewFragment() // Existing fragment
        }
    }
    
    /**
     * Replace existing WebView fragment with Compose version
     */
    fun replaceWithComposeFragment(
        fragmentManager: FragmentManager,
        containerId: Int,
        url: String,
        title: String? = null,
        isWiktionaryWord: Boolean = false,
        languageCode: String? = null
    ) {
        val fragment = WebViewComposeFragment.newInstance(url, title, isWiktionaryWord, languageCode)
        fragmentManager.beginTransaction()
            .replace(containerId, fragment)
            .commit()
    }
    
    /**
     * Migrate existing CommonWebActivity to use Compose fragment
     */
    fun migrateCommonWebActivity(activity: FragmentActivity, containerId: Int) {
        val intent = activity.intent
        val url = intent.getStringExtra(AppConstants.URL) ?: return
        val title = intent.getStringExtra(AppConstants.TITLE)
        val isWiktionaryWord = intent.getBooleanExtra(AppConstants.IS_WIKTIONARY_WORD, false)
        val languageCode = intent.getStringExtra(AppConstants.LANGUAGE_CODE)
        
        replaceWithComposeFragment(
            activity.supportFragmentManager,
            containerId,
            url,
            title,
            isWiktionaryWord,
            languageCode
        )
    }
}

/**
 * Extension functions for easier migration
 */

/**
 * Extension function for Context to launch WebView with migration support
 */
fun Context.launchWebView(
    url: String,
    title: String? = null,
    isWiktionaryWord: Boolean = false,
    languageCode: String? = null
) {
    MigrationUtils.launchWebViewActivity(this, url, title, isWiktionaryWord, languageCode)
}

/**
 * Extension function for FragmentActivity to create WebView fragment with migration support
 */
fun FragmentActivity.createWebViewFragment(
    url: String,
    title: String? = null,
    isWiktionaryWord: Boolean = false,
    languageCode: String? = null
): Fragment {
    return MigrationUtils.createWebViewFragment(this, url, title, isWiktionaryWord, languageCode)
}

/**
 * Migration preferences helper
 */
object MigrationPreferences {
    
    /**
     * Enable Compose WebView for user
     */
    fun enableComposeWebView(context: Context) {
        val pref = PrefManager(context)
        // This could be extended to have its own preference key
        // For now, we'll tie it to the existing Wiktionary cleanup preference
    }
    
    /**
     * Disable Compose WebView for user
     */
    fun disableComposeWebView(context: Context) {
        val pref = PrefManager(context)
        // Implementation for disabling Compose WebView
    }
    
    /**
     * Check if user has opted into Compose WebView
     */
    fun isComposeWebViewEnabled(context: Context): Boolean {
        return MigrationUtils.shouldUseComposeWebView(context)
    }
}

/**
 * Migration analytics helper for tracking usage
 */
object MigrationAnalytics {
    
    /**
     * Track when Compose WebView is used
     */
    fun trackComposeWebViewUsage(context: Context, url: String) {
        // Implementation for analytics tracking
        // This could integrate with Firebase Analytics or other tracking systems
    }
    
    /**
     * Track when traditional WebView is used
     */
    fun trackTraditionalWebViewUsage(context: Context, url: String) {
        // Implementation for analytics tracking
    }
    
    /**
     * Track migration events
     */
    fun trackMigrationEvent(context: Context, eventType: String, details: Map<String, String> = emptyMap()) {
        // Implementation for tracking migration-related events
    }
}

/**
 * Compatibility layer for existing code
 */
object CompatibilityLayer {
    
    /**
     * Wrapper for existing WebView fragment methods to work with Compose fragment
     */
    fun performWebViewAction(fragment: Fragment, action: String, vararg params: Any) {
        when (fragment) {
            is WebViewComposeFragment -> {
                when (action) {
                    "goBack" -> fragment.backwardWebPage()
                    "goForward" -> fragment.forwardWebPage()
                    "refresh" -> fragment.refreshWebPage()
                    "copyLink" -> fragment.copyLink()
                    "shareLink" -> fragment.shareLink()
                    "openInAppBrowser" -> fragment.openInAppBrowser()
                    "loadWordWithOtherLang" -> {
                        if (params.isNotEmpty()) {
                            fragment.loadWordWithOtherLang(params[0] as? String)
                        }
                    }
                    "hideRecordButton" -> {
                        if (params.isNotEmpty()) {
                            fragment.hideRecordButton(params[0] as String)
                        }
                    }
                }
            }
            is WebViewFragment -> {
                // Handle traditional fragment actions
                when (action) {
                    "goBack" -> fragment.backwardWebPage()
                    "goForward" -> fragment.forwardWebPage()
                    "refresh" -> fragment.refreshWebPage()
                    "copyLink" -> fragment.copyLink()
                    "shareLink" -> fragment.shareLink()
                    "openInAppBrowser" -> fragment.openInAppBrowser()
                    "loadWordWithOtherLang" -> {
                        if (params.isNotEmpty()) {
                            fragment.loadWordWithOtherLang(params[0] as? String)
                        }
                    }
                    "hideRecordButton" -> {
                        if (params.isNotEmpty()) {
                            fragment.hideRecordButton(params[0] as String)
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Check if fragment can go back (works with both fragment types)
     */
    fun canGoBack(fragment: Fragment): Boolean {
        return when (fragment) {
            is WebViewComposeFragment -> fragment.canGoBackward()
            is WebViewFragment -> fragment.canGoBackward()
            else -> false
        }
    }
    
    /**
     * Check if fragment can go forward (works with both fragment types)
     */
    fun canGoForward(fragment: Fragment): Boolean {
        return when (fragment) {
            is WebViewComposeFragment -> fragment.canGoForward()
            is WebViewFragment -> fragment.canGoForward()
            else -> false
        }
    }
}
