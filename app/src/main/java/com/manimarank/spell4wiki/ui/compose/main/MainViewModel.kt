package com.manimarank.spell4wiki.ui.compose.main

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.ui.compose.search.SearchActivity

import com.manimarank.spell4wiki.utils.GeneralUtils.openUrl
import com.manimarank.spell4wiki.utils.NetworkUtils.isConnected
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.utils.constants.Urls
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _showLogoutDialog = MutableStateFlow(false)
    val showLogoutDialog: StateFlow<Boolean> = _showLogoutDialog.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun performSearch(query: String, context: Context) {
        if (query.isBlank()) return
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                // Launch search activity with query
                val intent = Intent(context, SearchActivity::class.java)
                intent.putExtra(AppConstants.SEARCH_TEXT, query)
                context.startActivity(intent)
                
                // Clear search query after a short delay
                kotlinx.coroutines.delay(100)
                _searchQuery.value = ""
                
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun showLogoutDialog(context: Context) {
        if (isConnected(context)) {
            _showLogoutDialog.value = true
        } else {
            _errorMessage.value = context.getString(R.string.check_internet)
        }
    }

    fun hideLogoutDialog() {
        _showLogoutDialog.value = false
    }

    fun confirmLogout(pref: PrefManager) {
        logoutApi()
        pref.logoutUser()
        hideLogoutDialog()
    }

    fun openContributionUrl(context: Context, pref: PrefManager) {
        if (isConnected(context)) {
            val urlMyContribution = String.format(Urls.COMMONS_CONTRIBUTION, pref.name)
            openUrl(context, urlMyContribution, context.getString(R.string.view_my_contribution))
        } else {
            _errorMessage.value = context.getString(R.string.check_internet)
        }
    }

    private fun logoutApi() {
        // TODO: Implement logout API call if needed
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
