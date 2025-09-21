package com.manimarank.spell4wiki.ui.compose.languageselection

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.db.DBHelper
import com.manimarank.spell4wiki.data.db.dao.WikiLangDao
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.ui.compose.dialogs.DialogMigrationUtils.showLanguageSelectionBottomSheet
import com.manimarank.spell4wiki.ui.dialogs.AppLanguageDialog
import com.manimarank.spell4wiki.ui.listerners.OnLanguageSelectionListener
import com.manimarank.spell4wiki.utils.constants.ListMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Language Selection Compose screen
 * Manages state for both app language and contribution language selection
 */
class LanguageSelectionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LanguageSelectionUiState())
    val uiState: StateFlow<LanguageSelectionUiState> = _uiState.asStateFlow()

    private var wikiLangDao: WikiLangDao? = null

    fun initializeWithContext(context: Context) {
        viewModelScope.launch {
            val pref = PrefManager(context)
            wikiLangDao = DBHelper.getInstance(context).appDatabase.wikiLangDao
            
            _uiState.value = _uiState.value.copy(
                appLanguageInfo = context.getString(
                    R.string.choose_your_preferred_app_language,
                    AppLanguageDialog.getSelectedLanguage()
                ),
                contributionLanguageInfo = getContributionLanguageInfo(context, pref)
            )
        }
    }

    /**
     * Proceed to contribution language selection step
     */
    fun proceedToContributionLanguage() {
        _uiState.value = _uiState.value.copy(showContributionLanguage = true)
    }

    /**
     * Show language selection dialog for contribution language
     */
    fun showLanguageSelection(context: Context) {
        if (context is androidx.fragment.app.FragmentActivity) {
            val callback = object : OnLanguageSelectionListener {
                override fun onCallBackListener(langCode: String?) {
                    updateContributionLanguageInfo(context)
                }
            }
            context.showLanguageSelectionBottomSheet(callback, ListMode.SPELL_4_WIKI_ALL)
        }
    }

    /**
     * Update contribution language info after language selection
     */
    private fun updateContributionLanguageInfo(context: Context) {
        viewModelScope.launch {
            val pref = PrefManager(context)
            _uiState.value = _uiState.value.copy(
                contributionLanguageInfo = getContributionLanguageInfo(context, pref)
            )
        }
    }

    /**
     * Get formatted contribution language information
     */
    private fun getContributionLanguageInfo(context: Context, pref: PrefManager): String {
        return try {
            val languageName = wikiLangDao?.getWikiLanguageWithCode(pref.languageCodeSpell4WikiAll)?.name ?: ""
            context.getString(R.string.choose_your_preferred_contribution_language, languageName)
        } catch (e: Exception) {
            context.getString(R.string.choose_your_preferred_contribution_language, "")
        }
    }
}

/**
 * UI state for Language Selection screen
 */
data class LanguageSelectionUiState(
    val showContributionLanguage: Boolean = false,
    val appLanguageInfo: String = "",
    val contributionLanguageInfo: String = ""
)
