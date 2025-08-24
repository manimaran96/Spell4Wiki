package com.manimarank.spell4wiki.ui.main

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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
import com.manimarank.spell4wiki.utils.EdgeToEdgeUtils.setupStatusBarHandling
import com.manimarank.spell4wiki.utils.GeneralUtils.hideKeyboard
import com.manimarank.spell4wiki.utils.GeneralUtils.openUrl
import com.manimarank.spell4wiki.utils.NetworkUtils.isConnected
import com.manimarank.spell4wiki.utils.SnackBarUtils.showLong
import com.manimarank.spell4wiki.utils.SnackBarUtils.showNormal
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.utils.constants.Urls
import com.manimarank.spell4wiki.utils.makeGone
import com.manimarank.spell4wiki.utils.makeVisible
import com.manimarank.spell4wiki.databinding.ActivityMainBinding

class MainActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    // Views
    private var languageChangeReceiver: BroadcastReceiver? = object : BroadcastReceiver() {
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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup proper status bar handling
        setupStatusBarHandling(binding.root)

        initViews()
        languageChangeReceiver?.let {
            LocalBroadcastManager.getInstance(this).registerReceiver(
                it, IntentFilter(AppLanguageDialog.SELECTED_LANGUAGE))
        }
        binding.searchView.queryHint = getString(R.string.wiktionary_search)
        binding.searchView.setIconifiedByDefault(false)
        binding.searchView.clearFocus()
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                val intent = Intent(applicationContext, WiktionarySearchActivity::class.java)
                intent.putExtra(AppConstants.SEARCH_TEXT, query)
                startActivity(intent)
                Handler().postDelayed({ binding.searchView.setQuery("", false) }, 100)
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
        binding.cardSpell4wiki.setOnClickListener(this)
        binding.cardSpell4wordlist.setOnClickListener(this)
        binding.cardSpell4word.setOnClickListener(this)
        binding.txtWelcomeUser.text = String.format(getString(R.string.welcome_user), pref.name)
        binding.btnAbout.setOnClickListener { startActivity(Intent(applicationContext, AboutActivity::class.java)) }
        binding.btnSettings.setOnClickListener { startActivity(Intent(applicationContext, SettingsActivity::class.java)) }
        binding.btnLogout.setOnClickListener { logoutUser() }
        val urlMyContribution = String.format(Urls.COMMONS_CONTRIBUTION, pref.name)
        binding.txtViewMyContribution.setOnClickListener { if (isConnected(applicationContext)) openUrl(this@MainActivity, urlMyContribution, getString(R.string.view_my_contribution)) else showNormal(binding.txtViewMyContribution, getString(R.string.check_internet)) }
        binding.txtLogin.setOnClickListener { if (isConnected(applicationContext)) pref.logoutUser() else showNormal(binding.searchView, getString(R.string.check_internet)) }
        if (pref.isAnonymous == true) {
            binding.txtViewMyContribution.makeGone()
            binding.txtWelcomeUser.makeGone()
            binding.btnLogout.makeGone()
            binding.layoutLogin.makeVisible()
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
        } else showNormal(binding.searchView, getString(R.string.check_internet))
    }

    private fun logoutApi() {}
    override fun onResume() {
        super.onResume()
        binding.searchView.clearFocus()
        hideKeyboard(this)
        if (!isConnected(applicationContext)) showNormal(binding.searchView, getString(R.string.check_internet))
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) 
            super.onBackPressed() 
        else {
            doubleBackToExitPressedOnce = true
            showLong(binding.searchView, getString(R.string.alert_to_exit))
        }
        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

    override fun onDestroy() {
        super.onDestroy()
        languageChangeReceiver?.let {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(it)
        }
    }
}