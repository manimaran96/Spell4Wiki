package com.manimarank.spell4wiki.ui.compose.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.manimarank.spell4wiki.R

/**
 * Language Settings Section
 */
@Composable
fun LanguageSettingsSection(
    spell4WikiLanguage: String,
    onSpell4WikiLanguageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsSection(
        title = stringResource(R.string.language),
        modifier = modifier
    ) {
        SettingsItem(
            title = stringResource(R.string.spell_4_wiki_all),
            subtitle = spell4WikiLanguage,
            onClick = onSpell4WikiLanguageClick,
            showArrow = true
        )
    }
}

/**
 * License Settings Section
 */
@Composable
fun LicenseSettingsSection(
    licenseName: String,
    legalCode: String,
    onLicenseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsSection(
        title = stringResource(R.string.license),
        modifier = modifier
    ) {
        SettingsItem(
            title = stringResource(R.string.license_of_upload_audio),
            subtitle = "$licenseName ($legalCode)",
            onClick = onLicenseClick,
            showArrow = true
        )
    }
}

/**
 * App Settings Section
 */
@Composable
fun AppSettingsSection(
    appLanguage: String,
    currentTheme: String,
    isWiktionaryCleanupEnabled: Boolean,
    onAppLanguageClick: () -> Unit,
    onThemeClick: () -> Unit,
    onWiktionaryCleanupToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsSection(
        title = stringResource(R.string.app_settings),
        modifier = modifier
    ) {
        SettingsItem(
            title = stringResource(R.string.app_language),
            subtitle = appLanguage,
            onClick = onAppLanguageClick,
            showArrow = true
        )
        
        SettingsItem(
            title = stringResource(R.string.app_theme),
            subtitle = currentTheme,
            onClick = onThemeClick,
            showArrow = true
        )
        
        SettingsItemWithSwitch(
            title = stringResource(R.string.wiktionary_cleanup),
            subtitle = stringResource(R.string.wiktionary_cleanup_description),
            isChecked = isWiktionaryCleanupEnabled,
            onCheckedChange = onWiktionaryCleanupToggle
        )
    }
}

/**
 * Run Filter Settings Section
 */
@Composable
fun RunFilterSettingsSection(
    currentCount: Int,
    onCountChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var sliderValue by remember { mutableFloatStateOf(currentCount.toFloat()) }
    
    SettingsSection(
        title = stringResource(R.string.run_filter_settings_count, currentCount),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "1",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = currentCount.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "100",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Slider(
                value = sliderValue,
                onValueChange = { 
                    sliderValue = it
                    onCountChange(it.toInt())
                },
                valueRange = 1f..100f,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Generic Settings Section
 */
@Composable
fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            content()
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Settings Item with clickable action
 */
@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    showArrow: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        
        if (showArrow) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Settings Item with Switch
 */
@Composable
fun SettingsItemWithSwitch(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}
