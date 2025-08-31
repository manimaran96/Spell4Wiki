package com.manimarank.spell4wiki.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.manimarank.spell4wiki.utils.constants.AppConstants

/**
 * Utility class for handling permissions in Android 15 compatible way
 */
object PermissionUtils {

    /**
     * Check if audio recording permission is granted
     */
    fun isAudioRecordingPermissionGranted(activity: Activity): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if notification permission is granted (Android 13+)
     */
    fun isNotificationPermissionGranted(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not required for older versions
        }
    }

    /**
     * Request audio recording permission
     */
    fun requestAudioRecordingPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            AppConstants.RC_STORAGE_AUDIO_PERMISSION
        )
    }

    /**
     * Request notification permission (Android 13+)
     */
    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                AppConstants.RC_PERMISSIONS
            )
        }
    }

    /**
     * Request all required permissions for the app
     */
    fun requestAllRequiredPermissions(activity: Activity) {
        val permissionsToRequest = mutableListOf<String>()

        // Audio recording permission
        if (!isAudioRecordingPermissionGranted(activity)) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }

        // Notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && 
            !isNotificationPermissionGranted(activity)) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                permissionsToRequest.toTypedArray(),
                AppConstants.RC_PERMISSIONS
            )
        }
    }

    /**
     * Check if permission was permanently denied
     */
    fun isPermissionPermanentlyDenied(activity: Activity, permission: String): Boolean {
        return !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    /**
     * Check if audio recording permission was permanently denied
     */
    fun isAudioRecordingPermissionPermanentlyDenied(activity: Activity): Boolean {
        return isPermissionPermanentlyDenied(activity, Manifest.permission.RECORD_AUDIO)
    }
}
