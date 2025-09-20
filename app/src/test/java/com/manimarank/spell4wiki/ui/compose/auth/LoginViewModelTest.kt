package com.manimarank.spell4wiki.ui.compose.auth

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        viewModel = LoginViewModel()
    }

    @Test
    fun `initial state should be correct`() = runTest {
        val initialState = viewModel.uiState.first()
        
        assertEquals("", initialState.username)
        assertEquals("", initialState.password)
        assertEquals("", initialState.otpCode)
        assertFalse(initialState.isPasswordVisible)
        assertFalse(initialState.isLoading)
        assertFalse(initialState.isOtpMode)
        assertFalse(initialState.isLoginSuccessful)
        assertNull(initialState.errorMessage)
        assertNull(initialState.loadingMessage)
        assertNull(initialState.otpMessage)
    }

    @Test
    fun `updateUsername should update username in state`() = runTest {
        val testUsername = "testuser"
        
        viewModel.updateUsername(testUsername)
        
        val state = viewModel.uiState.first()
        assertEquals(testUsername, state.username)
    }

    @Test
    fun `updatePassword should update password in state`() = runTest {
        val testPassword = "testpassword"
        
        viewModel.updatePassword(testPassword)
        
        val state = viewModel.uiState.first()
        assertEquals(testPassword, state.password)
    }

    @Test
    fun `updateOtpCode should update OTP code in state`() = runTest {
        val testOtp = "123456"
        
        viewModel.updateOtpCode(testOtp)
        
        val state = viewModel.uiState.first()
        assertEquals(testOtp, state.otpCode)
    }

    @Test
    fun `togglePasswordVisibility should toggle password visibility`() = runTest {
        // Initially false
        var state = viewModel.uiState.first()
        assertFalse(state.isPasswordVisible)
        
        // Toggle to true
        viewModel.togglePasswordVisibility()
        state = viewModel.uiState.first()
        assertTrue(state.isPasswordVisible)
        
        // Toggle back to false
        viewModel.togglePasswordVisibility()
        state = viewModel.uiState.first()
        assertFalse(state.isPasswordVisible)
    }

    @Test
    fun `isFormValid should return false when username is empty`() = runTest {
        viewModel.updateUsername("")
        viewModel.updatePassword("password")
        
        assertFalse(viewModel.isFormValid())
    }

    @Test
    fun `isFormValid should return false when password is empty`() = runTest {
        viewModel.updateUsername("username")
        viewModel.updatePassword("")
        
        assertFalse(viewModel.isFormValid())
    }

    @Test
    fun `isFormValid should return true when both username and password are provided`() = runTest {
        viewModel.updateUsername("username")
        viewModel.updatePassword("password")
        
        assertTrue(viewModel.isFormValid())
    }

    @Test
    fun `state updates should be reflected correctly`() = runTest {
        val username = "testuser"
        val password = "testpass"
        val otp = "123456"
        
        viewModel.updateUsername(username)
        viewModel.updatePassword(password)
        viewModel.updateOtpCode(otp)
        viewModel.togglePasswordVisibility()
        
        val state = viewModel.uiState.first()
        assertEquals(username, state.username)
        assertEquals(password, state.password)
        assertEquals(otp, state.otpCode)
        assertTrue(state.isPasswordVisible)
    }
}
