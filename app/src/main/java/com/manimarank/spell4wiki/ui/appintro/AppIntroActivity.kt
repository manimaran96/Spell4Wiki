package com.manimarank.spell4wiki.ui.appintro

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.ui.login.LoginActivity
import com.manimarank.spell4wiki.ui.common.BaseActivity
import com.manimarank.spell4wiki.data.model.AppIntroData
import com.manimarank.spell4wiki.ui.dialogs.CommonDialog.showNotificationPermissionDialog
import com.manimarank.spell4wiki.utils.GeneralUtils
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.utils.PermissionUtils
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.utils.makeGone
import com.manimarank.spell4wiki.utils.makeVisible
import com.manimarank.spell4wiki.databinding.ActivityAppIntroBinding

class AppIntroActivity : BaseActivity() {

    private lateinit var binding: ActivityAppIntroBinding
    private lateinit var pref: PrefManager
    private var isDoneCalled = false

    val list
        get() =
            listOf(
                    AppIntroData(R.drawable.ic_spell4wiktionary, getString(R.string.app_intro_slide_1_title), getString(R.string.app_intro_slide_1_description)),
                    AppIntroData(R.drawable.ic_spell4explore, getString(R.string.app_intro_slide_2_title), getString(R.string.app_intro_slide_2_description)),
                    AppIntroData(R.drawable.ic_spell4word_list, getString(R.string.app_intro_slide_3_title), getString(R.string.app_intro_slide_3_description)),
                    AppIntroData(R.drawable.ic_spell4word, getString(R.string.app_intro_slide_4_title), getString(R.string.app_intro_slide_4_description)),
                    AppIntroData(R.drawable.ic_spell4wiktionary, getString(R.string.app_intro_slide_5_title), getString(R.string.app_intro_slide_5_description))
            )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pref = PrefManager(applicationContext)

        val appIntroDataList = list
        val tabsPagerAdapter = AppIntroTabsPagerAdapter(supportFragmentManager, appIntroDataList)
        binding.viewPager.adapter = tabsPagerAdapter

        binding.viewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                if (binding.tabLayout.tabCount - 1 <= position) {
                    binding.btnDone.makeVisible()
                    binding.btnNext.makeGone()
                } else {
                    binding.btnNext.makeVisible()
                    binding.btnDone.makeGone()
                }
            }
        })

        binding.tabLayout.setupWithViewPager(binding.viewPager)

        binding.btnNext.setOnClickListener {
            if (binding.tabLayout.selectedTabPosition < binding.tabLayout.tabCount - 1) {
                binding.viewPager.currentItem = binding.tabLayout.selectedTabPosition + 1
            }
        }

        binding.btnDone.setOnClickListener {
            if (!isDoneCalled) {
                isDoneCalled = true
                // Ask required permissions on done pressed
                requestRequiredPermissions()
            }
        }
    }

    private fun requestRequiredPermissions() {
        // First check and request audio recording permission
        if (!PermissionUtils.isAudioRecordingPermissionGranted(this) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PermissionUtils.requestAudioRecordingPermission(this)
        } else {
            // Audio permission granted or not needed, now check notification permission
            requestNotificationPermissionIfNeeded()
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        showNotificationPermissionDialog(
            onPermissionGranted = {
                openMainActivity()
            },
            onPermissionDenied = {
                openMainActivity()
            }
        )
    }

    private fun openMainActivity() {
        isDoneCalled = false
        pref.isFirstTimeLaunch = false
        val intent = Intent(applicationContext, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            AppConstants.RC_STORAGE_AUDIO_PERMISSION -> {
                // Audio permission result - now check notification permission
                requestNotificationPermissionIfNeeded()
            }
            AppConstants.RC_PERMISSIONS -> {
                // Notification permission result - proceed to main activity
                openMainActivity()
            }
        }
    }
}
