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
import com.manimarank.spell4wiki.ui.main.MainActivity
import com.manimarank.spell4wiki.data.apis.ApiClient
import com.manimarank.spell4wiki.data.apis.ApiInterface
import com.manimarank.spell4wiki.data.auth.AccountUtils
import com.manimarank.spell4wiki.data.model.WikiLogin
import com.manimarank.spell4wiki.data.model.WikiToken
import com.manimarank.spell4wiki.data.model.WikiUser
import com.manimarank.spell4wiki.ui.common.BaseActivity
import com.manimarank.spell4wiki.utils.GeneralUtils.hideKeyboard
import com.manimarank.spell4wiki.utils.GeneralUtils.openUrl
import com.manimarank.spell4wiki.utils.NetworkUtils.isConnected
import com.manimarank.spell4wiki.utils.SnackBarUtils.showLong
import com.manimarank.spell4wiki.utils.ToastUtils.showLong
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.utils.constants.Urls
import com.manimarank.spell4wiki.utils.extensions.getAppVersion
import com.manimarank.spell4wiki.data.prefs.AppPref.INSTANCE.getVc4ForceLogout
import com.manimarank.spell4wiki.data.prefs.AppPref.INSTANCE.setVc4ForceLogoutDone
import com.manimarank.spell4wiki.data.prefs.PrefManager
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : BaseActivity() {
    private lateinit var pref: PrefManager
    private var api: ApiInterface? = null
    private var isDuringLogin = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        pref = PrefManager(applicationContext)

        // Force logout below version 4
        if (this.getAppVersion() < 4 && !getVc4ForceLogout()) {
            setVc4ForceLogoutDone()
            if (pref.isLoggedIn) {
                showLong(getString(R.string.login_expired))
                pref.clearLoginData()
            }
        }

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
            btn_login.setOnClickListener {
                if (!TextUtils.isEmpty(edit_username.text) && !TextUtils.isEmpty(edit_password.text)) {
                    if (isConnected(applicationContext)) {
                        hideKeyboard(this@LoginActivity)
                        btn_login.startAnimation()
                        isDuringLogin = true
                        callToken(edit_username.text.toString(), edit_password.text.toString())
                    } else showMsg(getString(R.string.check_internet))
                } else showMsg(getString(R.string.invalid_credential))
            }


            // Hit Skip Button
            btn_skip_login.setOnClickListener {
                if (isDuringLogin()) {
                    showMsg(getString(R.string.please_wait))
                } else {
                    pref.isAnonymous = true
                    launchActivity()
                }
            }

            // Hit Forgot Password Button
            btn_forgot_password.setOnClickListener { openUrl(Urls.FORGOT_PASSWORD, getString(R.string.forgot_password)) }

            // Hit Join Wikipedia Button
            btn_join_wikipedia.setOnClickListener { openUrl(Urls.JOIN_WIKI, getString(R.string.join_wiki)) }
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
                                    btn_login.doneLoadingAnimation(ContextCompat.getColor(this@LoginActivity, R.color.w_green), BitmapFactory.decodeResource(resources, R.drawable.ic_done))
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
        btn_login.revertAnimation()
        isDuringLogin = false
    }

    private fun showMsg(msg: String?) {
        showLong(btn_login, msg ?: "")
    }

    override fun onDestroy() {
        super.onDestroy()
        btn_login.revertAnimation()
        btn_login.dispose()
    }

    private fun openUrl(url: String, title: String) {
        if (isDuringLogin()) {
            showMsg(getString(R.string.please_wait))
        } else {
            if (isConnected(applicationContext)) openUrl(this@LoginActivity, url, title) else showMsg(getString(R.string.check_internet))
        }
    }

    private fun isDuringLogin(): Boolean {
        return btn_login.isAnimating || isDuringLogin
    }
}