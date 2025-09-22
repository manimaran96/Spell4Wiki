package com.manimarank.spell4wiki.ui.compose.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.db.DBHelper
import com.manimarank.spell4wiki.data.db.dao.WikiLangDao
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.ui.compose.dialogs.DialogMigrationUtils.showLanguageSelectionBottomSheet
import com.manimarank.spell4wiki.ui.compose.dialogs.DialogMigrationUtils.showLicenseSelectionDialog
import com.manimarank.spell4wiki.ui.compose.dialogs.DialogMigrationUtils.showAppLanguageDialog
import com.manimarank.spell4wiki.ui.listerners.OnLanguageSelectionListener
import com.manimarank.spell4wiki.utils.constants.ListMode
import com.manimarank.spell4wiki.ui.dialogs.AppLanguageDialog
import com.manimarank.spell4wiki.utils.WikiLicense
import com.manimarank.spell4wiki.utils.ThemeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for SettingsActivity
 * Manages settings state and user preferences
 */
class SettingsViewModel(private val context: Context) : ViewModel() {
    
    private val pref = PrefManager(context)
    private val wikiLangDao: WikiLangDao? = DBHelper.getInstance(context).appDatabase.wikiLangDao
    
    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    /**
     * Load current settings from preferences
     */
    private fun loadSettings() {
        viewModelScope.launch {
            val isAnonymous = pref.isAnonymous == true
            
            _settingsState.value = SettingsState(
                isAnonymous = isAnonymous,
                spell4WikiLanguage = getSpell4WikiLanguage(),
                licenseName = getLicenseName(),
                legalCode = getLegalCode(),
                appLanguage = getAppLanguage(),
                currentTheme = getCurrentTheme(),
                isWiktionaryCleanupEnabled = pref.isWiktionaryCleanupEnabled,
                runFilterCount = pref.runFilterNumberOfWordsToCheck ?: 25
            )
        }
    }
    
    /**
     * Show Spell4Wiki language selection dialog
     */
    fun showSpell4WikiLanguageDialog(context: Context) {
        if (context is androidx.fragment.app.FragmentActivity) {
            val callback = object : OnLanguageSelectionListener {
                override fun onCallBackListener(langCode: String?) {
                    loadSettings() // Refresh settings after language change
                }
            }
            context.showLanguageSelectionBottomSheet(callback, ListMode.SPELL_4_WIKI_ALL)
        }
    }
    
    /**
     * Show license selection dialog
     */
    fun showLicenseDialog(context: Context) {
        if (context is androidx.fragment.app.FragmentActivity) {
            context.showLicenseSelectionDialog {
                loadSettings() // Refresh settings after license change
            }
        }
    }
    
    /**
     * Show app language selection dialog
     */
    fun showAppLanguageDialog(context: Context) {
        if (context is androidx.activity.ComponentActivity) {
            context.showAppLanguageDialog()
            // Refresh settings after potential language change
            loadSettings()
        }
    }
    
    /**
     * Show theme selection dialog
     */
    fun showThemeDialog() {
        _settingsState.value = _settingsState.value.copy(showThemeDialog = true)
    }

    /**
     * Hide theme selection dialog
     */
    fun hideThemeDialog() {
        _settingsState.value = _settingsState.value.copy(showThemeDialog = false)
    }

    /**
     * Handle theme selection with enhanced global application
     */
    fun selectTheme(themeMode: String, context: Context) {
        val oldTheme = pref.themeMode

        // Apply theme globally using enhanced utilities
        ThemeUtils.changeTheme(context, themeMode, recreateActivity = true)

        // Refresh settings after theme change
        loadSettings()
        hideThemeDialog()
    }
    
    /**
     * Update Wiktionary cleanup setting
     */
    fun updateWiktionaryCleanup(enabled: Boolean) {
        pref.isWiktionaryCleanupEnabled = enabled
        _settingsState.value = _settingsState.value.copy(
            isWiktionaryCleanupEnabled = enabled
        )
    }
    
    /**
     * Update run filter count
     */
    fun updateRunFilterCount(count: Int) {
        pref.runFilterNumberOfWordsToCheck = count
        _settingsState.value = _settingsState.value.copy(
            runFilterCount = count
        )
    }
    
    /**
     * Get current Spell4Wiki language
     */
    private fun getSpell4WikiLanguage(): String {
        return try {
            val langCode = pref.languageCodeSpell4WikiAll
            val wikiLang = wikiLangDao?.getWikiLanguageWithCode(langCode ?: "en")
            if (wikiLang != null && !wikiLang.name.isNullOrEmpty()) {
                "${wikiLang.localName} - ${wikiLang.name} : $langCode"
            } else {
                "English"
            }
        } catch (e: Exception) {
            "English"
        }
    }
    
    /**
     * Get current license name
     */
    private fun getLicenseName(): String {
        val licenseId = WikiLicense.licenseNameId(pref.uploadAudioLicense)
        return context.getString(licenseId)
    }
    
    /**
     * Get current legal code
     */
    private fun getLegalCode(): String {
        return pref.uploadAudioLicense ?: "CC0"
    }
    
    /**
     * Get current app language
     */
    private fun getAppLanguage(): String {
        return try {
            AppLanguageDialog.getSelectedLanguage()
        } catch (e: Exception) {
            "English"
        }
    }
    
    /**
     * Get current theme setting
     */
    private fun getCurrentTheme(): String {
        return when (pref.themeMode) {
            PrefManager.ThemeMode.LIGHT -> context.getString(R.string.theme_light)
            PrefManager.ThemeMode.DARK -> context.getString(R.string.theme_dark)
            else -> context.getString(R.string.theme_system_default)
        }
    }
}

/**
 * Data class representing settings state
 */
data class SettingsState(
    val isAnonymous: Boolean = false,
    val spell4WikiLanguage: String = "",
    val licenseName: String = "",
    val legalCode: String = "",
    val appLanguage: String = "",
    val currentTheme: String = "",
    val isWiktionaryCleanupEnabled: Boolean = true,
    val runFilterCount: Int = 25,
    val showThemeDialog: Boolean = false
)
