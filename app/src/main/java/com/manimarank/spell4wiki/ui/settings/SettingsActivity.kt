package com.manimarank.spell4wiki.ui.settings

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.db.DBHelper
import com.manimarank.spell4wiki.data.db.dao.WikiLangDao
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.ui.common.BaseActivity
import com.manimarank.spell4wiki.ui.dialogs.AppLanguageDialog.getSelectedLanguage
import com.manimarank.spell4wiki.ui.dialogs.AppLanguageDialog.show
import com.manimarank.spell4wiki.ui.languageselector.LanguageSelectionFragment
import com.manimarank.spell4wiki.ui.listerners.OnLanguageSelectionListener
import com.manimarank.spell4wiki.ui.recordaudio.RecordAudioActivity
import com.manimarank.spell4wiki.utils.EdgeToEdgeUtils.setupStatusBarHandling
import com.manimarank.spell4wiki.utils.WikiLicense
import com.manimarank.spell4wiki.utils.WikiLicense.licenseNameId
import com.manimarank.spell4wiki.utils.WikiLicense.licenseUrlFor
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.utils.constants.ListMode
import com.manimarank.spell4wiki.utils.extensions.showLicenseChooseDialog
import com.manimarank.spell4wiki.utils.makeGone
import com.manimarank.spell4wiki.databinding.ActivitySettingsBinding
import java.util.*

class SettingsActivity : BaseActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var pref: PrefManager
    private var wikiLangDao: WikiLangDao? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup proper status bar handling
        setupStatusBarHandling(binding.root)

        title = getString(R.string.settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        pref = PrefManager(applicationContext)
        wikiLangDao = DBHelper.getInstance(applicationContext).appDatabase.wikiLangDao
        if (pref.isAnonymous == true) {
            binding.txtTitleLicense.makeGone()
            // binding.layoutSpell4WikiLang.makeGone()
            binding.layoutLicenseOfUploadAudio.makeGone()
            binding.layoutRunFilter.makeGone()
        }
        updateLanguageView(binding.txtSpell4WikiLang, pref.languageCodeSpell4WikiAll)
        binding.layoutSpell4WikiLang.setOnClickListener {
            val callback = object : OnLanguageSelectionListener {
                override fun onCallBackListener(langCode: String?) {
                    updateLanguageView(binding.txtSpell4WikiLang, pref.languageCodeSpell4WikiAll)
                }
            }
            val languageSelectionFragment = LanguageSelectionFragment(this)
            languageSelectionFragment.init(callback, ListMode.SPELL_4_WIKI_ALL)
            languageSelectionFragment.show(supportFragmentManager)
        }


        updateLicenseView(binding.txtLicenseOfUploadAudio, binding.txtLicenseOfUploadAudioLegalCode)



        binding.layoutLicenseOfUploadAudio.setOnClickListener {
            showLicenseChooseDialog {
                updateLicenseView(binding.txtLicenseOfUploadAudio, binding.txtLicenseOfUploadAudioLegalCode)
            } }

        binding.txtAppLanguage.text = getSelectedLanguage()
        binding.layoutLanguageOfApp.setOnClickListener { show(this@SettingsActivity) }

        // Wiktionary cleanup setting
        binding.switchWiktionaryCleanup.isChecked = pref.isWiktionaryCleanupEnabled
        binding.switchWiktionaryCleanup.setOnCheckedChangeListener { _, isChecked ->
            pref.isWiktionaryCleanupEnabled = isChecked
        }

        binding.txtRfCount.text = getString(R.string.run_filter_settings_count, pref.runFilterNumberOfWordsToCheck
                ?: AppConstants.RUN_FILTER_NO_OF_WORDS_CHECK_COUNT)
        binding.seekBarRunFilterCount.progress = pref.runFilterNumberOfWordsToCheck
                ?: AppConstants.RUN_FILTER_NO_OF_WORDS_CHECK_COUNT
        binding.seekBarRunFilterCount.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            var finalProgress = pref.runFilterNumberOfWordsToCheck
                    ?: AppConstants.RUN_FILTER_NO_OF_WORDS_CHECK_COUNT

            override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
                finalProgress = if (progress <= 0) 1 else progress
                binding.txtRfCount.text = getString(R.string.run_filter_settings_count, finalProgress)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) = Unit

            override fun onStopTrackingTouch(p0: SeekBar?) {
                pref.runFilterNumberOfWordsToCheck = finalProgress
            }

        })
    }

    private fun updateLanguageView(txtView: TextView, languageCode: String?) {
        if (languageCode != null) {
            val wikiLang = wikiLangDao?.getWikiLanguageWithCode(languageCode)
            var value = ""
            if (wikiLang != null && !TextUtils.isEmpty(wikiLang.name)) value = wikiLang.localName + " - " + wikiLang.name + " : " + languageCode
            txtView.text = value
        }
    }

    private fun updateLicenseView(txtLicenseOfUploadAudio: TextView, txtLicenseOfUploadAudioLegalCode: TextView) {
        txtLicenseOfUploadAudio.text = getString(licenseNameId(pref.uploadAudioLicense))
        txtLicenseOfUploadAudioLegalCode.movementMethod = LinkMovementMethod.getInstance()
        val ccLegalInfo = "(<a href=" + licenseUrlFor(pref.uploadAudioLicense) + "><font color='" + ContextCompat.getColor(applicationContext, R.color.w_green) + "'>legal code</font></a>)"
        txtLicenseOfUploadAudioLegalCode.text = HtmlCompat.fromHtml(ccLegalInfo, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(menuItem)
    }
}