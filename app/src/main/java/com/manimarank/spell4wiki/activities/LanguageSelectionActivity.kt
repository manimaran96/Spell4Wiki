package com.manimarank.spell4wiki.activities

import android.content.Intent
import android.os.Bundle
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.activities.base.BaseActivity
import com.manimarank.spell4wiki.utils.AppLanguageUtils
import com.manimarank.spell4wiki.utils.PrefManager
import kotlinx.android.synthetic.main.activity_language_selection.*
import kotlinx.android.synthetic.main.bottom_sheet_language_selection.btnAddMyLanguage
import kotlinx.android.synthetic.main.bottom_sheet_language_selection.txtAddLangInfo

class LanguageSelectionActivity : BaseActivity() {

    private lateinit var pref: PrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_selection)

        pref = PrefManager(applicationContext)

        txtAddLangInfo.text = String.format(getString(R.string.choose_your_preferred_language), AppLanguageUtils.getSelectedLanguage())

        btnAddMyLanguage.setOnClickListener {
            AppLanguageUtils.showAppLanguageSelectionDialog(this)
        }

        btnNext.setOnClickListener{
            openMainActivity()
        }
    }

    private fun openMainActivity() {
        val intent = Intent(applicationContext, AppIntroActivity::class.java)
        startActivity(intent)
        finish()
    }
}
