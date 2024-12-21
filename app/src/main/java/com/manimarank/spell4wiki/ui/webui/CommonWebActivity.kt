package com.manimarank.spell4wiki.ui.webui

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.db.DBHelper
import com.manimarank.spell4wiki.data.db.dao.WikiLangDao
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.ui.common.BaseActivity
import com.manimarank.spell4wiki.ui.languageselector.LanguageSelectionFragment
import com.manimarank.spell4wiki.ui.listerners.OnLanguageSelectionListener
import com.manimarank.spell4wiki.utils.GeneralUtils
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.utils.constants.ListMode
import java.util.Locale

class CommonWebActivity : BaseActivity() {
    private var wikiLangDao: WikiLangDao? = null
    private var isWiktionaryWord = false
    private val fragment: WebViewFragment = WebViewFragment()
    private var languageCode: String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common_web_view)

        val pref = PrefManager(applicationContext)
        languageCode = pref.languageCodeSpell4WikiAll
        wikiLangDao = DBHelper.getInstance(applicationContext).appDatabase.wikiLangDao

        // Title & Sub title
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val bundle = intent.extras
        if (bundle != null) {
            val title: String?
            if (bundle.containsKey(AppConstants.TITLE)) {
                title = bundle.getString(AppConstants.TITLE)
                setTitle(title)
            }
            if (bundle.containsKey(AppConstants.IS_WIKTIONARY_WORD))
                isWiktionaryWord = bundle.getBoolean(AppConstants.IS_WIKTIONARY_WORD)
            if (bundle.containsKey(AppConstants.LANGUAGE_CODE)) {
                languageCode = bundle.getString(AppConstants.LANGUAGE_CODE)
                supportActionBar?.subtitle = GeneralUtils.getLanguageInfo(applicationContext, wikiLangDao?.getWikiLanguageWithCode(languageCode))
            }
            loadFragment(fragment)
        }
    }

    private fun loadFragment(fragment: Fragment?) {
        val fm = supportFragmentManager
        val fragmentTransaction = fm.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment!!)
        fragmentTransaction.commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.web_view_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_favorite -> true
            R.id.action_share -> {
                fragment.shareLink()
                true
            }
            R.id.action_refresh -> {
                fragment.refreshWebPage()
                true
            }
            R.id.action_forward -> {
                fragment.forwardWebPage()
                true
            }
            R.id.action_backward -> {
                fragment.backwardWebPage()
                true
            }
            R.id.action_open_in_browser -> {
                fragment.openInAppBrowser()
                true
            }
            R.id.action_copy_link -> {
                fragment.copyLink()
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupLanguageSelectorMenuItem(menu: Menu) {
        menu.findItem(R.id.menu_lang_selector).isVisible = isWiktionaryWord
        if (isWiktionaryWord) {
            val item = menu.findItem(R.id.menu_lang_selector)
            val rootView = item.actionView ?: return
            val selectedLang = rootView.findViewById<TextView>(R.id.txtSelectedLanguage)
            selectedLang.text = languageCode?.toUpperCase(Locale.ROOT) ?: ""
            rootView.setOnClickListener { loadLanguages() }
        }
    }

    private fun loadLanguages() {
        if (isWiktionaryWord) {
            val callback = object : OnLanguageSelectionListener {
                override fun onCallBackListener(langCode: String?) {
                    if (languageCode != langCode) {
                        languageCode = langCode
                        supportActionBar?.subtitle = GeneralUtils.getLanguageInfo(applicationContext, wikiLangDao?.getWikiLanguageWithCode(langCode))
                        invalidateOptionsMenu()
                        fragment.loadWordWithOtherLang(langCode)
                    }
                }
            }
            val languageSelectionFragment = LanguageSelectionFragment(this)
            languageSelectionFragment.init(callback, ListMode.TEMP, languageCode)
            languageSelectionFragment.show(supportFragmentManager)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val result = super.onPrepareOptionsMenu(menu)
        changeMenuButtonStyle(menu.findItem(R.id.action_forward), fragment.canGoForward())
        changeMenuButtonStyle(menu.findItem(R.id.action_backward), fragment.canGoBackward())
        setupLanguageSelectorMenuItem(menu)
        return result
    }

    private fun changeMenuButtonStyle(menuItem: MenuItem?, isAllow: Boolean) {
        if (menuItem != null) {
            val s = SpannableString(menuItem.title)
            s.setSpan(ForegroundColorSpan(ContextCompat.getColor(applicationContext, if (isAllow) R.color.black else R.color.light_gray)), 0, s.length, 0)
            menuItem.isEnabled = isAllow
            menuItem.title = s
        }
    }

    fun updateList(word: String?) {
        fragment.hideRecordButton(word!!)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppConstants.RC_UPLOAD_DIALOG) {
            if (data != null && data.hasExtra(AppConstants.WORD)) {
                updateList(data.getStringExtra(AppConstants.WORD))
            }
        }
    }
}