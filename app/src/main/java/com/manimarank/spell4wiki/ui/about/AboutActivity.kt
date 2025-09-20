package com.manimarank.spell4wiki.ui.about

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.manimaran.crash_reporter.utils.AppUtils.getDeviceDetails
import com.manimarank.spell4wiki.BuildConfig
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.databinding.ActivityAboutBinding
import com.manimarank.spell4wiki.ui.common.BaseActivity
import com.manimarank.spell4wiki.utils.EdgeToEdgeUtils.setupEdgeToEdgeWithToolbar
import com.manimarank.spell4wiki.utils.GeneralUtils.openUrl
import com.manimarank.spell4wiki.utils.GeneralUtils.openUrlInBrowser
import com.manimarank.spell4wiki.utils.NetworkUtils.executeWithNetworkCheck
import com.manimarank.spell4wiki.utils.NetworkUtils.isConnected
import com.manimarank.spell4wiki.utils.SnackBarUtils.showLong
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.utils.constants.Urls

class AboutActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityAboutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup proper status bar handling (AboutActivity doesn't have a toolbar)
        setupEdgeToEdgeWithToolbar(
            rootView = binding.root,
            toolbar = null
        )

        title = getString(R.string.about)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.txtPoweredByLink.text = Urls.KANIYAM
        binding.txtInitiatedByLink.text = Urls.VGLUG
        binding.txtAppVersionAndLicense.setOnClickListener(this)
        binding.txtRateApp.setOnClickListener(this)
        binding.txtShare.setOnClickListener(this)
        binding.txtHowToContribute.setOnClickListener(this)
        binding.txtSourceCode.setOnClickListener(this)
        binding.txtContributors.setOnClickListener(this)
        binding.txtThirdPartyLib.setOnClickListener(this)
        binding.txtCredits.setOnClickListener(this)
        binding.layoutHelpDevelopment.setOnClickListener(this)
        binding.txtFeedback.setOnClickListener(this)
        binding.txtPrivacyPolicy.setOnClickListener(this)
        binding.txtHelpTranslate.setOnClickListener(this)
        binding.layoutKaniyam.setOnClickListener(this)
        binding.layoutVglug.setOnClickListener(this)
        binding.txtAppVersionAndLicense.movementMethod = LinkMovementMethod.getInstance()
        val appVersionLicense = getString(R.string.version) + " : " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ") & " + getString(R.string.license) + " : <u><font color='" + ContextCompat.getColor(applicationContext, R.color.w_green) + "'>GPLv3</font></u>"
        binding.txtAppVersionAndLicense.text = HtmlCompat.fromHtml(appVersionLicense, HtmlCompat.FROM_HTML_MODE_LEGACY)

        // Start pulse animation for the heart icon
        startPulseAnimation()
    }

    private fun startPulseAnimation() {
        try {
            // Start pulse animation immediately - it's infinite so no need for handler
            binding.imgSupportDev.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulse))
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
            showLong(binding.txtRateApp, getString(R.string.check_internet))
            return
        }
        when (v.id) {
            R.id.txt_rate_app -> executeWithNetworkCheck(applicationContext, binding.txtRateApp) {
                openUrlInBrowser(this, Urls.APP_LINK)
            }
            R.id.txt_share -> shareApp()
            R.id.txt_how_to_contribute -> executeWithNetworkCheck(applicationContext, binding.txtHowToContribute) {
                openUrl(this, Urls.HOW_TO_CONTRIBUTE, getString(R.string.how_to_contribute))
            }
            R.id.txt_source_code -> executeWithNetworkCheck(applicationContext, binding.txtSourceCode) {
                openUrlInBrowser(this, Urls.SOURCE_CODE)
            }
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
            R.id.layout_help_development -> executeWithNetworkCheck(applicationContext, binding.layoutHelpDevelopment) {
                // Open donation page in external browser to avoid GitHub collector analytics issues
                openUrlInBrowser(this, Urls.HELP_DEVELOPMENT)
            }
            R.id.txtFeedback -> sendFeedback()
            R.id.txtPrivacyPolicy -> executeWithNetworkCheck(applicationContext, binding.txtPrivacyPolicy) {
                openUrlInBrowser(this, Urls.PRIVACY_POLICY)
            }
            R.id.txtHelpTranslate -> executeWithNetworkCheck(applicationContext, binding.txtHelpTranslate) {
                openUrlInBrowser(this, Urls.HELP_US_TRANSLATE)
            }
            R.id.layout_kaniyam -> executeWithNetworkCheck(applicationContext, binding.layoutKaniyam) {
                openUrlInBrowser(this, Urls.KANIYAM)
            }
            R.id.layout_vglug -> executeWithNetworkCheck(applicationContext, binding.layoutVglug) {
                openUrlInBrowser(this, Urls.VGLUG)
            }
            R.id.txt_app_version_and_license -> executeWithNetworkCheck(applicationContext, binding.txtAppVersionAndLicense) {
                openUrlInBrowser(this, Urls.GPL_V3)
            }
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
            showLong(binding.txtShare, getString(R.string.something_went_wrong))
        }
    }

    @SuppressLint("IntentReset")
    private fun sendFeedback() {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.type = "message/rfc822"
        emailIntent.data = Uri.parse("mailto:")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(AppConstants.CONTACT_MAIL))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " App - Feedback")
        emailIntent.putExtra(Intent.EXTRA_TEXT, String.format(
                "\n\n%s\n%s", "-- Basic Information --", extraInfo))
        try {
            startActivity(emailIntent)
        } catch (ex: ActivityNotFoundException) {
            showLong(binding.txtFeedback, getString(R.string.no_email_client))
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