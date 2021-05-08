package com.manimarank.spell4wiki.data.auth

import android.accounts.AbstractAccountAuthenticator
import android.app.Service
import android.content.Intent
import android.os.IBinder

class WikiAuthenticatorService : Service() {
    private var authenticator: AbstractAccountAuthenticator? = null
    override fun onCreate() {
        super.onCreate()
        authenticator = WikiAuthenticator(this)
    }

    override fun onBind(intent: Intent): IBinder? {
        return authenticator?.iBinder
    }
}