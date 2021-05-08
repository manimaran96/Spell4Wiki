package com.manimarank.spell4wiki.data.auth;

import android.accounts.AbstractAccountAuthenticator;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class WikiAuthenticatorService extends Service {

    @Nullable private AbstractAccountAuthenticator authenticator;

    @Override
    public void onCreate() {
        super.onCreate();
        authenticator = new WikiAuthenticator(this);
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return authenticator == null ? null : authenticator.getIBinder();
    }
}