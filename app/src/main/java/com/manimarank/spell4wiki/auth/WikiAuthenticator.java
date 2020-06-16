package com.manimarank.spell4wiki.auth;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.manimarank.spell4wiki.R;
import com.manimarank.spell4wiki.activities.LoginActivity;
import com.manimarank.spell4wiki.utils.Print;

import static android.accounts.AccountManager.KEY_BOOLEAN_RESULT;

public class WikiAuthenticator extends AbstractAccountAuthenticator {

    private final Context mContext;
    private String TAG = WikiAuthenticator.class.getSimpleName() + " --> ";

    WikiAuthenticator(Context context) {
        super(context);
        this.mContext = context;
    }

    private boolean supportedAccountType(@Nullable String type) {
        return AccountUtils.accountType().equals(type);
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        Print.log(TAG + "Add Account Main");
        if (!supportedAccountType(accountType) || AccountUtils.account() != null) {
            return unsupportedOperation();
        }
        return addAccount(response);
    }

    private Bundle addAccount(AccountAuthenticatorResponse response) {
        Print.log(TAG + "Add Account Sub");
        Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }


    @Override
    public String getAuthTokenLabel(String authTokenType) {
        Print.log(TAG + "Get Auth Token Label - " + authTokenType);
        return supportedAccountType(authTokenType) ? mContext.getString(R.string.account_name) : null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) {
        final Bundle result = new Bundle();
        result.putBoolean(KEY_BOOLEAN_RESULT, false);
        return result;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return unsupportedOperation();
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) {
        return unsupportedOperation();
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) {
        return unsupportedOperation();
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) {
        return unsupportedOperation();
    }

    private Bundle unsupportedOperation() {
        Print.log(TAG + "unsupportedOperation");
        Bundle bundle = new Bundle();
        bundle.putInt(AccountManager.KEY_ERROR_CODE, AccountManager.ERROR_CODE_UNSUPPORTED_OPERATION);
        // HACK: the docs indicate that this is a required key bit it's not displayed to the user.
        bundle.putString(AccountManager.KEY_ERROR_MESSAGE, "");
        return bundle;
    }
}