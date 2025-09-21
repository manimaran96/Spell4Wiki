package com.manimarank.spell4wiki.ui.compose.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.db.DBHelper
import com.manimarank.spell4wiki.data.db.dao.WikiLangDao
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.ui.languageselector.LanguageSelectionFragment
import com.manimarank.spell4wiki.utils.WikiLicense
import com.manimarank.spell4wiki.utils.extensions.showLicenseChooseDialog
import android.app.AlertDialog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for SettingsComposeActivity
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
        // Implementation would show language selection dialog
        // This would typically use a DialogFragment or Compose Dialog
    }
    
    /**
     * Show license selection dialog
     */
    fun showLicenseDialog(context: Context) {
        if (context is androidx.activity.ComponentActivity) {
            context.showLicenseChooseDialog {
                loadSettings() // Refresh settings after license change
            }
        }
    }
    
    /**
     * Show app language selection dialog
     */
    fun showAppLanguageDialog(context: Context) {
        // Implementation would show app language selection
        // This could use LanguageSelectionFragment or a Compose equivalent
    }
    
    /**
     * Show theme selection dialog
     */
    fun showThemeDialog(context: Context) {
        if (context is androidx.activity.ComponentActivity) {
            val themes = arrayOf(
                context.getString(R.string.theme_light),
                context.getString(R.string.theme_dark),
                context.getString(R.string.theme_system_default)
            )

            val currentSelection = when (pref.themeMode) {
                PrefManager.ThemeMode.LIGHT -> 0
                PrefManager.ThemeMode.DARK -> 1
                PrefManager.ThemeMode.SYSTEM -> 2
                else -> 2
            }

            AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.choose_theme))
                .setSingleChoiceItems(themes, currentSelection) { dialog, which ->
                    val selectedTheme = when (which) {
                        0 -> PrefManager.ThemeMode.LIGHT
                        1 -> PrefManager.ThemeMode.DARK
                        2 -> PrefManager.ThemeMode.SYSTEM
                        else -> PrefManager.ThemeMode.SYSTEM
                    }

                    pref.themeMode = selectedTheme
                    loadSettings() // Refresh settings after theme change

                    // Recreate activity to apply theme change
                    context.recreate()

                    dialog.dismiss()
                }
                .setNegativeButton(context.getString(R.string.cancel), null)
                .show()
        }
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
            wikiLang?.localName ?: wikiLang?.name ?: "English"
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
        // Implementation would get current app language
        return "English" // Placeholder
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
    val runFilterCount: Int = 25
)
