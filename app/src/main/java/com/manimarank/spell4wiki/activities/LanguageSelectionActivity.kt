package com.manimarank.spell4wiki.activities

import android.content.Intent
import android.os.Bundle
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.activities.base.BaseActivity
import com.manimarank.spell4wiki.utils.pref.PrefManager
import com.manimarank.spell4wiki.ui.dialogs.AppLanguageDialog
import kotlinx.android.synthetic.main.activity_language_selection.*
import kotlinx.android.synthetic.main.bottom_sheet_language_selection.btnAddMyLanguage
import kotlinx.android.synthetic.main.bottom_sheet_language_selection.txtAddLangInfo

class LanguageSelectionActivity : BaseActivity() {

    private lateinit var pref: PrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_selection)

        pref = PrefManager(applicationContext)

        txtAddLangInfo.text = String.format(getString(R.string.choose_your_preferred_language), AppLanguageDialog.getSelectedLanguage())

        btnAddMyLanguage.setOnClickListener {
            AppLanguageDialog.show(this)
        }

        btnNext.setOnClickListener {
            openMainActivity()
        }
    }

    private fun openMainActivity() {
        val intent = Intent(applicationContext, AppIntroActivity::class.java)
        startActivity(intent)
        finish()
    }
}
