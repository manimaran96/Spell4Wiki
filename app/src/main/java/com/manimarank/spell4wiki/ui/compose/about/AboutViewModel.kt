package com.manimarank.spell4wiki.ui.compose.about

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.utils.NetworkUtils.isConnected
import com.manimarank.spell4wiki.utils.SnackBarUtils.showLong
import com.manimarank.spell4wiki.utils.constants.Urls
import com.manimarank.spell4wiki.utils.NetworkUtils.executeWithNetworkCheck
import com.manimarank.spell4wiki.utils.GeneralUtils.openUrl
import com.manimarank.spell4wiki.utils.GeneralUtils.openUrlInBrowser
import com.manimarank.spell4wiki.utils.constants.AppConstants

/**
 * ViewModel for AboutComposeActivity
 * Manages about screen actions and navigation
 */
class AboutViewModel(private val context: Context) : ViewModel() {
    
    /**
     * Rate the app on Play Store
     */
    fun rateApp(context: Context) {
        executeWithNetworkCheck(context, null) {
            openUrlInBrowser(context, Urls.APP_LINK)
        }
    }
    
    /**
     * Share the app
     */
    fun shareApp(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
            val shareMessage = "${context.getString(R.string.app_name)}\n\n${context.getString(R.string.share_app_text)}\n\n${Urls.APP_LINK}"
            intent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Send feedback email
     */
    @SuppressLint("IntentReset")
    fun sendFeedback(context: Context) {
        try {
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.type = "message/rfc822"
            emailIntent.data = Uri.parse("mailto:")
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(AppConstants.CONTACT_MAIL))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name) + " App - Feedback")
            emailIntent.putExtra(Intent.EXTRA_TEXT, "\n\n-- Basic Information --\nApp Version: ${context.packageManager.getPackageInfo(context.packageName, 0).versionName}")
            context.startActivity(emailIntent)
        } catch (ex: ActivityNotFoundException) {
            // Handle case where no email client is available
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Open how to contribute page
     */
    fun openHowToContribute(context: Context) {
        executeWithNetworkCheck(context, null) {
            openUrl(context, Urls.HOW_TO_CONTRIBUTE, context.getString(R.string.how_to_contribute))
        }
    }
    
    /**
     * Open source code repository
     */
    fun openSourceCode(context: Context) {
        executeWithNetworkCheck(context, null) {
            openUrlInBrowser(context, Urls.SOURCE_CODE)
        }
    }
    
    /**
     * Open help development page
     */
    fun openHelpDevelopment(context: Context) {
        executeWithNetworkCheck(context, null) {
            // Open donation page in external browser to avoid GitHub collector analytics issues
            openUrlInBrowser(context, Urls.HELP_DEVELOPMENT)
        }
    }
    
    /**
     * Open privacy policy
     */
    fun openPrivacyPolicy(context: Context) {
        executeWithNetworkCheck(context, null) {
            openUrlInBrowser(context, Urls.PRIVACY_POLICY)
        }
    }
    
    /**
     * Open GPL v3 license
     */
    fun openLicense(context: Context) {
        executeWithNetworkCheck(context, null) {
            openUrlInBrowser(context, Urls.GPL_V3)
        }
    }
    
    /**
     * Open help translate page
     */
    fun openHelpTranslate(context: Context) {
        executeWithNetworkCheck(context, null) {
            openUrlInBrowser(context, Urls.HELP_US_TRANSLATE)
        }
    }
    
    /**
     * Open Kaniyam Foundation website
     */
    fun openKaniyam(context: Context) {
        executeWithNetworkCheck(context, null) {
            openUrlInBrowser(context, Urls.KANIYAM)
        }
    }
    
    /**
     * Open VGLUG website
     */
    fun openVglug(context: Context) {
        executeWithNetworkCheck(context, null) {
            openUrlInBrowser(context, Urls.VGLUG)
        }
    }
    
    /**
     * Check network connectivity and show error if not connected
     */
    private fun checkNetworkAndShowError(context: Context): Boolean {
        if (!isConnected(context)) {
            // Show network error - would need a view reference for SnackBar
            return false
        }
        return true
    }
}
