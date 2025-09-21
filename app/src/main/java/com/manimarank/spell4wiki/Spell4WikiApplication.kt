package com.manimarank.spell4wiki

import android.app.Application
import com.manimarank.spell4wiki.utils.ThemeUtils

/**
 * Application class for Spell4Wiki
 * Handles app-wide initialization including theme setup
 */
class Spell4WikiApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Initialize theme on app startup
        ThemeUtils.initializeTheme(this)
    }
}
