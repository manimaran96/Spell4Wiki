package com.manimarank.spell4wiki.ui.compose.auth

import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.apis.ApiClient
import com.manimarank.spell4wiki.data.apis.ApiInterface
import com.manimarank.spell4wiki.data.auth.AccountUtils
import com.manimarank.spell4wiki.data.model.ClientLogin
import com.manimarank.spell4wiki.data.model.WikiLogin
import com.manimarank.spell4wiki.data.model.WikiToken
import com.manimarank.spell4wiki.data.model.WikiUser
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.utils.NetworkUtils.isConnected
import com.manimarank.spell4wiki.utils.constants.AppConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val otpCode: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isOtpMode: Boolean = false,
    val isLoginSuccessful: Boolean = false,
    val errorMessage: String? = null,
    val loadingMessage: String? = null,
    val otpMessage: String? = null
)

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private var api: ApiInterface? = null
    private var currentLoginToken: String? = null

    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun updateOtpCode(otpCode: String) {
        _uiState.value = _uiState.value.copy(otpCode = otpCode)
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
    }

    fun isFormValid(): Boolean {
        val state = _uiState.value
        return if (state.isOtpMode) {
            state.otpCode.isNotBlank()
        } else {
            state.username.isNotBlank() && state.password.isNotBlank()
        }
    }

    fun login(context: Context) {
        if (!isConnected(context)) {
            showError(context.getString(R.string.check_internet))
            return
        }

        if (api == null) {
            api = ApiClient.getCommonsApi(context).create(ApiInterface::class.java)
        }

        setLoadingState(true, context.getString(R.string.logging_in))
        callToken(context, _uiState.value.username, _uiState.value.password)
    }

    fun completeOtpLogin(context: Context) {
        if (!isConnected(context)) {
            showError(context.getString(R.string.check_internet))
            return
        }

        if (_uiState.value.otpCode.isBlank()) {
            showError(context.getString(R.string.invalid_otp))
            return
        }

        setLoadingState(true, context.getString(R.string.verifying_code))
        
        val call = api?.clientLoginWithOtp(
            _uiState.value.username,
            _uiState.value.password,
            currentLoginToken,
            "1", // logincontinue parameter
            _uiState.value.otpCode
        )

        call?.enqueue(object : Callback<WikiLogin?> {
            override fun onResponse(call: Call<WikiLogin?>, response: Response<WikiLogin?>) {
                handleLoginResponse(context, response)
            }

            override fun onFailure(call: Call<WikiLogin?>, t: Throwable) {
                showError(context.getString(R.string.something_went_wrong_try_again))
                resetToNormalLogin()
            }
        })
    }

    private fun callToken(context: Context, username: String, password: String) {
        val call = api?.loginToken
        call?.enqueue(object : Callback<WikiToken?> {
            override fun onResponse(call: Call<WikiToken?>, response: Response<WikiToken?>) {
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val lgToken = response.body()?.query?.tokenValue?.loginToken
                        currentLoginToken = lgToken
                        completeLogin(context, username, password, lgToken)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showError(context.getString(R.string.something_went_wrong))
                    }
                } else {
                    showError(context.getString(R.string.something_went_wrong_try_again))
                }
            }

            override fun onFailure(call: Call<WikiToken?>, t: Throwable) {
                t.printStackTrace()
                showError(context.getString(R.string.something_went_wrong_try_again))
            }
        })
    }

    private fun completeLogin(context: Context, username: String, password: String, loginToken: String?) {
        val call = api?.clientLogin(username, password, loginToken)
        call?.enqueue(object : Callback<WikiLogin?> {
            override fun onResponse(call: Call<WikiLogin?>, response: Response<WikiLogin?>) {
                handleLoginResponse(context, response)
            }

            override fun onFailure(call: Call<WikiLogin?>, t: Throwable) {
                showError(context.getString(R.string.something_went_wrong_try_again))
            }
        })
    }

    private fun handleLoginResponse(context: Context, response: Response<WikiLogin?>) {
        if (response.isSuccessful && response.body() != null) {
            try {
                val login = response.body()?.clientLogin
                if (login?.status != null) {
                    when (login.status) {
                        AppConstants.PASS -> {
                            handleLoginSuccess(context, login)
                        }
                        AppConstants.OTP_OR_TWO_FACTOR -> {
                            handleOtpRequired(login)
                        }
                        AppConstants.FAIL -> {
                            showError(login.message ?: context.getString(R.string.invalid_credential))
                            if (_uiState.value.isOtpMode) {
                                // Clear OTP field for retry
                                _uiState.value = _uiState.value.copy(otpCode = "")
                            }
                        }
                        else -> {
                            showError(context.getString(R.string.server_misbehaved))
                            if (_uiState.value.isOtpMode) {
                                resetToNormalLogin()
                            }
                        }
                    }
                } else {
                    showError(context.getString(R.string.something_went_wrong))
                    if (_uiState.value.isOtpMode) {
                        resetToNormalLogin()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showError(context.getString(R.string.something_went_wrong))
                if (_uiState.value.isOtpMode) {
                    resetToNormalLogin()
                }
            }
        } else {
            showError(context.getString(R.string.something_went_wrong))
            if (_uiState.value.isOtpMode) {
                resetToNormalLogin()
            }
        }
    }

    private fun handleLoginSuccess(context: Context, login: ClientLogin) {
        viewModelScope.launch {
            try {
                // Handle account authentication response if present
                // This would need to be passed from the activity if needed
                val wikiUser = WikiUser(_uiState.value.username, _uiState.value.password)
                // AccountUtils.updateAccount(accountAuthenticatorResponse, wikiUser)
                
                // Save user session
                val pref = PrefManager(context)
                pref.setUserSession(login.username)
                
                // Show success message and mark login as successful
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoginSuccessful = true,
                    errorMessage = null
                )
            } catch (e: Exception) {
                e.printStackTrace()
                showError(context.getString(R.string.something_went_wrong))
            }
        }
    }

    private fun handleOtpRequired(login: ClientLogin) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isOtpMode = true,
            otpMessage = login.message,
            errorMessage = null
        )
    }

    private fun resetToNormalLogin() {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isOtpMode = false,
            otpCode = "",
            otpMessage = null,
            errorMessage = null
        )
        currentLoginToken = null
    }

    private fun setLoadingState(isLoading: Boolean, loadingMessage: String? = null) {
        _uiState.value = _uiState.value.copy(
            isLoading = isLoading,
            loadingMessage = loadingMessage,
            errorMessage = null
        )
    }

    private fun showError(message: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = message
        )
    }
}
