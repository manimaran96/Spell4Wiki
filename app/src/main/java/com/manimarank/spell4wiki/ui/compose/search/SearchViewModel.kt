package com.manimarank.spell4wiki.ui.compose.search

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manimarank.spell4wiki.data.apis.ApiClient
import com.manimarank.spell4wiki.data.apis.ApiInterface
import com.manimarank.spell4wiki.data.model.WikiSearchWords
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.ui.compose.webview.CommonWebContentComposeActivity
import com.manimarank.spell4wiki.utils.NetworkUtils.isConnected
import com.manimarank.spell4wiki.utils.constants.AppConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class SearchResult(
    val title: String,
    val description: String,
    val url: String,
    val id: String = ""
)

class SearchViewModel : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var nextOffset: Int? = null
    private var currentQuery: String = ""
    private var searchJob: Job? = null
    private var context: Context? = null

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query

        // Cancel previous search job
        searchJob?.cancel()

        // Start new search with debouncing
        if (query.isNotBlank()) {
            searchJob = viewModelScope.launch {
                delay(400) // 400ms debounce delay
                performSearchInternal(query, context)
            }
        } else {
            // Clear results if query is empty
            _searchResults.value = emptyList()
            _isLoading.value = false
        }
    }

    fun setContext(context: Context) {
        this.context = context
    }

    fun performSearch(query: String, context: Context, loadMore: Boolean = false) {
        performSearchInternal(query, context, loadMore)
    }

    private fun performSearchInternal(query: String, context: Context? = null, loadMore: Boolean = false) {
        if (query.isBlank()) return

        val ctx = context ?: return

        if (!isConnected(ctx)) {
            _errorMessage.value = "No internet connection"
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                if (!loadMore) {
                    _searchResults.value = emptyList()
                    nextOffset = null
                    currentQuery = query
                }

                val pref = PrefManager(ctx)
                val wikiLangCode = pref.languageCodeSpell4WikiAll ?: "en"

                searchWiktionary(query, wikiLangCode, nextOffset ?: 0, ctx)

            } catch (e: Exception) {
                _errorMessage.value = e.message
                _isLoading.value = false
            }
        }
    }

    private fun searchWiktionary(query: String, langCode: String, offset: Int, context: Context) {
        val apiInterface = ApiClient.getWiktionaryApi(context, langCode).create(ApiInterface::class.java)
        val call = apiInterface.fetchRecords(query, offset)

        call.enqueue(object : Callback<WikiSearchWords?> {
            override fun onResponse(call: Call<WikiSearchWords?>, response: Response<WikiSearchWords?>) {
                _isLoading.value = false
                
                if (response.isSuccessful) {
                    val searchResponse = response.body()
                    val searchData = searchResponse?.query?.wikiTitleList ?: emptyList()

                    val results = searchData.map { item ->
                        SearchResult(
                            title = item.title ?: "",
                            description = "", // WikiWord doesn't have snippet/description
                            url = "https://$langCode.wiktionary.org/wiki/${item.title}",
                            id = item.pageId?.toString() ?: ""
                        )
                    }

                    if (offset == 0) {
                        _searchResults.value = results
                    } else {
                        _searchResults.value = _searchResults.value + results
                    }

                    // Update offset for pagination
                    nextOffset = if (results.isNotEmpty()) offset + results.size else null
                    
                } else {
                    _errorMessage.value = "Search failed: ${response.message()}"
                }
            }

            override fun onFailure(call: Call<WikiSearchWords?>, t: Throwable) {
                _isLoading.value = false
                _errorMessage.value = "Network error: ${t.message}"
            }
        })
    }

    fun loadMoreResults(context: Context) {
        if (currentQuery.isNotEmpty() && nextOffset != null && !_isLoading.value) {
            performSearch(currentQuery, context, loadMore = true)
        }
    }

    fun openSearchResult(result: SearchResult, context: Context) {
        val intent = Intent(context, CommonWebContentComposeActivity::class.java)
        intent.putExtra(AppConstants.TITLE, result.title)
        intent.putExtra(AppConstants.URL, result.url)
        context.startActivity(intent)
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
