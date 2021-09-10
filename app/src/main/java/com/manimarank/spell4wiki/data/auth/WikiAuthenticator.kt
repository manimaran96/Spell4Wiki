package com.manimarank.spell4wiki.data.auth

import android.accounts.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.ui.login.LoginActivity
import com.manimarank.spell4wiki.utils.Print.log

class WikiAuthenticator internal constructor(private val mContext: Context) : AbstractAccountAuthenticator(mContext) {
    private val TAG = WikiAuthenticator::class.java.simpleName + " --> "
    private fun supportedAccountType(type: String?): Boolean {
        return AccountUtils.accountType() == type
    }

    @Throws(NetworkErrorException::class)
    override fun addAccount(response: AccountAuthenticatorResponse, accountType: String, authTokenType: String, requiredFeatures: Array<String>, options: Bundle): Bundle {
        log(TAG + "Add Account Main")
        return if (!supportedAccountType(accountType) || AccountUtils.account() != null) {
            unsupportedOperation()
        } else addAccount(response)
    }

    private fun addAccount(response: AccountAuthenticatorResponse): Bundle {
        log(TAG + "Add Account Sub")
        val intent = Intent(mContext, LoginActivity::class.java)
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
        val bundle = Bundle()
        bundle.putParcelable(AccountManager.KEY_INTENT, intent)
        return bundle
    }

    override fun getAuthTokenLabel(authTokenType: String): String {
        log(TAG + "Get Auth Token Label - " + authTokenType)
        return if (supportedAccountType(authTokenType)) mContext.getString(R.string.account_name) else ""
    }

    override fun hasFeatures(response: AccountAuthenticatorResponse, account: Account, features: Array<String>): Bundle {
        val result = Bundle()
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false)
        return result
    }

    override fun editProperties(response: AccountAuthenticatorResponse, accountType: String): Bundle {
        return unsupportedOperation()
    }

    override fun confirmCredentials(response: AccountAuthenticatorResponse, account: Account, options: Bundle): Bundle {
        return unsupportedOperation()
    }

    override fun getAuthToken(response: AccountAuthenticatorResponse, account: Account, authTokenType: String, options: Bundle): Bundle {
        return unsupportedOperation()
    }

    override fun updateCredentials(response: AccountAuthenticatorResponse, account: Account, authTokenType: String, options: Bundle): Bundle {
        return unsupportedOperation()
    }

    private fun unsupportedOperation(): Bundle {
        log(TAG + "unsupportedOperation")
        val bundle = Bundle()
        bundle.putInt(AccountManager.KEY_ERROR_CODE, AccountManager.ERROR_CODE_UNSUPPORTED_OPERATION)
        // HACK: the docs indicate that this is a required key bit it's not displayed to the user.
        bundle.putString(AccountManager.KEY_ERROR_MESSAGE, "")
        return bundle
    }
}