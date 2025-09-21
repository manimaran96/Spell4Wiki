package com.manimarank.spell4wiki.ui.compose.spell4wordlist

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
 * ViewModel for Spell4WordListComposeActivity
 * Manages word list state, file content, language selection, and recording validation
 */
class Spell4WordListViewModel : ViewModel() {

    private val _currentMode = MutableStateFlow(Spell4WordListMode.SELECT)
    val currentMode: StateFlow<Spell4WordListMode> = _currentMode.asStateFlow()

    private val _fileContent = MutableStateFlow("")
    val fileContent: StateFlow<String> = _fileContent.asStateFlow()

    private val _wordList = MutableStateFlow<List<String>>(emptyList())
    val wordList: StateFlow<List<String>> = _wordList.asStateFlow()

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
     * Switch to select mode
     */
    fun switchToSelectMode() {
        _currentMode.value = Spell4WordListMode.SELECT
        _fileContent.value = ""
        _wordList.value = emptyList()
    }

    /**
     * Switch to edit mode
     */
    fun switchToEditMode() {
        _currentMode.value = Spell4WordListMode.EDIT
    }

    /**
     * Switch to list mode
     */
    fun switchToListMode() {
        _currentMode.value = Spell4WordListMode.LIST
    }

    /**
     * Switch to empty state mode
     */
    fun switchToEmptyMode() {
        _currentMode.value = Spell4WordListMode.EMPTY
    }

    /**
     * Set file content from file selection
     */
    fun setFileContent(content: String) {
        _fileContent.value = content
    }

    /**
     * Update file content from user input
     */
    fun updateFileContent(content: String) {
        _fileContent.value = content
    }

    /**
     * Process word list from file content
     */
    fun processWordList() {
        val content = _fileContent.value.trim()
        if (content.isEmpty()) {
            switchToEmptyMode()
            return
        }

        val words = getWordListFromString(content)
        if (words.isEmpty()) {
            switchToEmptyMode()
        } else {
            _wordList.value = words
            switchToListMode()
        }
    }

    /**
     * Extract word list from string content
     */
    private fun getWordListFromString(content: String): List<String> {
        return content.split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() && it.length <= 30 }
            .distinct()
    }

    /**
     * Check if recording is allowed for the given word
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
     * Check if there's any content that would be lost on back press
     */
    fun hasContent(): Boolean {
        return _fileContent.value.isNotEmpty() || _wordList.value.isNotEmpty()
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
     * Clear all content
     */
    fun clearContent() {
        _fileContent.value = ""
        _wordList.value = emptyList()
        switchToSelectMode()
    }

    /**
     * Get current word count
     */
    fun getWordCount(): Int {
        return _wordList.value.size
    }

    /**
     * Get filtered word count (words without audio)
     */
    fun getFilteredWordCount(): Int {
        return _wordList.value.count { word ->
            isAllowRecord(word)
        }
    }
}
