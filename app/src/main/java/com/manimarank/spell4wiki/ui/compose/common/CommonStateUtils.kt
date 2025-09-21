package com.manimarank.spell4wiki.ui.compose.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.StateFlow

/**
 * Common state management utilities for Compose screens
 * Provides reusable patterns for handling loading, error, and data states
 */

/**
 * Common UI state for screens with loading, error, and data states
 */
data class CommonUIState<T>(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val data: T? = null,
    val error: String? = null,
    val isEmpty: Boolean = false
) {
    val hasError: Boolean get() = error != null
    val hasData: Boolean get() = data != null && !isEmpty
    val showEmpty: Boolean get() = !isLoading && !hasError && (data == null || isEmpty)
}

/**
 * Extension function to create a loading state
 */
fun <T> CommonUIState<T>.loading(): CommonUIState<T> = copy(
    isLoading = true,
    error = null
)

/**
 * Extension function to create a refreshing state
 */
fun <T> CommonUIState<T>.refreshing(): CommonUIState<T> = copy(
    isRefreshing = true,
    error = null
)

/**
 * Extension function to create a success state
 */
fun <T> CommonUIState<T>.success(data: T, isEmpty: Boolean = false): CommonUIState<T> = copy(
    isLoading = false,
    isRefreshing = false,
    data = data,
    error = null,
    isEmpty = isEmpty
)

/**
 * Extension function to create an error state
 */
fun <T> CommonUIState<T>.error(message: String): CommonUIState<T> = copy(
    isLoading = false,
    isRefreshing = false,
    error = message
)

/**
 * Composable function to handle common state rendering
 */
@Composable
fun <T> HandleCommonState(
    state: CommonUIState<T>,
    onRetry: () -> Unit,
    onRefresh: () -> Unit = onRetry,
    loadingContent: @Composable () -> Unit = { LoadingState() },
    errorContent: @Composable (String) -> Unit = { error ->
        ErrorState(
            errorText = error,
            onRetry = onRetry
        )
    },
    emptyContent: @Composable () -> Unit = {
        EmptyState(
            onAction = onRefresh
        )
    },
    content: @Composable (T) -> Unit
) {
    when {
        state.isLoading && !state.isRefreshing -> {
            loadingContent()
        }
        state.hasError -> {
            errorContent(state.error!!)
        }
        state.showEmpty -> {
            emptyContent()
        }
        state.hasData -> {
            content(state.data!!)
        }
    }
}

/**
 * Common pattern for handling error messages with snackbar/toast
 */
@Composable
fun HandleErrorMessage(
    errorMessage: String?,
    onErrorShown: () -> Unit,
    showAsSnackbar: Boolean = true
) {
    val context = LocalContext.current
    
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            if (showAsSnackbar) {
                // TODO: Implement snackbar showing logic
                // For now, we'll just clear the error
                onErrorShown()
            } else {
                // TODO: Implement toast showing logic
                onErrorShown()
            }
        }
    }
}

/**
 * Common pattern for handling success messages
 */
@Composable
fun HandleSuccessMessage(
    successMessage: String?,
    onSuccessShown: () -> Unit,
    showAsSnackbar: Boolean = true
) {
    val context = LocalContext.current
    
    LaunchedEffect(successMessage) {
        successMessage?.let { message ->
            if (showAsSnackbar) {
                // TODO: Implement snackbar showing logic
                onSuccessShown()
            } else {
                // TODO: Implement toast showing logic
                onSuccessShown()
            }
        }
    }
}

/**
 * Common pattern for collecting StateFlow and handling initial loading
 */
@Composable
fun <T> rememberStateFlowWithLoading(
    stateFlow: StateFlow<T>,
    onInitialLoad: () -> Unit
): T {
    val state by stateFlow.collectAsState()
    
    LaunchedEffect(Unit) {
        onInitialLoad()
    }
    
    return state
}

/**
 * Common pattern for handling pull-to-refresh
 * Note: Implementation depends on available pull-to-refresh library
 */
// TODO: Implement pull-to-refresh when library is available

/**
 * Common validation state for forms
 */
data class ValidationState(
    val isValid: Boolean = true,
    val errorMessage: String? = null
) {
    val hasError: Boolean get() = errorMessage != null
}

/**
 * Common form state for input validation
 */
data class FormState(
    val isSubmitting: Boolean = false,
    val hasErrors: Boolean = false,
    val submitError: String? = null,
    val isValid: Boolean = true
) {
    val canSubmit: Boolean get() = isValid && !isSubmitting && !hasErrors
}

/**
 * Extension function to validate form state
 */
fun FormState.submitting(): FormState = copy(
    isSubmitting = true,
    submitError = null
)

/**
 * Extension function to handle form success
 */
fun FormState.success(): FormState = copy(
    isSubmitting = false,
    submitError = null
)

/**
 * Extension function to handle form error
 */
fun FormState.error(message: String): FormState = copy(
    isSubmitting = false,
    submitError = message
)

/**
 * Common pattern for handling form validation
 */
@Composable
fun rememberFormValidation(
    vararg validationRules: () -> ValidationState
): ValidationState {
    var validationState by remember { mutableStateOf(ValidationState()) }
    
    LaunchedEffect(validationRules) {
        val errors = validationRules.mapNotNull { rule ->
            val result = rule()
            if (result.hasError) result.errorMessage else null
        }
        
        validationState = if (errors.isEmpty()) {
            ValidationState(isValid = true)
        } else {
            ValidationState(isValid = false, errorMessage = errors.first())
        }
    }
    
    return validationState
}

/**
 * Common search state for search functionality
 */
data class SearchState(
    val query: String = "",
    val isSearching: Boolean = false,
    val hasResults: Boolean = false,
    val noResultsMessage: String? = null
) {
    val showNoResults: Boolean get() = !isSearching && !hasResults && query.isNotEmpty()
}

/**
 * Extension function to start searching
 */
fun SearchState.searching(query: String): SearchState = copy(
    query = query,
    isSearching = true,
    noResultsMessage = null
)

/**
 * Extension function to show search results
 */
fun SearchState.results(hasResults: Boolean, noResultsMessage: String? = null): SearchState = copy(
    isSearching = false,
    hasResults = hasResults,
    noResultsMessage = if (!hasResults) noResultsMessage else null
)

/**
 * Extension function to clear search
 */
fun SearchState.clear(): SearchState = SearchState()
