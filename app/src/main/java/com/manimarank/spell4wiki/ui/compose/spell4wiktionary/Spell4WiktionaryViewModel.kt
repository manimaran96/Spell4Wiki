package com.manimarank.spell4wiki.ui.compose.spell4wiktionary

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.apis.ApiClient.getWiktionaryApi
import com.manimarank.spell4wiki.data.apis.ApiInterface
import com.manimarank.spell4wiki.data.db.DBHelper
import com.manimarank.spell4wiki.data.db.dao.WikiLangDao
import com.manimarank.spell4wiki.data.db.dao.WordsHaveAudioDao
import com.manimarank.spell4wiki.data.db.entities.WordsHaveAudio
import com.manimarank.spell4wiki.data.model.WikiWordsWithoutAudio
import com.manimarank.spell4wiki.data.prefs.AppPref
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.ui.compose.dialogs.DialogMigrationUtils.showRunFilterInfoDialog
import com.manimarank.spell4wiki.utils.GeneralUtils
import com.manimarank.spell4wiki.utils.NetworkUtils
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.utils.constants.Urls
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.HttpURLConnection
import java.net.URL

/**
 * ViewModel for Spell4Wiktionary Compose screen
 * Manages state and business logic for the Wiktionary words without audio feature
 */
class Spell4WiktionaryViewModel : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(Spell4WiktionaryUiState())
    val uiState: StateFlow<Spell4WiktionaryUiState> = _uiState.asStateFlow()

    // Words list
    private val _words = MutableStateFlow<List<String>>(emptyList())
    val words: StateFlow<List<String>> = _words.asStateFlow()

    // Loading states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Dialog states
    private val _showBackConfirmationDialog = MutableStateFlow(false)
    val showBackConfirmationDialog: StateFlow<Boolean> = _showBackConfirmationDialog.asStateFlow()

    private val _showFilterDialog = MutableStateFlow(false)
    val showFilterDialog: StateFlow<Boolean> = _showFilterDialog.asStateFlow()

    // Filter progress
    private val _filterProgress = MutableStateFlow(0)
    val filterProgress: StateFlow<Int> = _filterProgress.asStateFlow()

    private val _filterTotal = MutableStateFlow(0)
    val filterTotal: StateFlow<Int> = _filterTotal.asStateFlow()

    // Private properties
    private var context: Context? = null
    private var wikiLangDao: WikiLangDao? = null
    private var wordsHaveAudioDao: WordsHaveAudioDao? = null
    private var wordsListAlreadyHaveAudio: MutableList<String> = mutableListOf()
    private var nextOffsetObj: String? = null
    private var languageCode: String? = null
    private var wiktionaryTitleOfWordsWithoutAudio: String? = null
    private var filterJob: Job? = null
    private var isFilterCancelled = false

    /**
     * Initialize the ViewModel with context and language code
     */
    fun initialize(context: Context, langCode: String?) {
        this.context = context
        this.languageCode = langCode

        val dbHelper = DBHelper.getInstance(context)
        wikiLangDao = dbHelper.appDatabase.wikiLangDao
        wordsHaveAudioDao = dbHelper.appDatabase.wordsHaveAudioDao

        // Load already recorded words
        loadWordsAlreadyHaveAudio()

        // Update language info
        updateLanguageInfo()

        // Load initial data
        loadWords()
    }

    /**
     * Load words from server
     */
    fun loadWords() {
        if (_isLoading.value || context == null) return

        _isLoading.value = true

        context?.let { ctx ->
            if (NetworkUtils.isConnected(ctx)) {
                val api = getWiktionaryApi(ctx, languageCode ?: AppConstants.DEFAULT_LANGUAGE_CODE)
                    .create(ApiInterface::class.java)

            val call = api.fetchUnAudioRecords(
                wiktionaryTitleOfWordsWithoutAudio ?: "null",
                nextOffsetObj,
                AppPref.getFetchLimit(),
                AppPref.getFetchBy(),
                AppPref.getFetchDir()
            )

            call.enqueue(object : Callback<WikiWordsWithoutAudio?> {
                override fun onResponse(
                    call: Call<WikiWordsWithoutAudio?>,
                    response: Response<WikiWordsWithoutAudio?>
                ) {
                    _isLoading.value = false
                    _isRefreshing.value = false

                    if (response.isSuccessful && response.body() != null) {
                        response.body()?.let { body ->
                            processWordsResponse(body)
                        }
                    } else {
                        // Handle error
                        updateErrorState("Failed to load words")
                    }
                }

                override fun onFailure(call: Call<WikiWordsWithoutAudio?>, t: Throwable) {
                    _isLoading.value = false
                    _isRefreshing.value = false
                    updateErrorState("Network error: ${t.message}")
                }
            })
            } else {
                _isLoading.value = false
                updateErrorState("No internet connection")
            }
        }
    }

    /**
     * Load more words for infinite scrolling
     */
    fun loadMoreWords() {
        if (hasMoreData() && !_isLoading.value) {
            loadWords()
        }
    }

    /**
     * Refresh words list
     */
    fun refreshWords() {
        _isRefreshing.value = true
        nextOffsetObj = null
        _words.value = emptyList()
        loadWords()
    }

    /**
     * Update language and reload data
     */
    fun updateLanguage(context: Context, langCode: String?) {
        this.languageCode = langCode
        updateLanguageInfo()
        loadWordsAlreadyHaveAudio()
        refreshWords()
    }

    /**
     * Update selected category
     */
    fun updateCategory(category: String?) {
        wiktionaryTitleOfWordsWithoutAudio = category
        _uiState.value = _uiState.value.copy(
            selectedCategory = category ?: "",
            categoryInfo = if (category != null) "Category: $category" else ""
        )
        refreshWords()
    }

    /**
     * Show filter dialog
     */
    fun showFilterDialog() {
        _showFilterDialog.value = true
        startFilter()
    }

    /**
     * Hide filter dialog
     */
    fun hideFilterDialog() {
        _showFilterDialog.value = false
        cancelFilter()
    }

    /**
     * Show filter info dialog
     */
    fun showFilterInfo(context: Context) {
        if (context is androidx.fragment.app.FragmentActivity) {
            context.showRunFilterInfoDialog()
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
     * Check if there's an active filter
     */
    fun hasActiveFilter(): Boolean {
        return filterJob?.isActive == true
    }

    /**
     * Check if there's more data to load
     */
    fun hasMoreData(): Boolean {
        return nextOffsetObj != null
    }

    /**
     * Update word list after recording
     */
    fun updateWordList(word: String) {
        context?.let { ctx ->
            wordsHaveAudioDao?.insert(WordsHaveAudio(word, languageCode))
            wordsListAlreadyHaveAudio.add(word)
            
            // Remove word from current list
            val currentWords = _words.value.toMutableList()
            currentWords.remove(word)
            _words.value = currentWords
        }
    }

    private fun loadWordsAlreadyHaveAudio() {
        languageCode?.let { langCode ->
            wordsListAlreadyHaveAudio = wordsHaveAudioDao
                ?.getWordsAlreadyHaveAudioByLanguage(langCode)
                ?.toMutableList() ?: mutableListOf()
        }
    }

    private fun updateLanguageInfo() {
        val wikiLang = wikiLangDao?.getWikiLanguageWithCode(languageCode)
        val languageInfo = if (wikiLang != null) {
            // Initialize the wiktionary title for words without audio
            wiktionaryTitleOfWordsWithoutAudio = wikiLang.titleOfWordsWithoutAudio

            context?.let { ctx ->
                GeneralUtils.getLanguageInfo(ctx, wikiLang, R.string.language)
            } ?: ""
        } else ""

        _uiState.value = _uiState.value.copy(languageInfo = languageInfo)
    }

    private fun processWordsResponse(response: WikiWordsWithoutAudio) {
        val titleList = response.query?.wikiTitleList?.mapNotNull { it.title }?.toMutableList() ?: mutableListOf()
        nextOffsetObj = response.offset?.nextOffset

        if (titleList.isNotEmpty()) {
            // Remove already recorded words
            titleList.removeAll(wordsListAlreadyHaveAudio.toSet())

            if (titleList.isNotEmpty()) {
                val currentWords = _words.value.toMutableList()
                titleList.forEach { word ->
                    if (!currentWords.contains(word)) {
                        currentWords.add(word)
                    }
                }
                _words.value = currentWords
            }
        }
    }

    private fun updateErrorState(message: String) {
        // Handle error state - could show snackbar or error dialog
        // For now, just log the error
        println("Spell4Wiktionary Error: $message")
    }

    private fun startFilter() {
        val currentWords = _words.value
        val pref = context?.let { PrefManager(it) }
        val wordsToCheck = currentWords.take(pref?.runFilterNumberOfWordsToCheck ?: AppConstants.RUN_FILTER_NO_OF_WORDS_CHECK_COUNT)

        if (wordsToCheck.isEmpty() || languageCode == null) return

        _filterTotal.value = wordsToCheck.size
        _filterProgress.value = 0
        isFilterCancelled = false

        filterJob = viewModelScope.launch {
            val filteredWords = mutableListOf<String>()
            
            wordsToCheck.forEachIndexed { index, word ->
                if (isFilterCancelled) return@launch
                
                _filterProgress.value = index + 1
                
                val hasAudio = withContext(Dispatchers.IO) {
                    checkIfWordHasAudio(word)
                }
                
                if (!hasAudio) {
                    filteredWords.add(word)
                } else {
                    // Add to already have audio list
                    wordsListAlreadyHaveAudio.add(word)
                    wordsHaveAudioDao?.insert(WordsHaveAudio(word, languageCode))
                }
            }
            
            if (!isFilterCancelled) {
                // Update words list with filtered results
                val currentWords = _words.value.toMutableList()
                currentWords.removeAll(wordsToCheck.toSet())
                currentWords.addAll(0, filteredWords)
                _words.value = currentWords
                
                _showFilterDialog.value = false
            }
        }
    }

    private fun cancelFilter() {
        isFilterCancelled = true
        filterJob?.cancel()
    }

    private suspend fun checkIfWordHasAudio(word: String): Boolean {
        return try {
            val url = String.format(Urls.AUDIO_FILE_IN_COMMONS, languageCode, word)
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.connect()
            val hasAudio = connection.responseCode == 200
            connection.disconnect()
            hasAudio
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * UI State for Spell4Wiktionary screen
 */
data class Spell4WiktionaryUiState(
    val languageInfo: String = "",
    val selectedCategory: String = "",
    val categoryInfo: String = "",
    val errorMessage: String? = null
)
