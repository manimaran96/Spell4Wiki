package com.manimarank.spell4wiki.ui.login

import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import androidx.core.content.ContextCompat
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.apis.ApiClient
import com.manimarank.spell4wiki.data.apis.ApiInterface
import com.manimarank.spell4wiki.data.auth.AccountUtils
import com.manimarank.spell4wiki.data.model.WikiLogin
import com.manimarank.spell4wiki.data.model.WikiToken
import com.manimarank.spell4wiki.data.model.WikiUser
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.ui.common.BaseActivity
import com.manimarank.spell4wiki.ui.main.MainActivity
import com.manimarank.spell4wiki.utils.GeneralUtils.hideKeyboard
import com.manimarank.spell4wiki.utils.GeneralUtils.openUrl
import com.manimarank.spell4wiki.utils.NetworkUtils.isConnected
import com.manimarank.spell4wiki.utils.SnackBarUtils.showLong
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.utils.constants.Urls
import com.manimarank.spell4wiki.databinding.ActivityLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var pref: PrefManager
    private var api: ApiInterface? = null
    private var isDuringLogin = false
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
                if (!TextUtils.isEmpty(binding.editUsername.text) && !TextUtils.isEmpty(binding.editPassword.text)) {
                    if (isConnected(applicationContext)) {
                        hideKeyboard(this@LoginActivity)
                        // binding.btnLogin.startAnimation() // Temporarily disabled - loading button library issue
                        isDuringLogin = true
                        callToken(binding.editUsername.text.toString(), binding.editPassword.text.toString())
                    } else showMsg(getString(R.string.check_internet))
                } else showMsg(getString(R.string.invalid_credential))
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
        val call = api?.loginToken
        call?.enqueue(object : Callback<WikiToken?> {
            override fun onResponse(call: Call<WikiToken?>, response: Response<WikiToken?>) {
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val lgToken = response.body()?.query?.tokenValue?.loginToken
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