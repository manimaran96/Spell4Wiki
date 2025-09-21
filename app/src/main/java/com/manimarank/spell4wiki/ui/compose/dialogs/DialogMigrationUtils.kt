package com.manimarank.spell4wiki.ui.compose.dialogs

import android.app.Activity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.manimarank.spell4wiki.data.prefs.AppPref
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.ui.listerners.OnCategorySelectionListener
import com.manimarank.spell4wiki.ui.listerners.OnLanguageSelectionListener
import com.manimarank.spell4wiki.utils.GeneralUtils
import com.manimarank.spell4wiki.utils.PermissionUtils
import com.manimarank.spell4wiki.utils.constants.ListMode
import com.manimarank.spell4wiki.utils.constants.Urls

/**
 * Utility class to help migrate from traditional dialogs to Compose dialogs
 * Provides backward-compatible methods that can be used as drop-in replacements
 */
object DialogMigrationUtils {

    /**
     * Show info dialog using Compose implementation
     * Replaces CommonDialog.openInfoDialog
     */
    fun Activity.showInfoDialog(title: String, message: String) {
        if (this is FragmentActivity) {
            showComposeDialog { onDismiss ->
                InfoDialog(
                    title = title,
                    message = message,
                    onOk = {
                        AppPref.setRecordInfoShowed()
                        onDismiss()
                    },
                    onDismiss = onDismiss
                )
            }
        }
    }

    /**
     * Show run filter info dialog using Compose implementation
     * Replaces CommonDialog.openRunFilterInfoDialog
     */
    fun Activity.showRunFilterInfoDialog() {
        showInfoDialog(getString(com.manimarank.spell4wiki.R.string.run_filter_use), getString(com.manimarank.spell4wiki.R.string.run_filter_info))
    }

    /**
     * Show record info dialog using Compose implementation
     * Replaces RecordInfoDialog.show
     */
    fun Activity.showRecordInfoDialog() {
        if (!AppPref.getRecordInfoShowed() && this is FragmentActivity) {
            showComposeDialog { onDismiss ->
                RecordInfoDialog(
                    onOk = {
                        AppPref.setRecordInfoShowed()
                        onDismiss()
                    },
                    onDismiss = onDismiss
                )
            }
        }
    }

    /**
     * Show rate app dialog using Compose implementation
     * Replaces RateAppDialog.show
     */
    fun Activity.showRateAppDialog() {
        if (!AppPref.getDontShowAgain() && this is FragmentActivity) {
            // Check if should show based on launch count and time
            var lastLaunch = AppPref.getLastLaunchTimeStamp()
            if (lastLaunch <= 0) {
                lastLaunch = System.currentTimeMillis()
                AppPref.setLastLaunchTimeStamp(lastLaunch)
            }

            val launchCount = AppPref.getLaunchCount()
            if (launchCount <= AppPref.INSTANCE.MAX_LAUNCHES) {
                val duration = System.currentTimeMillis() - lastLaunch
                if (java.util.concurrent.TimeUnit.MILLISECONDS.toDays(duration) >= AppPref.INSTANCE.DAYS_UNTIL_WAIT) {
                    
                    // Update launch count
                    AppPref.setLastLaunchTimeStamp(System.currentTimeMillis())
                    AppPref.setLaunchCount(launchCount + 1)

                    showComposeDialog { onDismiss ->
                        RateAppDialog(
                            onRate = {
                                AppPref.setDontShowAgain()
                                GeneralUtils.openUrlInBrowser(this@showRateAppDialog, Urls.APP_LINK)
                                onDismiss()
                            },
                            onLater = onDismiss,
                            onNever = {
                                AppPref.setDontShowAgain()
                                onDismiss()
                            },
                            onDismiss = onDismiss
                        )
                    }
                }
            } else {
                AppPref.setDontShowAgain()
            }
        }
    }

