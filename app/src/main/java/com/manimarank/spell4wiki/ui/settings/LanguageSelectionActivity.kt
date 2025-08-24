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
import com.manimarank.spell4wiki.utils.EdgeToEdgeUtils.setupStatusBarHandling
import com.manimarank.spell4wiki.utils.constants.ListMode
import com.manimarank.spell4wiki.databinding.ActivityLanguageSelectionBinding
import com.manimarank.spell4wiki.databinding.ContributionLanguageSelectionBinding

class LanguageSelectionActivity : BaseActivity() {

    private lateinit var binding: ActivityLanguageSelectionBinding
    private lateinit var contributionBinding: ContributionLanguageSelectionBinding
    private lateinit var pref: PrefManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup proper status bar handling
        setupStatusBarHandling(binding.root)

        pref = PrefManager(applicationContext)

        binding.txtAddLangInfo.text = String.format(getString(R.string.choose_your_preferred_app_language), AppLanguageDialog.getSelectedLanguage())

        binding.btnAddMyLanguage.setOnClickListener { AppLanguageDialog.show(this) }

        binding.btnNext.setOnClickListener {
            contributionLang()
        }
    }
    var wikiLangDao: WikiLangDao? = null;
    private fun contributionLang() {
        wikiLangDao = DBHelper.getInstance(applicationContext).appDatabase.wikiLangDao
        contributionBinding = ContributionLanguageSelectionBinding.inflate(layoutInflater)
        setContentView(contributionBinding.root)
        contributionBinding.txtAddLangInfo.text = String.format(getString(R.string.choose_your_preferred_contribution_language), wikiLangDao?.getWikiLanguageWithCode(pref.languageCodeSpell4WikiAll)?.name ?: "")

        contributionBinding.btnAddMyLanguage.setOnClickListener { loadLanguages() }

        contributionBinding.btnNext.setOnClickListener {
            openMainActivity()
        }
    }

    private fun loadLanguages() {
        val callback = object : OnLanguageSelectionListener {
            override fun onCallBackListener(langCode: String?) {
                contributionBinding.txtAddLangInfo.text = String.format(getString(R.string.choose_your_preferred_contribution_language), wikiLangDao?.getWikiLanguageWithCode(pref.languageCodeSpell4WikiAll)?.name ?: "")
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