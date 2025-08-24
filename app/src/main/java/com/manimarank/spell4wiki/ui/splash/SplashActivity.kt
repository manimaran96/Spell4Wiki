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
import com.manimarank.spell4wiki.data.apis.SyncHelper
import com.manimarank.spell4wiki.utils.makeGone
import com.manimarank.spell4wiki.utils.makeVisible
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.databinding.ActivitySplashBinding

/**
 * Splash screen activity
 */
class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var pref: PrefManager
    private var isNetworkFail = false

    /**
     * Called when the activity is first created.
     */
    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pref = PrefManager(applicationContext)
        val animation = AnimationUtils.loadAnimation(applicationContext, R.anim.zoom)
        binding.imgSplash.startAnimation(animation)
        binding.btnNext.setOnClickListener {
            if (isConnected(applicationContext)) {
                binding.btnNext.makeGone()
                if (isNetworkFail) {
                    loadSplash()
                } else {
                    callNextScreen()
                }
            } else showNormal(binding.btnNext, getString(R.string.check_internet))
        }
        binding.btnNext.makeGone()
        if (isConnected(applicationContext)) {
            loadSplash()
        } else {
            showNormal(binding.btnNext, getString(R.string.check_internet))
            isNetworkFail = true
            binding.btnNext.makeVisible()
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
                        binding.btnNext.makeVisible()
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
        val mainIntent = Intent(this@SplashActivity, if (pref.isFirstTimeLaunch ) LanguageSelectionActivity::class.java else LoginActivity::class.java)
        startActivity(mainIntent)
        finish()
    }
}