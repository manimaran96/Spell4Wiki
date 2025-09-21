package com.manimarank.spell4wiki.ui.compose.main

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.ui.compose.search.SearchComposeActivity
import com.manimarank.spell4wiki.ui.dialogs.styleDialogButtons
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
                val intent = Intent(context, SearchComposeActivity::class.java)
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

    fun showLogoutDialog(context: Context, pref: PrefManager) {
        if (isConnected(context)) {
            val dialog = AlertDialog.Builder(context, R.style.AlertDialogTheme)
                .setTitle(R.string.logout_confirmation)
                .setMessage(R.string.logout_message)
                .setPositiveButton(context.getString(R.string.yes)) { _: DialogInterface?, _: Int ->
                    // Logout user
                    logoutApi()
                    pref.logoutUser()
                }
                .setNegativeButton(R.string.no, null)
                .create()
            dialog.show()

            // Apply consistent button styling
            dialog.styleDialogButtons(context)
        } else {
            _errorMessage.value = context.getString(R.string.check_internet)
        }
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
