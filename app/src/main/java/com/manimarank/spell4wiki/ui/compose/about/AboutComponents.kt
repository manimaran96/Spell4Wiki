package com.manimarank.spell4wiki.ui.compose.about

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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.manimarank.spell4wiki.R

/**
 * App Actions Section
 */
@Composable
fun AppActionsSection(
    onRateAppClick: () -> Unit,
    onShareAppClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AboutSection(
        title = stringResource(R.string.app_actions),
        modifier = modifier
    ) {
        AboutItem(
            icon = Icons.Default.Star,
            title = stringResource(R.string.rate_app),
            onClick = onRateAppClick
        )
        
        AboutItem(
            icon = Icons.Default.Share,
            title = stringResource(R.string.share),
            onClick = onShareAppClick
        )
        
        AboutItem(
            icon = Icons.Default.Email,
            title = stringResource(R.string.feedback),
            onClick = onFeedbackClick
        )
    }
}

/**
 * Development Section
 */
@Composable
fun DevelopmentSection(
    onHowToContributeClick: () -> Unit,
    onSourceCodeClick: () -> Unit,
    onContributorsClick: () -> Unit,
    onThirdPartyLibsClick: () -> Unit,
    onCreditsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AboutSection(
        title = stringResource(R.string.development),
        modifier = modifier
    ) {
        AboutItem(
            icon = Icons.Default.Build,
            title = stringResource(R.string.how_to_contribute),
            onClick = onHowToContributeClick
        )

        AboutItem(
            icon = Icons.Default.Build,
            title = stringResource(R.string.source_code),
            onClick = onSourceCodeClick
        )

        AboutItem(
            icon = Icons.Default.Person,
            title = stringResource(R.string.contributors),
            onClick = onContributorsClick
        )

        AboutItem(
            icon = Icons.Default.Info,
            title = stringResource(R.string.third_party_libraries),
            onClick = onThirdPartyLibsClick
        )

        AboutItem(
            icon = Icons.Default.Person,
            title = stringResource(R.string.credits),
            onClick = onCreditsClick
        )
    }
}

/**
 * Legal Section
 */
@Composable
fun LegalSection(
    onPrivacyPolicyClick: () -> Unit,
    onLicenseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AboutSection(
        title = stringResource(R.string.legal),
        modifier = modifier
    ) {
        AboutItem(
            icon = Icons.Default.Lock,
            title = stringResource(R.string.privacy_policy),
            onClick = onPrivacyPolicyClick
        )

        AboutItem(
            icon = Icons.Default.Info,
            title = stringResource(R.string.license),
            onClick = onLicenseClick
        )
    }
}

/**
 * Community Section
 */
@Composable
fun CommunitySection(
    onHelpTranslateClick: () -> Unit,
    onKaniyamClick: () -> Unit,
    onVglugClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AboutSection(
        title = stringResource(R.string.community),
        modifier = modifier
    ) {
        AboutItem(
            icon = Icons.Default.Settings,
            title = stringResource(R.string.help_us_translate),
            onClick = onHelpTranslateClick
        )

        AboutItem(
            icon = Icons.Default.Home,
            title = "Kaniyam Foundation",
            onClick = onKaniyamClick
        )

        AboutItem(
            icon = Icons.Default.Person,
            title = "VGLUG",
            onClick = onVglugClick
        )
    }
}

/**
 * Generic About Section
 */
@Composable
fun AboutSection(
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
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                content()
            }
        }
    }
}

/**
 * About Item with icon and title
 */
@Composable
fun AboutItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Organization Card
 */
@Composable
fun OrganizationCard(
    name: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            
            if (description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
