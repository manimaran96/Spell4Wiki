package com.manimarank.spell4wiki.ui.spell4wiktionary

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.*
import android.widget.TextView
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.db.DBHelper
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.data.prefs.ShowCasePref
import com.manimarank.spell4wiki.data.prefs.ShowCasePref.isNotShowed
import com.manimarank.spell4wiki.data.prefs.ShowCasePref.showed
import com.manimarank.spell4wiki.ui.common.BaseActivity
import com.manimarank.spell4wiki.ui.dialogs.showConfirmBackDialog
import com.manimarank.spell4wiki.ui.languageselector.LanguageSelectionFragment
import com.manimarank.spell4wiki.ui.listerners.OnLanguageSelectionListener
import com.manimarank.spell4wiki.ui.webui.CommonWebActivity
import com.manimarank.spell4wiki.utils.GeneralUtils.getPromptBuilder
import com.manimarank.spell4wiki.utils.GeneralUtils.showRecordDialog
import com.manimarank.spell4wiki.utils.NetworkUtils.isConnected
import com.manimarank.spell4wiki.utils.SnackBarUtils.showLong
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.utils.constants.ListMode
import com.manimarank.spell4wiki.utils.constants.Urls
import com.manimarank.spell4wiki.utils.removeStyleAfterPaste
import kotlinx.android.synthetic.main.activity_spell_4_word.*
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetSequence
import java.util.*

class Spell4WordActivity : BaseActivity() {
    private lateinit var pref: PrefManager
    private var languageCode: String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spell_4_word)
        pref = PrefManager(this)
        languageCode = pref.languageCodeSpell4Word
        initUI()
    }

    private fun openWiktionaryPage(wordInfo: String) {
        val intent = Intent(applicationContext, CommonWebActivity::class.java)
        val url = String.format(Urls.WIKTIONARY_WEB, pref.languageCodeSpell4Word, wordInfo)
        intent.putExtra(AppConstants.TITLE, wordInfo)
        intent.putExtra(AppConstants.URL, url)
        intent.putExtra(AppConstants.IS_WIKTIONARY_WORD, true)
        intent.putExtra(AppConstants.LANGUAGE_CODE, pref.languageCodeSpell4Word)
        startActivity(intent)
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun initUI() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.spell4word)

        editSpell4Word.setOnTouchListener { _: View?, event: MotionEvent ->
            val DRAWABLE_RIGHT = 2
            if (event.action == MotionEvent.ACTION_UP && event.rawX >= editSpell4Word.right - editSpell4Word.compoundDrawables[DRAWABLE_RIGHT].bounds.width()) {
                if (!TextUtils.isEmpty(editSpell4Word.text) && editSpell4Word.text?.length ?: 0 < 30) openWiktionaryPage(
                    editSpell4Word.text.toString()
                ) else showLong(editSpell4Word, getString(R.string.enter_valid_word))
                return@setOnTouchListener true
            }
            false
        }

        // Remove styles after paste content
        editSpell4Word.removeStyleAfterPaste()
        btn_record.setOnClickListener {
            if (!TextUtils.isEmpty(editSpell4Word.text) && editSpell4Word.text?.length ?: 0 < 30) {
                if (isConnected(applicationContext)) {
                    val word = editSpell4Word.text.toString().trim { it <= ' ' }
                    if (isAllowRecord(word)) showRecordDialog(this@Spell4WordActivity, word, languageCode) else showLong(editSpell4Word, String.format(getString(R.string.audio_file_already_exist), word))
                } else showLong(editSpell4Word, getString(R.string.check_internet))
            } else showLong(editSpell4Word, getString(R.string.enter_valid_word))
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
                    invalidateOptionsMenu()
                }
            }
        }
        val languageSelectionFragment = LanguageSelectionFragment(this)
        languageSelectionFragment.init(callback, ListMode.SPELL_4_WORD)
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
        val rootView = item.actionView
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
        if (!TextUtils.isEmpty(editSpell4Word.text)) {
            this.showConfirmBackDialog { super.onBackPressed() }
        } else {
            super.onBackPressed()
        }
    }
}