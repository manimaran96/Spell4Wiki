package com.manimaran.wikiaudio.auth;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.Spell4WikiApp;
import com.manimaran.wikiaudio.models.WikiUser;
import com.manimaran.wikiaudio.utils.Print;

public class AccountUtils {

    private static boolean createAccount(@NonNull String userName, @NonNull String password) {
        Account account = account();
        Print.log("ACCOUNT - CREATE CALL");
        if (account == null || TextUtils.isEmpty(account.name) || !account.name.equals(userName)) {
            removeAccount();
            account = new Account(userName, accountType());
            return accountManager().addAccountExplicitly(account, password, null);
        }
        return true;
    }

    public static void updateAccount(@Nullable AccountAuthenticatorResponse response, WikiUser wikiUser) {
        if (createAccount(wikiUser.getUserName(), wikiUser.getPassword())) {
            if (response != null) {
                Bundle bundle = new Bundle();
                bundle.putString(AccountManager.KEY_ACCOUNT_NAME, wikiUser.getUserName());
                bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType());
                response.onResult(bundle);
            }
            Print.log("ACCOUNT - CREATED -- response " + (response != null));
        } else {
            if (response != null) {
                response.onError(AccountManager.ERROR_CODE_REMOTE_EXCEPTION, "");
            }
            Print.error("ACCOUNT - CREATION FAIL");
            return;
        }

        setPassword(wikiUser.getPassword());
        //putUserIdForLanguage(result.getSite().languageCode(), result.getUserId());
        //setGroups(result.getGroups());
    }

    @Nullable
    static Account account() {
        try {
            Account[] accounts = accountManager().getAccountsByType(accountType());
            if (accounts.length > 0) {
                return accounts[0];
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    @NonNull
    static String accountType() {
        return app().getString(R.string.account_type);
    }

    private static AccountManager accountManager() {
        return AccountManager.get(app());
    }

    @NonNull
    private static Spell4WikiApp app() {
        return Spell4WikiApp.getInstance();
    }

    public static boolean isLoggedIn() {
        return account() != null;
    }

    @Nullable public static String getUserName() {
        Account account = account();
        return account == null ? null : account.name;
    }

    @Nullable
    public static String getPassword() {
        Account account = account();
        return account == null ? null : accountManager().getPassword(account);
    }

    private static void setPassword(@NonNull String password) {
        Account account = account();
        if (account != null) {
            accountManager().setPassword(account, password);
        }
    }

    public static void removeAccount() {
        Print.log("ACCOUNT - REMOVE");
        Account account = account();
        if (account != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                accountManager().removeAccountExplicitly(account);
            } else {
                accountManager().removeAccount(account, null, null);
            }
        }
    }

}
