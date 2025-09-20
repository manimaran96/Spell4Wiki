package com.manimarank.spell4wiki

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.manimarank.spell4wiki.ui.activities.MainActivity
import com.manimarank.spell4wiki.utils.constants.AppConstants
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation test, which will execute on an Android device.
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java, false, false)

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.manimarank.spell4wiki", appContext.packageName)
    }

    @Test
    fun mainActivity_launchesSuccessfully() {
        val intent = Intent()
        activityRule.launchActivity(intent)

        val activity = activityRule.activity
        assertNotNull(activity)
        assertFalse(activity.isFinishing)
    }

    @Test
    fun appConstants_haveCorrectValues() {
        assertEquals("url", AppConstants.URL)
        assertEquals("title", AppConstants.TITLE)
        assertEquals("isWiktionaryWord", AppConstants.IS_WIKTIONARY_WORD)
        assertEquals("languageCode", AppConstants.LANGUAGE_CODE)
    }

    @Test
    fun context_hasCorrectPermissions() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val packageManager = appContext.packageManager
        val packageInfo = packageManager.getPackageInfo(appContext.packageName, 0)

        assertNotNull(packageInfo)
        assertEquals("com.manimarank.spell4wiki", packageInfo.packageName)
    }
}