package com.manimarank.spell4wiki.ui.settings

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.view.MenuItem
import android.widget.CompoundButton
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.databases.DBHelper
import com.manimarank.spell4wiki.databases.dao.WikiLangDao
import com.manimarank.spell4wiki.fragments.LanguageSelectionFragment
import com.manimarank.spell4wiki.listerners.OnLanguageSelectionListener
import com.manimarank.spell4wiki.ui.common.BaseActivity
import com.manimarank.spell4wiki.ui.dialogs.AppLanguageDialog.getSelectedLanguage
import com.manimarank.spell4wiki.ui.dialogs.AppLanguageDialog.show
import com.manimarank.spell4wiki.utils.WikiLicense
import com.manimarank.spell4wiki.utils.WikiLicense.licenseNameId
import com.manimarank.spell4wiki.utils.WikiLicense.licenseUrlFor
import com.manimarank.spell4wiki.utils.constants.ListMode
import com.manimarank.spell4wiki.utils.makeGone
import com.manimarank.spell4wiki.utils.pref.PrefManager
import kotlinx.android.synthetic.main.activity_settings.*
import java.util.*

class SettingsActivity : BaseActivity() {
    private lateinit var pref: PrefManager
    private lateinit var wikiLangDao: WikiLangDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        title = getString(R.string.settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        pref = PrefManager(applicationContext)
        wikiLangDao = DBHelper.getInstance(applicationContext).appDatabase.wikiLangDao
        if (pref.isAnonymous == true) {
            txtTitleLicense.makeGone()
            layoutSpell4WikiLang.makeGone()
            layoutSpell4WordListLang.makeGone()
            layoutSpell4WordLang.makeGone()
            layoutLicenseOfUploadAudio.makeGone()
        }
        updateLanguageView(txtSpell4WikiLang, pref.languageCodeSpell4Wiki)
        updateLanguageView(txtSpell4WordListLang, pref.languageCodeSpell4WordList)
        updateLanguageView(txtSpell4WordLang, pref.languageCodeSpell4Word)
        updateLanguageView(txtWiktionaryLang, pref.languageCodeWiktionary)
        layoutSpell4WikiLang.setOnClickListener {
            val callback = object : OnLanguageSelectionListener {
                override fun onCallBackListener(langCode: String?) {
                    updateLanguageView(txtSpell4WikiLang, pref.languageCodeSpell4Wiki)
                }
            }
            val languageSelectionFragment = LanguageSelectionFragment(this)
            languageSelectionFragment.init(callback, ListMode.SPELL_4_WIKI)
            languageSelectionFragment.show(supportFragmentManager)
        }
        layoutSpell4WordListLang.setOnClickListener {
            val callback = object : OnLanguageSelectionListener {
                override fun onCallBackListener(langCode: String?) {
                    updateLanguageView(txtSpell4WordListLang, pref.languageCodeSpell4WordList)
                }
            }
            val languageSelectionFragment = LanguageSelectionFragment(this)
            languageSelectionFragment.init(callback, ListMode.SPELL_4_WORD_LIST)
            languageSelectionFragment.show(supportFragmentManager)
        }
        layoutSpell4WordLang.setOnClickListener {
            val callback = object : OnLanguageSelectionListener {
                override fun onCallBackListener(langCode: String?) {
                    updateLanguageView(txtSpell4WordLang, pref.languageCodeSpell4Word)
                }
            }
            val languageSelectionFragment = LanguageSelectionFragment(this)
            languageSelectionFragment.init(callback, ListMode.SPELL_4_WORD)
            languageSelectionFragment.show(supportFragmentManager)
        }
        layoutWiktionaryLang.setOnClickListener {
            val callback = object : OnLanguageSelectionListener {
                override fun onCallBackListener(langCode: String?) {
                    updateLanguageView(txtWiktionaryLang, pref.languageCodeWiktionary)
                }
            }
            val languageSelectionFragment = LanguageSelectionFragment(this)
            languageSelectionFragment.init(callback, ListMode.WIKTIONARY)
            languageSelectionFragment.show(supportFragmentManager)
        }
        updateLicenseView(txtLicenseOfUploadAudio, txtLicenseOfUploadAudioLegalCode)
        layoutLicenseOfUploadAudio.setOnClickListener {
            // setup the alert builder
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.license_choose_alert) // add a radio button list
            val licensePrefList = arrayOf(
                    WikiLicense.LicensePrefs.CC_0,
                    WikiLicense.LicensePrefs.CC_BY_3,
                    WikiLicense.LicensePrefs.CC_BY_SA_3,
                    WikiLicense.LicensePrefs.CC_BY_4,
                    WikiLicense.LicensePrefs.CC_BY_SA_4
            )
            val licenseList = arrayOf(
                    getString(R.string.license_name_cc_zero),
                    getString(R.string.license_name_cc_by_three),
                    getString(R.string.license_name_cc_by_sa_three),
                    getString(R.string.license_name_cc_by_four),
                    getString(R.string.license_name_cc_by_sa_four)
            )
            val checkedItem = licensePrefList.indexOf(pref.uploadAudioLicense)
            builder.setSingleChoiceItems(licenseList, checkedItem) { dialog: DialogInterface, which: Int ->
                pref.uploadAudioLicense = licensePrefList[which]
                updateLicenseView(txtLicenseOfUploadAudio, txtLicenseOfUploadAudioLegalCode)
                dialog.dismiss()
            }
            builder.setNegativeButton(getString(R.string.cancel), null)
            val dialog = builder.create()
            dialog.show()
        }
        txtAppLanguage.text = getSelectedLanguage()
        layoutLanguageOfApp.setOnClickListener { show(this@SettingsActivity) }
        updateAbortAlertStatus(switchAbortAlerts, txtAbortAlertStatus, pref.abortAlertStatus)
        layoutAbortAlert.setOnClickListener { updateAbortAlertStatus(switchAbortAlerts, txtAbortAlertStatus, pref.abortAlertStatus?.not()) }
        switchAbortAlerts.setOnCheckedChangeListener { _: CompoundButton?, show: Boolean -> updateAbortAlertStatus(switchAbortAlerts, txtAbortAlertStatus, show) }
    }

    private fun updateAbortAlertStatus(switchAbortAlerts: SwitchCompat, txtAbortAlertStatus: TextView, show: Boolean?) {
        switchAbortAlerts.isChecked = show ?: false
        pref.abortAlertStatus = show ?: false
        txtAbortAlertStatus.text = getString(if (show == true) R.string.show_exit_alerts_in_spell4wiki_options else R.string.hide_exit_alerts_in_spell4wiki_options)
    }

    private fun updateLanguageView(txtView: TextView, languageCode: String?) {
        if (languageCode != null) {
            val wikiLang = wikiLangDao.getWikiLanguageWithCode(languageCode)
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