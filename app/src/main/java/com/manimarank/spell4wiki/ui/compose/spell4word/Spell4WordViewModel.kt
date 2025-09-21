package com.manimarank.spell4wiki.ui.compose.spell4word

import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manimarank.spell4wiki.data.db.DBHelper
import com.manimarank.spell4wiki.data.db.dao.WikiLangDao
import com.manimarank.spell4wiki.data.db.dao.WordsHaveAudioDao
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.utils.GeneralUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Spell4WordComposeActivity
 * Manages word input state, language selection, and recording validation
 */
class Spell4WordViewModel : ViewModel() {

    private val _wordInput = MutableStateFlow("")
    val wordInput: StateFlow<String> = _wordInput.asStateFlow()

    private val _languageCode = MutableStateFlow("")
    val languageCode: StateFlow<String> = _languageCode.asStateFlow()

    private val _languageInfo = MutableStateFlow("")
    val languageInfo: StateFlow<String> = _languageInfo.asStateFlow()

    private val _showBackConfirmationDialog = MutableStateFlow(false)
    val showBackConfirmationDialog: StateFlow<Boolean> = _showBackConfirmationDialog.asStateFlow()

    private var wikiLangDao: WikiLangDao? = null
    private var wordsHaveAudioDao: WordsHaveAudioDao? = null
    private var pref: PrefManager? = null

    /**
     * Initialize the ViewModel with context and language code
     */
    fun initialize(context: Context, languageCode: String?) {
        viewModelScope.launch {
            pref = PrefManager(context)
            wikiLangDao = DBHelper.getInstance(context).appDatabase.wikiLangDao
            wordsHaveAudioDao = DBHelper.getInstance(context).appDatabase.wordsHaveAudioDao
            
            updateLanguageCode(context, languageCode ?: "en")
        }
    }

    /**
     * Update word input
     */
    fun updateWordInput(input: String) {
        _wordInput.value = input
    }

    /**
     * Update language code and refresh language info
     */
    fun updateLanguage(context: Context, languageCode: String?) {
        viewModelScope.launch {
            updateLanguageCode(context, languageCode ?: "en")
            // Update preference
            pref?.languageCodeSpell4WikiAll = languageCode
        }
    }

    /**
     * Update language code and language info display
     */
    private fun updateLanguageCode(context: Context, langCode: String) {
        _languageCode.value = langCode
        
        viewModelScope.launch {
            val wikiLang = wikiLangDao?.getWikiLanguageWithCode(langCode)
            val languageInfo = GeneralUtils.getLanguageInfo(context, wikiLang)
            _languageInfo.value = languageInfo ?: ""
        }
    }

    /**
     * Check if recording is allowed for the given word
     * Returns false if the word already has audio recorded
     */
    fun isAllowRecord(word: String): Boolean {
        return try {
            if (pref?.isAnonymous != true && !TextUtils.isEmpty(word)) {
                val wordsAlreadyHaveAudio = wordsHaveAudioDao?.getWordsAlreadyHaveAudioByLanguage(_languageCode.value)
                wordsAlreadyHaveAudio?.contains(word) != true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Show back confirmation dialog
     */
    fun showBackConfirmationDialog() {
        _showBackConfirmationDialog.value = true
    }

    /**
     * Hide back confirmation dialog
     */
    fun hideBackConfirmationDialog() {
        _showBackConfirmationDialog.value = false
    }

    /**
     * Validate word input
     */
    fun isValidWord(word: String): Boolean {
        return word.isNotEmpty() && word.length < 30
    }

    /**
     * Clear word input
     */
    fun clearWordInput() {
        _wordInput.value = ""
    }

    /**
     * Get current word input trimmed
     */
    fun getTrimmedWordInput(): String {
        return _wordInput.value.trim()
    }
}
