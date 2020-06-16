package com.manimarank.spell4wiki.activities.base

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.manimarank.spell4wiki.utils.AppLanguageUtils

abstract class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(AppLanguageUtils.applyLanguageConfig(base))
    }
}