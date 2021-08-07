package com.manimarank.spell4wiki.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.android.material.snackbar.Snackbar
import com.manimarank.spell4wiki.BuildConfig
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.apis.WikimediaCommonsUtils.checkFileAvailability
import com.manimarank.spell4wiki.data.db.DBHelper
import com.manimarank.spell4wiki.data.db.entities.WordsHaveAudio
import com.manimarank.spell4wiki.ui.dialogs.RecordInfoDialog.show
import com.manimarank.spell4wiki.ui.listerners.FileAvailabilityCallback
import com.manimarank.spell4wiki.ui.recordaudio.RecordAudioActivity
import com.manimarank.spell4wiki.ui.spell4wiktionary.Spell4Wiktionary
import com.manimarank.spell4wiki.ui.spell4wiktionary.Spell4WordActivity
import com.manimarank.spell4wiki.ui.spell4wiktionary.Spell4WordListActivity
import com.manimarank.spell4wiki.ui.webui.CommonWebActivity
import com.manimarank.spell4wiki.ui.webui.CommonWebContentActivity
import com.manimarank.spell4wiki.utils.NetworkUtils.isConnected
import com.manimarank.spell4wiki.utils.ToastUtils.showLong
import com.manimarank.spell4wiki.utils.constants.AppConstants
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal
import kotlin.Exception as KotlinException

/**
 * Common utility class
 */
object GeneralUtils {

    fun checkPermissionGranted(activity: Activity?): Boolean {
        return ContextCompat.checkSelfPermission(activity!!, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    fun permissionDenied(activity: Activity?): Boolean {
        return ContextCompat.checkSelfPermission(activity!!, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
    }

    fun hideKeyboard(activity: Activity) {
        try {
            val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
            // Find the currently focused view, so we can grab the correct window token from it.
            var view = activity.currentFocus
            // If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = View(activity)
            }
            inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
        } catch (e: KotlinException) {
            e.printStackTrace()
        }
    }

    fun openUrl(context: Context, url: String?, title: String?) = try {
        if (isConnected(context)) {
            if (url?.isNotEmpty() == true) {
                val intent = Intent(context, CommonWebActivity::class.java)
                intent.putExtra(AppConstants.TITLE, title)
                intent.putExtra(AppConstants.URL, url)
                context.startActivity(intent)
            } else showLong(context.getString(R.string.check_url))
        } else showLong(context.getString(R.string.check_internet))
    } catch (e: KotlinException) {
        e.printStackTrace()
    }

    fun openUrlInBrowser(context: Context, url: String?) {
        try {
            if (url?.isNotEmpty() == true) {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        } catch (e: KotlinException) {
            showLong(context.getString(R.string.something_went_wrong))
            e.printStackTrace()
        }
    }

    fun showRecordDialog(activity: Activity, word: String?, langCode: String?) {
        if (isConnected(activity)) {
            checkFileAvailability(activity, word!!, langCode!!, object : FileAvailabilityCallback {
                override fun status(fileExist: Boolean) {
                    if (!activity.isDestroyed && !activity.isFinishing) {
                        if (fileExist) {
                            val wordsHaveAudioDao = DBHelper.getInstance(activity).appDatabase.wordsHaveAudioDao
                            wordsHaveAudioDao?.insert(WordsHaveAudio(word, langCode))
                            if (activity is Spell4Wiktionary) activity.updateList(word) else if (activity is Spell4WordListActivity) activity.updateList(word) else if (activity is Spell4WordActivity) activity.updateList(word) else if (activity is CommonWebActivity) activity.updateList(word)
                            showLong(String.format(activity.getString(R.string.audio_file_already_exist), word))
                            show(activity)
                        } else {
                            val intent = Intent(activity, RecordAudioActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                            intent.putExtra(AppConstants.WORD, word)
                            intent.putExtra(AppConstants.LANGUAGE_CODE, langCode)
                            activity.startActivityForResult(intent, AppConstants.RC_UPLOAD_DIALOG)
                        }
                    }
                }
            })
        } else showLong(activity.getString(R.string.check_internet))
    }

    fun openMarkdownUrl(activity: Activity, url: String?, title: String?) {
        val intent = Intent(activity, CommonWebContentActivity::class.java)
        intent.putExtra(AppConstants.TITLE, title)
        intent.putExtra(AppConstants.URL, url)
        activity.startActivityForResult(intent, AppConstants.RC_UPLOAD_DIALOG)
    }

    fun getPromptBuilder(activity: Activity): MaterialTapTargetPrompt.Builder {
        return MaterialTapTargetPrompt.Builder(activity)
            .setPromptFocal(RectanglePromptFocal())
            .setAnimationInterpolator(FastOutSlowInInterpolator())
            .setFocalPadding(R.dimen.show_case_focal_padding)
    }

    fun showAppSettingsPageSnackBar(view: View) {
        view.context.run {
            Snackbar.make(view, getString(R.string.permission_required), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.go_settings)) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", BuildConfig.APPLICATION_ID, null))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                .show()
        }
    }
}