package com.manimarank.spell4wiki.ui.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.manimarank.spell4wiki.BuildConfig
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.prefs.AppPref
import com.manimarank.spell4wiki.utils.PermissionUtils

object CommonDialog {

    fun Activity.openInfoDialog(title: String, message: String) {
        val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setCancelable(false)

        builder.setPositiveButton(R.string.record_info_dialog_ok) { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        AppPref.setRecordInfoShowed()
        dialog.show()

        // Set button colors programmatically for better visibility
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
            setTextColor(ContextCompat.getColor(this@openInfoDialog, R.color.colorAccent))
            textSize = 14f
            isAllCaps = false
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
    }

    fun Activity.openRunFilterInfoDialog() {
        openInfoDialog(getString(R.string.run_filter_use) , getString(R.string.run_filter_info))
    }

    /**
     * Show notification permission dialog with proper rationale and settings navigation
     */
    fun Activity.showNotificationPermissionDialog(
        onPermissionGranted: () -> Unit = {},
        onPermissionDenied: () -> Unit = {}
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            onPermissionGranted()
            return
        }

        if (PermissionUtils.isNotificationPermissionGranted(this)) {
            onPermissionGranted()
            return
        }

        val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this, android.Manifest.permission.POST_NOTIFICATIONS
        )

        if (shouldShowRationale) {
            // Show rationale dialog
            showNotificationPermissionRationaleDialog(onPermissionGranted, onPermissionDenied)
        } else {
            // Check if permission was permanently denied
            val isFirstTime = !AppPref.hasRequestedNotificationPermission()
            if (isFirstTime) {
                // First time asking - show rationale and request
                AppPref.setNotificationPermissionRequested()
                showNotificationPermissionRationaleDialog(onPermissionGranted, onPermissionDenied)
            } else {
                // Permission permanently denied - show settings dialog
                showNotificationPermissionSettingsDialog(onPermissionDenied)
            }
        }
    }

    private fun Activity.showNotificationPermissionRationaleDialog(
        onPermissionGranted: () -> Unit,
        onPermissionDenied: () -> Unit
    ) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.notification_permission_title)
        builder.setMessage(R.string.notification_permission_rationale)
        builder.setCancelable(false)

        builder.setPositiveButton(R.string.allow) { dialog, _ ->
            dialog.dismiss()
            PermissionUtils.requestNotificationPermission(this)
        }

        builder.setNegativeButton(R.string.deny) { dialog, _ ->
            dialog.dismiss()
            onPermissionDenied()
        }

        builder.create().show()
    }

    private fun Activity.showNotificationPermissionSettingsDialog(
        onPermissionDenied: () -> Unit
    ) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.notification_permission_denied_title)
        builder.setMessage(R.string.notification_permission_denied_message)
        builder.setCancelable(false)

        builder.setPositiveButton(R.string.go_settings) { dialog, _ ->
            dialog.dismiss()
            openAppSettings()
        }

        builder.setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.dismiss()
            onPermissionDenied()
        }

        builder.create().show()
    }

    private fun Activity.openAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to general settings
            val intent = Intent(Settings.ACTION_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

}