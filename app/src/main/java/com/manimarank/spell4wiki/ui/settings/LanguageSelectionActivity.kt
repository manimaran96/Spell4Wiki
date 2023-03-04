package com.manimarank.spell4wiki.ui.settings

import android.content.Intent
import android.os.Bundle
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.ui.appintro.AppIntroActivity
import com.manimarank.spell4wiki.ui.common.BaseActivity
import com.manimarank.spell4wiki.ui.dialogs.AppLanguageDialog
import com.manimarank.spell4wiki.ui.languageselector.LanguageSelectionFragment
import com.manimarank.spell4wiki.ui.listerners.OnLanguageSelectionListener
import com.manimarank.spell4wiki.ui.spell4wiktionary.Spell4Wiktionary
import com.manimarank.spell4wiki.utils.constants.ListMode
import kotlinx.android.synthetic.main.activity_language_selection.*
import kotlinx.android.synthetic.main.bottom_sheet_language_selection.btnAddMyLanguage
import kotlinx.android.synthetic.main.bottom_sheet_language_selection.txtAddLangInfo

class LanguageSelectionActivity : BaseActivity() {
    var spellforwiki :Spell4Wiktionary = Spell4Wiktionary();
    private lateinit var pref: PrefManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_selection)

        pref = PrefManager(applicationContext)

        txtAddLangInfo.text = String.format(getString(R.string.choose_your_preferred_app_language), AppLanguageDialog.getSelectedLanguage())

        btnAddMyLanguage.setOnClickListener { AppLanguageDialog.show(this) }

        btnNext.setOnClickListener {
            contributionLang()
        }
    }
    private fun contributionLang(){
        setContentView(R.layout.contribution_language_selection)
        txtAddLangInfo.text = String.format(getString(R.string.choose_your_preferred_contribution_language), AppLanguageDialog.getSelectedLanguage())

        btnAddMyLanguage.setOnClickListener { loadLanguages() }

        btnNext.setOnClickListener {
            openMainActivity()

        }
    }

    private fun loadLanguages() {
        val callback = object : OnLanguageSelectionListener {
            override fun onCallBackListener(langCode: String?) {

            }
        }
        val languageSelectionFragment = LanguageSelectionFragment(this)
        languageSelectionFragment.init(callback, ListMode.SPELL_4_WIKI)
        languageSelectionFragment.show(supportFragmentManager)
    }
    private fun openMainActivity() {
        startActivity(Intent(applicationContext, AppIntroActivity::class.java))
        finish()
    }
}



