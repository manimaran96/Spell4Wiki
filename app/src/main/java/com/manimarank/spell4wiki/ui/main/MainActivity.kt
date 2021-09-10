package com.manimarank.spell4wiki.ui.main

import android.app.AlertDialog
import android.content.*
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.widget.SearchView
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.prefs.AppPref.INSTANCE.checkAppUpdateAvailable
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.ui.about.AboutActivity
import com.manimarank.spell4wiki.ui.common.BaseActivity
import com.manimarank.spell4wiki.ui.dialogs.AppLanguageDialog
import com.manimarank.spell4wiki.ui.dialogs.RateAppDialog
import com.manimarank.spell4wiki.ui.dialogs.UpdateAppDialog
import com.manimarank.spell4wiki.ui.settings.SettingsActivity
import com.manimarank.spell4wiki.ui.spell4wiktionary.Spell4Wiktionary
import com.manimarank.spell4wiki.ui.spell4wiktionary.Spell4WordActivity
import com.manimarank.spell4wiki.ui.spell4wiktionary.Spell4WordListActivity
import com.manimarank.spell4wiki.ui.spell4wiktionary.WiktionarySearchActivity
import com.manimarank.spell4wiki.utils.GeneralUtils.hideKeyboard
import com.manimarank.spell4wiki.utils.GeneralUtils.openUrl
import com.manimarank.spell4wiki.utils.NetworkUtils.isConnected
import com.manimarank.spell4wiki.utils.SnackBarUtils.showLong
import com.manimarank.spell4wiki.utils.SnackBarUtils.showNormal
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.utils.constants.Urls
import com.manimarank.spell4wiki.utils.makeGone
import com.manimarank.spell4wiki.utils.makeVisible
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity(), View.OnClickListener {
    // Views
    var filter = IntentFilter(AppLanguageDialog.LANGUAGE_FILTER)
    var languageChangeReceiver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!isDestroyed && !isFinishing && intent.extras != null) {
                val value = intent.extras?.getString(AppLanguageDialog.SELECTED_LANGUAGE, "")
                if (value != null) {
                    recreate()
                }
            }
        }
    }
    private var doubleBackToExitPressedOnce = false
    private lateinit var pref: PrefManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pref = PrefManager(this@MainActivity)
        setContentView(R.layout.activity_main)
        initViews()
        registerReceiver(languageChangeReceiver, filter)
        search_view.queryHint = getString(R.string.wiktionary_search)
        search_view.setIconifiedByDefault(false)
        search_view.clearFocus()
        search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                val intent = Intent(applicationContext, WiktionarySearchActivity::class.java)
                intent.putExtra(AppConstants.SEARCH_TEXT, query)
                startActivity(intent)
                Handler().postDelayed({ search_view.setQuery("", false) }, 100)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
        Handler().post { hideKeyboard(this@MainActivity) }

        // Update and Rate the app
        if (checkAppUpdateAvailable(this@MainActivity)) UpdateAppDialog.show(this@MainActivity) else RateAppDialog.show(this@MainActivity)
    }

    override fun onClick(view: View) {
        if (!isConnected(applicationContext)) {
            showLong(view, getString(R.string.check_internet))
            return
        }
        if (pref.isAnonymous == true) {
            showLong(view, getString(R.string.login_to_contribute))
            return
        }
        when (view.id) {
            R.id.card_spell4wiki -> {
                val intentWiki = Intent(applicationContext, Spell4Wiktionary::class.java)
                intentWiki.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intentWiki)
            }
            R.id.card_spell4wordlist -> {
                val intentWordList = Intent(applicationContext, Spell4WordListActivity::class.java)
                intentWordList.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intentWordList)
            }
            R.id.card_spell4word -> {
                val intentWord = Intent(applicationContext, Spell4WordActivity::class.java)
                intentWord.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intentWord)
            }
        }
    }

    /**
     * Init views
     */
    private fun initViews() {
        card_spell4wiki.setOnClickListener(this)
        card_spell4wordlist.setOnClickListener(this)
        card_spell4word.setOnClickListener(this)
        txt_welcome_user.text = String.format(getString(R.string.welcome_user), pref.name)
        btn_about.setOnClickListener { startActivity(Intent(applicationContext, AboutActivity::class.java)) }
        btn_settings.setOnClickListener { startActivity(Intent(applicationContext, SettingsActivity::class.java)) }
        btn_logout.setOnClickListener { logoutUser() }
        val urlMyContribution = String.format(Urls.COMMONS_CONTRIBUTION, pref.name)
        txtViewMyContribution.setOnClickListener { if (isConnected(applicationContext)) openUrl(this@MainActivity, urlMyContribution, getString(R.string.view_my_contribution)) else showNormal(txtViewMyContribution, getString(R.string.check_internet)) }
        txtLogin.setOnClickListener { if (isConnected(applicationContext)) pref.logoutUser() else showNormal(search_view, getString(R.string.check_internet)) }
        if (pref.isAnonymous == true) {
            txtViewMyContribution.makeGone()
            txt_welcome_user.makeGone()
            btn_logout.makeGone()
            layoutLogin.makeVisible()
        }
    }

    private fun logoutUser() {
        if (isConnected(applicationContext)) {
            AlertDialog.Builder(this)
                    .setTitle(R.string.logout_confirmation)
                    .setMessage(R.string.logout_message)
                    .setPositiveButton(getString(R.string.yes)) { _: DialogInterface?, _: Int ->
                        // Logout user
                        logoutApi()
                        pref.logoutUser()
                    }
                    .setNegativeButton(R.string.no, null)
                    .show()
        } else showNormal(search_view, getString(R.string.check_internet))
    }

    private fun logoutApi() {}
    override fun onResume() {
        super.onResume()
        search_view.clearFocus()
        hideKeyboard(this)
        if (!isConnected(applicationContext)) showNormal(search_view, getString(R.string.check_internet))
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) 
            super.onBackPressed() 
        else {
            doubleBackToExitPressedOnce = true
            showLong(search_view, getString(R.string.alert_to_exit))
        }
        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (languageChangeReceiver != null) unregisterReceiver(languageChangeReceiver)
    }
}