package com.manimarank.spell4wiki.ui.common

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.manimarank.spell4wiki.ui.dialogs.AppLanguageDialog

abstract class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(AppLanguageDialog.applyLanguageConfig(base))
    }
}