package com.manimarank.spell4wiki.activities.base

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.manimarank.spell4wiki.utils.dialogs.AppLanguageDialog

abstract class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(AppLanguageDialog.applyLanguageConfig(base))
    }
}