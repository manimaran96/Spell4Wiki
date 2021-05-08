package com.manimarank.spell4wiki.data.auth

import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.Spell4WikiApp
import com.manimarank.spell4wiki.Spell4WikiApp.Companion.getApplicationContext
import com.manimarank.spell4wiki.data.model.WikiUser
import com.manimarank.spell4wiki.utils.Print.error
import com.manimarank.spell4wiki.utils.Print.log

object AccountUtils {
    private fun createAccount(userName: String, password: String): Boolean {
        var account = account()
        log("ACCOUNT - CREATE CALL")
        if (account == null || TextUtils.isEmpty(account.name) || account.name != userName) {
            removeAccount()
            account = Account(userName, accountType())
            return accountManager().addAccountExplicitly(account, password, null)
        }
        return true
    }

    fun updateAccount(response: AccountAuthenticatorResponse?, wikiUser: WikiUser) {
        if (createAccount(wikiUser.userName, wikiUser.password)) {
            if (response != null) {
                val bundle = Bundle()
                bundle.putString(AccountManager.KEY_ACCOUNT_NAME, wikiUser.userName)
                bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType())
                response.onResult(bundle)
            }
            log("ACCOUNT - CREATED -- response " + (response != null))
        } else {
            response?.onError(AccountManager.ERROR_CODE_REMOTE_EXCEPTION, "")
            error("ACCOUNT - CREATION FAIL")
            return
        }
        setPassword(wikiUser.password)
    }

    fun account(): Account? {
        try {
            val accounts = accountManager().getAccountsByType(accountType())
            if (accounts.isNotEmpty()) {
                return accounts[0]
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
        return null
    }

    fun accountType(): String {
        return app().getString(R.string.account_type)
    }

    private fun accountManager(): AccountManager {
        return AccountManager.get(app())
    }

    private fun app(): Spell4WikiApp {
        return getApplicationContext()
    }

    val isLoggedIn: Boolean
        get() = account() != null
    @JvmStatic
    val userName: String?
        get() {
            val account = account()
            return account?.name
        }
    @JvmStatic
    val password: String?
        get() {
            val account = account()
            return if (account == null) null else accountManager().getPassword(account)
        }

    private fun setPassword(password: String) {
        val account = account()
        if (account != null) {
            accountManager().setPassword(account, password)
        }
    }

    fun removeAccount() {
        log("ACCOUNT - REMOVE")
        val account = account()
        if (account != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                accountManager().removeAccountExplicitly(account)
            } else {
                accountManager().removeAccount(account, null, null)
            }
        }
    }
}