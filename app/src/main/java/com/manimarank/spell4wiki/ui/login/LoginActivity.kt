package com.manimarank.spell4wiki.ui.login

import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.apis.ApiClient
import com.manimarank.spell4wiki.data.apis.ApiInterface
import com.manimarank.spell4wiki.data.auth.AccountUtils
import com.manimarank.spell4wiki.data.model.ClientLogin
import com.manimarank.spell4wiki.data.model.WikiLogin
import com.manimarank.spell4wiki.data.model.WikiToken
import com.manimarank.spell4wiki.data.model.WikiUser
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.databinding.ActivityLoginBinding
import com.manimarank.spell4wiki.ui.common.BaseActivity
import com.manimarank.spell4wiki.ui.main.MainActivity
import com.manimarank.spell4wiki.utils.GeneralUtils.hideKeyboard
import com.manimarank.spell4wiki.utils.NetworkUtils.isConnected
import com.manimarank.spell4wiki.utils.SnackBarUtils.showLong
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.utils.constants.Urls
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var pref: PrefManager
    private var api: ApiInterface? = null
    private var isDuringLogin = false
    private var currentUsername: String? = null
    private var currentPassword: String? = null
    private var currentLoginToken: String? = null
    private var isOtpMode = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pref = PrefManager(applicationContext)

        /*
         * Check Already login or not
         * If yes - Open Main screen
         * Else - Ask to login
         */
        if (pref.isLoggedIn || pref.isAnonymous == true) {
            launchActivity()
        } else {
            hideKeyboard(this@LoginActivity)
            api = ApiClient.getCommonsApi(applicationContext).create(ApiInterface::class.java)

            // Hit Login Button
            binding.btnLogin.setOnClickListener {
                if (isOtpMode) {
                    // OTP verification mode
                    if (!TextUtils.isEmpty(binding.editOtp.text)) {
                        if (isConnected(applicationContext)) {
                            hideKeyboard(this@LoginActivity)
                            isDuringLogin = true
                            completeOtpLogin(binding.editOtp.text.toString())
                        } else showMsg(getString(R.string.check_internet))
                    } else showMsg(getString(R.string.invalid_otp))
                } else {
                    // Normal login mode
                    if (!TextUtils.isEmpty(binding.editUsername.text) && !TextUtils.isEmpty(binding.editPassword.text)) {
                        if (isConnected(applicationContext)) {
                            hideKeyboard(this@LoginActivity)
                            // binding.btnLogin.startAnimation() // Temporarily disabled - loading button library issue
                            isDuringLogin = true
                            callToken(binding.editUsername.text.toString(), binding.editPassword.text.toString())
                        } else showMsg(getString(R.string.check_internet))
                    } else showMsg(getString(R.string.invalid_credential))
                }
            }


            // Hit Skip Button
            binding.btnSkipLogin.setOnClickListener {
                if (isDuringLogin()) {
                    showMsg(getString(R.string.please_wait))
                } else {
                    pref.isAnonymous = true
                    launchActivity()
                }
            }

            // Hit Forgot Password Button
            binding.btnForgotPassword.setOnClickListener { openUrl(Urls.FORGOT_PASSWORD, getString(R.string.forgot_password)) }

            // Hit Join Wikipedia Button
            binding.btnJoinWikipedia.setOnClickListener { openUrl(Urls.JOIN_WIKI, getString(R.string.join_wiki)) }
        }
    }

    /**
     * Getting Token from wiki server before login
     *
     * @param username - username of the user
     * @param password - password of the user
     */
    private fun callToken(username: String, password: String) {
        // Store credentials for potential OTP use
        currentUsername = username
        currentPassword = password

        val call = api?.loginToken
        call?.enqueue(object : Callback<WikiToken?> {
            override fun onResponse(call: Call<WikiToken?>, response: Response<WikiToken?>) {
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val lgToken = response.body()?.query?.tokenValue?.loginToken
                        currentLoginToken = lgToken
                        // Once getting login token then call client login api
                        completeLogin(username, password, lgToken)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showErrorMsg(getString(R.string.something_went_wrong))
                    }
                } else {
                    showErrorMsg(getString(R.string.something_went_wrong_try_again))
                }
            }

            override fun onFailure(call: Call<WikiToken?>, t: Throwable) {
                t.printStackTrace()
                showErrorMsg(getString(R.string.something_went_wrong_try_again))
            }
        })
    }

    /**
     * Call client login api after getting login token
     *
     * @param username   - username of the user
     * @param password   - password of the user
     * @param loginToken - Login token
     */
    private fun completeLogin(username: String, password: String, loginToken: String?) {
        val call = api?.clientLogin(username, password, loginToken)
        call?.enqueue(object : Callback<WikiLogin?> {
            override fun onResponse(call: Call<WikiLogin?>, response: Response<WikiLogin?>) {
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val login = response.body()?.clientLogin
                        if (login?.status != null) {
                            when (login.status) {
                                AppConstants.PASS -> {
                                    val extras = intent.extras
                                    val accountAuthenticatorResponse = extras?.getParcelable<AccountAuthenticatorResponse>(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE)
                                    val wikiUser = WikiUser(username, password)
                                    AccountUtils.updateAccount(accountAuthenticatorResponse, wikiUser)
                                    showMsg(String.format(getString(R.string.welcome_user), login.username))
                                    //  Write to shared preferences
                                    pref.setUserSession(login.username)
                                    // binding.btnLogin.doneLoadingAnimation(ContextCompat.getColor(this@LoginActivity, R.color.w_green), BitmapFactory.decodeResource(resources, R.drawable.ic_done)) // Temporarily disabled - loading button library issue
                                    // Move to new activity
                                    Handler().postDelayed({ launchActivity() }, 1500)
                                }
                                "UI" -> {
                                    // OTP/Email verification required
                                    handleOtpRequired(login)
                                }
                                AppConstants.FAIL -> showErrorMsg(login.message)
                                AppConstants.TWO_FACTOR -> {
                                    showErrorMsg(getString(R.string.two_factor_login) + " ${login.message}")
                                    showErrorMsg(getString(R.string.server_misbehaved))
                                }
                                else -> showErrorMsg(getString(R.string.server_misbehaved))
                            }
                        } else showErrorMsg(getString(R.string.something_went_wrong))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showErrorMsg(getString(R.string.something_went_wrong))
                    }
                } else showErrorMsg(getString(R.string.something_went_wrong))
            }

            override fun onFailure(call: Call<WikiLogin?>, t: Throwable) {
                showErrorMsg(getString(R.string.something_went_wrong_try_again))
            }
        })
    }

    /**
     * Handle OTP requirement when status is "UI"
     */
    private fun handleOtpRequired(login: ClientLogin) {
        try {
            // Show OTP UI
            showOtpUI(login.message)
            isDuringLogin = false
        } catch (e: Exception) {
            e.printStackTrace()
            showErrorMsg(getString(R.string.something_went_wrong))
        }
    }

    /**
     * Show OTP input UI and update button text
     */
    private fun showOtpUI(message: String?) {
        isOtpMode = true

        // Show OTP section
        binding.layoutOtpSection.visibility = android.view.View.VISIBLE

        // Update message if provided
        if (!message.isNullOrEmpty()) {
            binding.txtOtpMessage.text = message
        }

        // Update button text
        binding.btnLogin.text = getString(R.string.verify_and_login)

        // Focus on OTP input
        binding.editOtp.requestFocus()

        showMsg(getString(R.string.otp_required))
    }

    /**
     * Complete login with OTP token
     */
    private fun completeOtpLogin(otpToken: String) {
        val call = api?.clientLoginWithOtp(
            currentUsername,
            currentPassword,
            currentLoginToken,
            "1", // logincontinue parameter
            otpToken
        )

        call?.enqueue(object : Callback<WikiLogin?> {
            override fun onResponse(call: Call<WikiLogin?>, response: Response<WikiLogin?>) {
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val login = response.body()?.clientLogin
                        if (login?.status != null) {
                            when (login.status) {
                                AppConstants.PASS -> {
                                    val extras = intent.extras
                                    val accountAuthenticatorResponse = extras?.getParcelable<AccountAuthenticatorResponse>(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE)
                                    val wikiUser = WikiUser(currentUsername!!, currentPassword!!)
                                    AccountUtils.updateAccount(accountAuthenticatorResponse, wikiUser)
                                    showMsg(String.format(getString(R.string.welcome_user), login.username))
                                    //  Write to shared preferences
                                    pref.setUserSession(login.username)
                                    // Move to new activity
                                    Handler().postDelayed({ launchActivity() }, 1500)
                                }
                                AppConstants.FAIL -> {
                                    showErrorMsg(login.message ?: getString(R.string.invalid_otp))
                                    // Reset OTP field for retry
                                    binding.editOtp.text?.clear()
                                }
                                else -> {
                                    showErrorMsg(getString(R.string.server_misbehaved))
                                    resetToNormalLogin()
                                }
                            }
                        } else {
                            showErrorMsg(getString(R.string.something_went_wrong))
                            resetToNormalLogin()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showErrorMsg(getString(R.string.something_went_wrong))
                        resetToNormalLogin()
                    }
                } else {
                    showErrorMsg(getString(R.string.something_went_wrong))
                    resetToNormalLogin()
                }
            }

            override fun onFailure(call: Call<WikiLogin?>, t: Throwable) {
                showErrorMsg(getString(R.string.something_went_wrong_try_again))
                resetToNormalLogin()
            }
        })
    }

    /**
     * Reset UI back to normal login mode
     */
    private fun resetToNormalLogin() {
        isOtpMode = false
        binding.layoutOtpSection.visibility = android.view.View.GONE
        binding.btnLogin.text = getString(R.string.login)
        binding.editOtp.text?.clear()
        currentUsername = null
        currentPassword = null
        currentLoginToken = null
    }

    /**
     * Launch activity
     */
    private fun launchActivity() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showErrorMsg(msg: String?) {
        if (isConnected(applicationContext)) showMsg(msg) else showMsg(getString(R.string.check_internet))
        // binding.btnLogin.revertAnimation() // Temporarily disabled - loading button library issue
        isDuringLogin = false
    }

    private fun showMsg(msg: String?) {
        showLong(binding.btnLogin, msg ?: "")
    }

    override fun onDestroy() {
        super.onDestroy()
        // binding.btnLogin.revertAnimation() // Temporarily disabled - loading button library issue
        // binding.btnLogin.dispose() // Temporarily disabled - loading button library issue
    }

    private fun openUrl(url: String, title: String) {
        if (isDuringLogin()) {
            showMsg(getString(R.string.please_wait))
        } else {
            if (isConnected(applicationContext)) com.manimarank.spell4wiki.utils.GeneralUtils.openUrl(this@LoginActivity, url, title) else showMsg(getString(R.string.check_internet))
        }
    }

    private fun isDuringLogin(): Boolean {
        return isDuringLogin // Temporarily simplified - loading button library issue
        // return binding.btnLogin.isAnimating || isDuringLogin
    }
}