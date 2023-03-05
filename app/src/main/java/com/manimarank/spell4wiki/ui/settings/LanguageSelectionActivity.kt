package com.manimarank.spell4wiki.ui.settings

import android.content.Intent
import android.os.Bundle
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.db.DBHelper
import com.manimarank.spell4wiki.data.db.dao.WikiLangDao
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
    var wikiLangDao: WikiLangDao? = null;
    private fun contributionLang() {
        wikiLangDao = DBHelper.getInstance(applicationContext).appDatabase.wikiLangDao
        setContentView(R.layout.contribution_language_selection)
        txtAddLangInfo.text = String.format(getString(R.string.choose_your_preferred_contribution_language), wikiLangDao?.getWikiLanguageWithCode(pref.languageCodeSpell4WikiAll)?.name ?: "")

        btnAddMyLanguage.setOnClickListener { loadLanguages() }

        btnNext.setOnClickListener {
            openMainActivity()
        }
    }

    private fun loadLanguages() {
        val callback = object : OnLanguageSelectionListener {
            override fun onCallBackListener(langCode: String?) {
                txtAddLangInfo.text = String.format(getString(R.string.choose_your_preferred_contribution_language), wikiLangDao?.getWikiLanguageWithCode(pref.languageCodeSpell4WikiAll)?.name ?: "")
            }
        }
        val languageSelectionFragment = LanguageSelectionFragment(this)
        languageSelectionFragment.init(callback, ListMode.SPELL_4_WIKI_ALL)
        languageSelectionFragment.show(supportFragmentManager)
    }
    private fun openMainActivity() {
        startActivity(Intent(applicationContext, AppIntroActivity::class.java))
        finish()
    }
}