package com.manimarank.spell4wiki.ui.compose.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.utils.WikiLicense
import com.manimarank.spell4wiki.data.prefs.AppPref
import com.manimarank.spell4wiki.utils.GeneralUtils
import com.manimarank.spell4wiki.utils.constants.Urls

/**
 * Compose implementation of common dialogs used throughout the app
 * Replaces AlertDialog.Builder with Material Design 3 AlertDialog components
 */

/**
 * Generic confirmation dialog with customizable title, message, and actions
 */
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = stringResource(R.string.yes),
    cancelText: String = stringResource(R.string.cancel),
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit = onCancel,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text(
                    text = confirmText,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onCancel()
                    onDismiss()
                }
            ) {
                Text(
                    text = cancelText,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.87f)
                )
            }
        },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}

/**
 * Information dialog with single OK button
 */
@Composable
fun InfoDialog(
    title: String,
    message: String,
    okText: String = stringResource(R.string.record_info_dialog_ok),
    onOk: () -> Unit,
    onDismiss: () -> Unit = onOk,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onOk()
                    onDismiss()
                }
            ) {
                Text(
                    text = okText,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}

/**
 * Logout confirmation dialog
 */
@Composable
fun LogoutDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit = onCancel,
    modifier: Modifier = Modifier
) {
    ConfirmationDialog(
        title = stringResource(R.string.logout_confirmation),
        message = stringResource(R.string.logout_message),
        confirmText = stringResource(R.string.yes),
        cancelText = stringResource(R.string.no),
        onConfirm = onConfirm,
        onCancel = onCancel,
        onDismiss = onDismiss,
        modifier = modifier
    )
}

/**
 * Back confirmation dialog
 */
@Composable
fun BackConfirmationDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit = onCancel,
    modifier: Modifier = Modifier
) {
    ConfirmationDialog(
        title = stringResource(R.string.confirmation),
        message = stringResource(R.string.confirm_to_back),
        confirmText = stringResource(R.string.yes),
        cancelText = stringResource(R.string.cancel),
        onConfirm = onConfirm,
        onCancel = onCancel,
        onDismiss = onDismiss,
        modifier = modifier
    )
}

/**
 * Record info dialog
 */
@Composable
fun RecordInfoDialog(
    onOk: () -> Unit,
    onDismiss: () -> Unit = onOk,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        AppPref.setRecordInfoShowed()
    }
    
    InfoDialog(
        title = stringResource(R.string.record_info_dialog_title),
        message = stringResource(R.string.record_info_dialog_message),
        okText = stringResource(R.string.record_info_dialog_ok),
        onOk = onOk,
        onDismiss = onDismiss,
        modifier = modifier
    )
}

/**
 * Run filter info dialog
 */
@Composable
fun RunFilterInfoDialog(
    onOk: () -> Unit,
    onDismiss: () -> Unit = onOk,
    modifier: Modifier = Modifier
) {
    InfoDialog(
        title = stringResource(R.string.run_filter_use),
        message = stringResource(R.string.run_filter_info),
        onOk = onOk,
        onDismiss = onDismiss,
        modifier = modifier
    )
}

/**
 * App update dialog
 */
@Composable
fun UpdateAppDialog(
    onUpdate: () -> Unit,
    onLater: () -> Unit,
    onDismiss: () -> Unit = onLater,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        AppPref.setUpdateShowed()
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.update_dialog_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = stringResource(R.string.update_dialog_message),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onUpdate()
                    GeneralUtils.openUrlInBrowser(context, Urls.APP_LINK)
                    onDismiss()
                }
            ) {
                Text(
                    text = stringResource(R.string.update_dialog_ok),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onLater()
                    onDismiss()
                }
            ) {
                Text(
                    text = stringResource(R.string.update_dialog_no),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.87f)
                )
            }
        },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}

/**
 * Theme selection dialog
 */
@Composable
fun ThemeSelectionDialog(
    currentTheme: String,
    onThemeSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeOptions = listOf(
        stringResource(R.string.theme_light),
        stringResource(R.string.theme_dark),
        stringResource(R.string.theme_system_default)
    )
    
    val themeValues = listOf("light", "dark", "system")
    val selectedIndex = themeValues.indexOf(currentTheme).takeIf { it >= 0 } ?: 2
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.choose_theme),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                themeOptions.forEachIndexed { index, option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedIndex == index,
                            onClick = {
                                onThemeSelected(themeValues[index])
                                onDismiss()
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.cancel),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.87f)
                )
            }
        },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}

/**
 * Rate app dialog with three options
 */
@Composable
fun RateAppDialog(
    onRate: () -> Unit,
    onLater: () -> Unit,
    onNever: () -> Unit,
    onDismiss: () -> Unit = onLater,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.rta_dialog_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = stringResource(R.string.rta_dialog_message),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start
            )
        },
        confirmButton = {
            TextButton(onClick = onRate) {
                Text(
                    text = stringResource(R.string.rta_dialog_ok),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onNever) {
                    Text(
                        text = stringResource(R.string.rta_dialog_no),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onLater) {
                    Text(
                        text = stringResource(R.string.rta_dialog_cancel),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        modifier = modifier,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    )
}

/**
 * Notification permission rationale dialog
 */
@Composable
fun NotificationPermissionDialog(
    onAllow: () -> Unit,
    onDeny: () -> Unit,
    onDismiss: () -> Unit = onDeny,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.notification_permission_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = stringResource(R.string.notification_permission_rationale),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start
            )
        },
        confirmButton = {
            TextButton(onClick = onAllow) {
                Text(
                    text = stringResource(R.string.allow),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDeny) {
                Text(
                    text = stringResource(R.string.deny),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        modifier = modifier,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    )
}

/**
 * License Selection Dialog - Compose implementation
 * Replaces the traditional AlertDialog.Builder license selection dialog
 */
@Composable
fun LicenseSelectionDialog(
    currentLicense: String?,
    onLicenseSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val licenseOptions = listOf(
        WikiLicense.LicensePrefs.CC_0 to R.string.license_name_cc_zero,
        WikiLicense.LicensePrefs.CC_BY_3 to R.string.license_name_cc_by_three,
        WikiLicense.LicensePrefs.CC_BY_SA_3 to R.string.license_name_cc_by_sa_three,
        WikiLicense.LicensePrefs.CC_BY_4 to R.string.license_name_cc_by_four,
        WikiLicense.LicensePrefs.CC_BY_SA_4 to R.string.license_name_cc_by_sa_four
    )

    var selectedLicense by remember { mutableStateOf(currentLicense ?: WikiLicense.LicensePrefs.CC_0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.license_choose_alert),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(licenseOptions) { (license, nameRes) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedLicense = license
                                onLicenseSelected(license)
                                onDismiss()
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedLicense == license,
                            onClick = {
                                selectedLicense = license
                                onLicenseSelected(license)
                                onDismiss()
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(nameRes),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            // No confirm button needed as selection happens on click
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.cancel),
                    fontWeight = FontWeight.Medium
                )
            }
        },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}
