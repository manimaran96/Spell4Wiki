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
import com.manimarank.spell4wiki.utils.GeneralUtils
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.utils.makeGone
import com.manimarank.spell4wiki.utils.makeVisible
import kotlinx.android.synthetic.main.activity_app_intro.*

class AppIntroActivity : BaseActivity() {

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
        setContentView(R.layout.activity_app_intro)

        pref = PrefManager(applicationContext)

        val appIntroDataList = list
        val tabsPagerAdapter = AppIntroTabsPagerAdapter(supportFragmentManager, appIntroDataList)
        val viewPager: ViewPager = findViewById(R.id.viewPager)
        viewPager.adapter = tabsPagerAdapter

        viewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                if (tabLayout.tabCount - 1 <= position) {
                    btnDone.makeVisible()
                    btnNext.makeGone()
                } else {
                    btnNext.makeVisible()
                    btnDone.makeGone()
                }
            }
        })

        tabLayout.setupWithViewPager(viewPager)

        btnNext.setOnClickListener {
            if (tabLayout.selectedTabPosition < tabLayout.tabCount - 1) {
                viewPager.currentItem = tabLayout.selectedTabPosition + 1
            }
        }

        btnDone.setOnClickListener {
            if (!isDoneCalled) {
                isDoneCalled = true
                // Ask required permission on done pressed
                if (!GeneralUtils.checkPermissionGranted(this) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE), AppConstants.RC_PERMISSIONS)
                } else {
                    openMainActivity()
                }
            }
        }
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
        if (requestCode == AppConstants.RC_PERMISSIONS) openMainActivity()
    }
}
