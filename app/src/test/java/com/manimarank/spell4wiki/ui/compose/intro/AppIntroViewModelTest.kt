package com.manimarank.spell4wiki.ui.compose.intro

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class AppIntroViewModelTest {

    private lateinit var viewModel: AppIntroViewModel

    @Before
    fun setup() {
        viewModel = AppIntroViewModel()
    }

    @Test
    fun `initial state should be correct`() = runTest {
        val initialState = viewModel.uiState.first()
        
        assertEquals(0, initialState.currentPage)
        assertFalse(initialState.isPermissionRequested)
    }

    @Test
    fun `updateCurrentPage should update current page in state`() = runTest {
        val testPage = 2
        
        viewModel.updateCurrentPage(testPage)
        
        val state = viewModel.uiState.first()
        assertEquals(testPage, state.currentPage)
    }

    @Test
    fun `updateCurrentPage should handle different page values`() = runTest {
        // Test page 0
        viewModel.updateCurrentPage(0)
        var state = viewModel.uiState.first()
        assertEquals(0, state.currentPage)
        
        // Test page 1
        viewModel.updateCurrentPage(1)
        state = viewModel.uiState.first()
        assertEquals(1, state.currentPage)
        
        // Test page 4 (last page)
        viewModel.updateCurrentPage(4)
        state = viewModel.uiState.first()
        assertEquals(4, state.currentPage)
    }

    @Test
    fun `setPermissionRequested should update permission requested state`() = runTest {
        // Initially false
        var state = viewModel.uiState.first()
        assertFalse(state.isPermissionRequested)
        
        // Set to true
        viewModel.setPermissionRequested(true)
        state = viewModel.uiState.first()
        assertTrue(state.isPermissionRequested)
        
        // Set back to false
        viewModel.setPermissionRequested(false)
        state = viewModel.uiState.first()
        assertFalse(state.isPermissionRequested)
    }

    @Test
    fun `multiple state updates should work correctly`() = runTest {
        viewModel.updateCurrentPage(3)
        viewModel.setPermissionRequested(true)
        
        val state = viewModel.uiState.first()
        assertEquals(3, state.currentPage)
        assertTrue(state.isPermissionRequested)
    }

    @Test
    fun `state should maintain independence between properties`() = runTest {
        // Update only current page
        viewModel.updateCurrentPage(2)
        var state = viewModel.uiState.first()
        assertEquals(2, state.currentPage)
        assertFalse(state.isPermissionRequested) // Should remain unchanged
        
        // Update only permission requested
        viewModel.setPermissionRequested(true)
        state = viewModel.uiState.first()
        assertEquals(2, state.currentPage) // Should remain unchanged
        assertTrue(state.isPermissionRequested)
    }

    @Test
    fun `negative page values should be handled`() = runTest {
        viewModel.updateCurrentPage(-1)
        
        val state = viewModel.uiState.first()
        assertEquals(-1, state.currentPage) // ViewModel doesn't validate, just stores
    }

    @Test
    fun `large page values should be handled`() = runTest {
        viewModel.updateCurrentPage(100)
        
        val state = viewModel.uiState.first()
        assertEquals(100, state.currentPage) // ViewModel doesn't validate, just stores
    }
}
