package com.manimarank.spell4wiki.ui.compose.intro

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppIntroUiState(
    val currentPage: Int = 0,
    val isPermissionRequested: Boolean = false
)

class AppIntroViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AppIntroUiState())
    val uiState: StateFlow<AppIntroUiState> = _uiState.asStateFlow()

    fun updateCurrentPage(page: Int) {
        _uiState.value = _uiState.value.copy(currentPage = page)
    }

    fun setPermissionRequested(requested: Boolean) {
        _uiState.value = _uiState.value.copy(isPermissionRequested = requested)
    }
}