    /**
     * Show update app dialog using Compose implementation
     * Replaces UpdateAppDialog.show
     */
    fun Activity.showUpdateAppDialog() {
        if (AppPref.checkAppUpdateAvailable(this) && this is FragmentActivity) {
            showComposeDialog { onDismiss ->
                UpdateAppDialog(
                    onUpdate = {
                        GeneralUtils.openUrlInBrowser(this@showUpdateAppDialog, Urls.APP_LINK)
                        onDismiss()
                    },
                    onLater = onDismiss,
                    onDismiss = onDismiss
                )
            }
        }
    }

    /**
     * Show confirmation back dialog using Compose implementation
     * Replaces DialogUtils.showConfirmBackDialog
     */
    fun Activity.showConfirmBackDialog(confirmAction: () -> Unit = {}) {
        if (this is FragmentActivity) {
            showComposeDialog { onDismiss ->
                BackConfirmationDialog(
                    onConfirm = {
                        confirmAction()
                        onDismiss()
                    },
                    onCancel = onDismiss,
                    onDismiss = onDismiss
                )
            }
        }
    }

    /**
     * Show notification permission dialog using Compose implementation
     * Replaces CommonDialog.showNotificationPermissionRationaleDialog
     */
    fun Activity.showNotificationPermissionDialog(
        onPermissionGranted: () -> Unit,
        onPermissionDenied: () -> Unit
    ) {
        if (this is FragmentActivity) {
            showComposeDialog { onDismiss ->
                NotificationPermissionDialog(
                    onAllow = {
                        PermissionUtils.requestNotificationPermission(this@showNotificationPermissionDialog)
                        onDismiss()
                    },
                    onDeny = {
                        onPermissionDenied()
                        onDismiss()
                    },
                    onDismiss = onDismiss
                )
            }
        }
    }

    /**
     * Show language selection bottom sheet using Compose implementation
     * Replaces LanguageSelectionFragment
     */
    fun Activity.showLanguageSelectionBottomSheet(
        callback: OnLanguageSelectionListener?,
        listMode: Int,
        preSelectedLanguageCode: String? = null
    ) {
        if (this is FragmentActivity) {
            showComposeBottomSheet { onDismiss ->
                LanguageSelectionBottomSheet(
                    listMode = listMode,
                    preSelectedLanguageCode = preSelectedLanguageCode,
                    onLanguageSelected = { langCode ->
                        callback?.onCallBackListener(langCode)
                        onDismiss()
                    },
                    onDismiss = onDismiss
                )
            }
        }
    }

    /**
     * Show category selection bottom sheet using Compose implementation
     * Replaces CategorySelectionFragment
     */
    fun Activity.showCategorySelectionBottomSheet(
        callback: OnCategorySelectionListener?,
        listMode: Int,
        preSelectedLanguageCode: String? = null,
        subTitleInfo: String? = null
    ) {
        if (this is FragmentActivity) {
            showComposeBottomSheet { onDismiss ->
                CategorySelectionBottomSheet(
                    listMode = listMode,
                    preSelectedLanguageCode = preSelectedLanguageCode,
                    subTitleInfo = subTitleInfo,
                    onCategorySelected = { category ->
                        callback?.onCallBackListener(category)
                        onDismiss()
                    },
                    onDismiss = onDismiss
                )
            }
        }
    }

    /**
     * Helper function to show a Compose dialog in a FragmentActivity
     */
    private fun FragmentActivity.showComposeDialog(
        content: @Composable (onDismiss: () -> Unit) -> Unit
    ) {
        val dialogFragment = ComposeDialogFragment { onDismiss ->
            content(onDismiss)
        }
        dialogFragment.show(supportFragmentManager, "compose_dialog")
    }

    /**
     * Helper function to show a Compose bottom sheet in a FragmentActivity
     */
    private fun FragmentActivity.showComposeBottomSheet(
        content: @Composable (onDismiss: () -> Unit) -> Unit
    ) {
        val bottomSheetFragment = ComposeBottomSheetFragment { onDismiss ->
            content(onDismiss)
        }
        bottomSheetFragment.show(supportFragmentManager, "compose_bottom_sheet")
    }
}
