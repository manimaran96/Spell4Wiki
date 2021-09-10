package com.manimarank.spell4wiki.ui.about

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.manimaran.crash_reporter.utils.AppUtils.getDeviceDetails
import com.manimarank.spell4wiki.BuildConfig
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.ui.common.BaseActivity
import com.manimarank.spell4wiki.utils.GeneralUtils.openUrl
import com.manimarank.spell4wiki.utils.GeneralUtils.openUrlInBrowser
import com.manimarank.spell4wiki.utils.NetworkUtils.isConnected
import com.manimarank.spell4wiki.utils.SnackBarUtils.showLong
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.utils.constants.Urls
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : BaseActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        title = getString(R.string.about)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        txtPoweredByLink.text = Urls.KANIYAM
        txtInitiatedByLink.text = Urls.VGLUG
        txt_app_version_and_license.setOnClickListener(this)
        txt_rate_app.setOnClickListener(this)
        txt_share.setOnClickListener(this)
        txt_how_to_contribute.setOnClickListener(this)
        txt_source_code.setOnClickListener(this)
        txt_contributors.setOnClickListener(this)
        txt_third_party_lib.setOnClickListener(this)
        txt_credits.setOnClickListener(this)
        txt_help_development.setOnClickListener(this)
        txtFeedback.setOnClickListener(this)
        txtTelegram.setOnClickListener(this)
        txtPrivacyPolicy.setOnClickListener(this)
        txtHelpTranslate.setOnClickListener(this)
        layout_kaniyam.setOnClickListener(this)
        layout_vglug.setOnClickListener(this)
        txt_app_version_and_license.movementMethod = LinkMovementMethod.getInstance()
        val appVersionLicense = getString(R.string.version) + " : " + BuildConfig.VERSION_NAME + " & " + getString(R.string.license) + " : <u><font color='" + ContextCompat.getColor(applicationContext, R.color.w_green) + "'>GPLv3</font></u>"
        txt_app_version_and_license.text = HtmlCompat.fromHtml(appVersionLicense, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(menuItem)
    }

    override fun onClick(v: View) {
        if (!isConnected(applicationContext)) {
            showLong(findViewById(R.id.txt_rate_app), getString(R.string.check_internet))
            return
        }
        when (v.id) {
            R.id.txt_rate_app -> openUrlInBrowser(this, Urls.APP_LINK)
            R.id.txt_share -> shareApp()
            R.id.txt_how_to_contribute -> openUrl(this, Urls.HOW_TO_CONTRIBUTE, getString(R.string.how_to_contribute))
            R.id.txt_source_code -> openUrlInBrowser(this, Urls.SOURCE_CODE)
            R.id.txt_contributors -> startActivity(Intent(applicationContext, ContributorsActivity::class.java))
            R.id.txt_third_party_lib -> {
                val intentTPL = Intent(applicationContext, ListItemActivity::class.java)
                intentTPL.putExtra(AppConstants.TITLE, getString(R.string.third_party_libraries))
                startActivity(intentTPL)
            }
            R.id.txt_credits -> {
                val intentCredits = Intent(applicationContext, ListItemActivity::class.java)
                intentCredits.putExtra(AppConstants.TITLE, getString(R.string.credits))
                startActivity(intentCredits)
            }
            R.id.txt_help_development -> openUrl(this, Urls.HELP_DEVELOPMENT, getString(R.string.help_development))
            R.id.txtFeedback -> feedback()
            R.id.txtTelegram -> openUrlInBrowser(this, Urls.TELEGRAM_CHANNEL)
            R.id.txtPrivacyPolicy -> openUrlInBrowser(this, Urls.PRIVACY_POLICY)
            R.id.txtHelpTranslate -> openUrlInBrowser(this, Urls.HELP_US_TRANSLATE)
            R.id.layout_kaniyam -> openUrlInBrowser(this, Urls.KANIYAM)
            R.id.layout_vglug -> openUrlInBrowser(this, Urls.VGLUG)
            R.id.txt_app_version_and_license -> openUrlInBrowser(this, Urls.GPL_V3)
        }
    }

    private fun shareApp() {
        try {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            val appInfo = """
                ${String.format(getString(R.string.app_share_invite_message), getString(R.string.app_description))}
                
                ${String.format(getString(R.string.app_share_link), Urls.APP_LINK)}
                """.trimIndent()
            intent.putExtra(Intent.EXTRA_TEXT, appInfo)
            startActivity(Intent.createChooser(intent, getString(R.string.app_share_title)))
        } catch (e: Exception) {
            showLong(findViewById(R.id.txt_share), getString(R.string.something_went_wrong))
        }
    }

    @SuppressLint("IntentReset")
    private fun feedback() {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.type = "message/rfc822"
        emailIntent.data = Uri.parse("mailto:")
        //emailIntent.setDataAndType(Uri.parse("mailto:"), "message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(AppConstants.CONTACT_MAIL))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " App - Feedback")
        emailIntent.putExtra(Intent.EXTRA_TEXT, String.format(
                "\n\n%s\n%s", "-- Basic Information --", extraInfo))
        try {
            startActivity(emailIntent)
        } catch (ex: ActivityNotFoundException) {
            showLong(findViewById(R.id.txtFeedback), getString(R.string.no_email_client))
        }
    }

    // Getting Username
    private val extraInfo: String
        get() {
            val builder = StringBuilder()
            builder.append(getDeviceDetails(applicationContext)).append("\n")
            val prefManager = PrefManager(applicationContext)
            // Getting Username
            builder.append("User name: ")
                    .append(if (prefManager.isAnonymous!!) "Anonymous User" else prefManager.name)
                    .append("\n")
            return builder.toString()
        }
}