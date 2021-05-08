package com.manimarank.spell4wiki.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.manimaran.crash_reporter.CrashReporter.showAlertDialogForShareCrash
import com.manimaran.crash_reporter.interfaces.CrashAlertClickListener
import com.manimaran.crash_reporter.utils.CrashUtil.Companion.isHaveCrashData
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.ui.settings.LanguageSelectionActivity
import com.manimarank.spell4wiki.ui.login.LoginActivity
import com.manimarank.spell4wiki.ui.common.BaseActivity
import com.manimarank.spell4wiki.utils.NetworkUtils.isConnected
import com.manimarank.spell4wiki.utils.SnackBarUtils.showNormal
import com.manimarank.spell4wiki.utils.SyncHelper
import com.manimarank.spell4wiki.utils.makeGone
import com.manimarank.spell4wiki.utils.makeVisible
import com.manimarank.spell4wiki.utils.pref.PrefManager
import kotlinx.android.synthetic.main.activity_splash.*

/**
 * Splash screen activity
 */
class SplashActivity : BaseActivity() {
    private lateinit var pref: PrefManager
    private var isNetworkFail = false

    /**
     * Called when the activity is first created.
     */
    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.activity_splash)
        pref = PrefManager(applicationContext)
        val animation = AnimationUtils.loadAnimation(applicationContext, R.anim.zoom)
        val splash = findViewById<ImageView>(R.id.img_splash)
        splash.startAnimation(animation)
        btnNext.setOnClickListener {
            if (isConnected(applicationContext)) {
                btnNext.makeGone()
                if (isNetworkFail) {
                    loadSplash()
                } else {
                    callNextScreen()
                }
            } else showNormal(btnNext, getString(R.string.check_internet))
        }
        btnNext.makeGone()
        if (isConnected(applicationContext)) {
            loadSplash()
        } else {
            showNormal(btnNext, getString(R.string.check_internet))
            isNetworkFail = true
            btnNext.makeVisible()
        }
    }

    private fun loadSplash() {
        isNetworkFail = false
        //splash screen will be shown for 1 second
        val SPLASH_DISPLAY_TIME = 1000
        Handler().postDelayed({
            try {
                val listener: CrashAlertClickListener = object : CrashAlertClickListener {
                    override fun onOkClick() {
                        btnNext.makeVisible()
                    }

                    override fun onCancelClick() {
                        callNextScreen()
                    }
                }
                if (isHaveCrashData) {
                    showAlertDialogForShareCrash(this, listener, true)
                } else callNextScreen()
            } catch (e: Exception) {
                e.printStackTrace()
                callNextScreen()
            }
        }, SPLASH_DISPLAY_TIME.toLong())
    }

    private fun callNextScreen() {
        // Sync Wiki Languages
        SyncHelper().syncWikiLanguages()

        // If app launch very first time to show the language selection and app intro. Other wise go to login page
        val mainIntent = Intent(this@SplashActivity, if (pref.isFirstTimeLaunch) LanguageSelectionActivity::class.java else LoginActivity::class.java)
        startActivity(mainIntent)
        finish()
    }
}