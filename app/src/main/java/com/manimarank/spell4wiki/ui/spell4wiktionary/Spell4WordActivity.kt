package com.manimarank.spell4wiki.ui.spell4wiktionary

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.db.DBHelper
import com.manimarank.spell4wiki.data.db.dao.WikiLangDao
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.data.prefs.ShowCasePref
import com.manimarank.spell4wiki.data.prefs.ShowCasePref.isNotShowed
import com.manimarank.spell4wiki.data.prefs.ShowCasePref.showed
import com.manimarank.spell4wiki.ui.common.BaseActivity
import com.manimarank.spell4wiki.ui.dialogs.showConfirmBackDialog
import com.manimarank.spell4wiki.ui.languageselector.LanguageSelectionFragment
import com.manimarank.spell4wiki.ui.listerners.OnLanguageSelectionListener
import com.manimarank.spell4wiki.ui.webui.CommonWebActivity
import com.manimarank.spell4wiki.utils.EdgeToEdgeUtils.setupEdgeToEdgeWithToolbar
import com.manimarank.spell4wiki.utils.GeneralUtils
import com.manimarank.spell4wiki.utils.GeneralUtils.getPromptBuilder
import com.manimarank.spell4wiki.utils.GeneralUtils.showRecordDialog
import com.manimarank.spell4wiki.utils.NetworkUtils.isConnected
import com.manimarank.spell4wiki.utils.SnackBarUtils.showLong
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.utils.constants.ListMode
import com.manimarank.spell4wiki.utils.constants.Urls
import com.manimarank.spell4wiki.utils.removeStyleAfterPaste
import com.manimarank.spell4wiki.databinding.ActivitySpell4WordBinding
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetSequence
import java.util.Locale

class Spell4WordActivity : BaseActivity() {

    private lateinit var binding: ActivitySpell4WordBinding
    private var wikiLangDao: WikiLangDao? = null
    private lateinit var pref: PrefManager
    private var languageCode: String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpell4WordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pref = PrefManager(this)
        languageCode = pref.languageCodeSpell4WikiAll
        initUI()
    }

    private fun openWiktionaryPage(wordInfo: String) {
        val intent = Intent(applicationContext, CommonWebActivity::class.java)
        val url = String.format(Urls.WIKTIONARY_WEB, pref.languageCodeSpell4WikiAll, wordInfo)
        intent.putExtra(AppConstants.TITLE, wordInfo)
        intent.putExtra(AppConstants.URL, url)
        intent.putExtra(AppConstants.IS_WIKTIONARY_WORD, true)
        intent.putExtra(AppConstants.LANGUAGE_CODE, pref.languageCodeSpell4WikiAll)
        startActivity(intent)
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun initUI() {
        wikiLangDao = DBHelper.getInstance(applicationContext).appDatabase.wikiLangDao

        // Title & Sub title
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val wikiLang = wikiLangDao?.getWikiLanguageWithCode(languageCode)

        // Setup proper status bar handling
        setupEdgeToEdgeWithToolbar(
            rootView = binding.root,
            toolbar = toolbar
        )

        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.spell4word)
        supportActionBar?.subtitle = GeneralUtils.getLanguageInfo(applicationContext, wikiLang)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.editSpell4Word.setOnTouchListener { _: View?, event: MotionEvent ->
            val DRAWABLE_RIGHT = 2
            if (event.action == MotionEvent.ACTION_UP && event.rawX >= binding.editSpell4Word.right - binding.editSpell4Word.compoundDrawables[DRAWABLE_RIGHT].bounds.width()) {
                if (!TextUtils.isEmpty(binding.editSpell4Word.text) && binding.editSpell4Word.text?.length ?: 0 < 30) openWiktionaryPage(
                    binding.editSpell4Word.text.toString()
                ) else showLong(binding.editSpell4Word, getString(R.string.enter_valid_word))
                return@setOnTouchListener true
            }
            false
        }

        // Remove styles after paste content
        binding.editSpell4Word.removeStyleAfterPaste()
        binding.btnRecord.setOnClickListener {
            if (!TextUtils.isEmpty(binding.editSpell4Word.text) && binding.editSpell4Word.text?.length ?: 0 < 30) {
                if (isConnected(applicationContext)) {
                    val word = binding.editSpell4Word.text.toString().trim { it <= ' ' }
                    if (isAllowRecord(word)) showRecordDialog(this@Spell4WordActivity, word, languageCode) else showLong(binding.editSpell4Word, String.format(getString(R.string.audio_file_already_exist), word))
                } else showLong(binding.editSpell4Word, getString(R.string.check_internet))
            } else showLong(binding.editSpell4Word, getString(R.string.enter_valid_word))
        }
    }

    private fun isAllowRecord(word: String): Boolean {
        var isValid = false
        try {
            if (pref.isAnonymous != true && !TextUtils.isEmpty(word)) {
                val wordsHaveAudioDao = DBHelper.getInstance(applicationContext).appDatabase.wordsHaveAudioDao
                val wordsAlreadyHaveAudio = wordsHaveAudioDao?.getWordsAlreadyHaveAudioByLanguage(languageCode)
                isValid = wordsAlreadyHaveAudio?.contains(word) != true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return isValid
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        return if (menuItem.itemId == android.R.id.home) {
            callBackPress()
            true
        } else super.onOptionsItemSelected(menuItem)
    }

    private fun loadLanguages() {
        val callback = object : OnLanguageSelectionListener {
            override fun onCallBackListener(langCode: String?) {
                if (languageCode != langCode) {
                    languageCode = langCode
                    supportActionBar?.subtitle = GeneralUtils.getLanguageInfo(applicationContext, wikiLangDao?.getWikiLanguageWithCode(langCode))
                    invalidateOptionsMenu()
                }
            }
        }
        val languageSelectionFragment = LanguageSelectionFragment(this)
        languageSelectionFragment.init(callback, ListMode.SPELL_4_WIKI_ALL)
        languageSelectionFragment.show(supportFragmentManager)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.spell4wiki_view_menu, menu)
        Handler().post { callShowCaseUI() }
        return true
    }

    private fun setupLanguageSelectorMenuItem(menu: Menu) {
        val item = menu.findItem(R.id.menu_lang_selector)
        item.isVisible = true
        val rootView = item.actionView ?: return
        val selectedLang = rootView.findViewById<TextView>(R.id.txtSelectedLanguage)
        selectedLang.text = languageCode?.toUpperCase(Locale.ROOT) ?: ""
        rootView.setOnClickListener { loadLanguages() }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        setupLanguageSelectorMenuItem(menu)
        return super.onPrepareOptionsMenu(menu)
    }

    private fun callShowCaseUI() {
        if (!isFinishing && !isDestroyed && isNotShowed(ShowCasePref.SPELL_4_WORD_PAGE)) {
            val sequence = MaterialTapTargetSequence().setSequenceCompleteListener { showed(ShowCasePref.SPELL_4_WORD_PAGE) }
            sequence.addPrompt(
                getPromptBuilder(this@Spell4WordActivity)
                    .setTarget(R.id.editSpell4Word)
                    .setPrimaryText(R.string.sc_t_spell4word_page_edit_word)
                    .setSecondaryText(R.string.sc_d_spell4word_page_edit_word))
                    .show()
        }
    }

    fun updateList(word: String?) {}

    override fun onBackPressed() {
        callBackPress()
    }

    private fun callBackPress() {
        if (!TextUtils.isEmpty(binding.editSpell4Word.text)) {
            this.showConfirmBackDialog { super.onBackPressed() }
        } else {
            super.onBackPressed()
        }
    }
}