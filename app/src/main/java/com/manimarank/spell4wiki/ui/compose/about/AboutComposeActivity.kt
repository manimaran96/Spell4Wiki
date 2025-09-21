package com.manimarank.spell4wiki.ui.compose.about

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.manimarank.spell4wiki.BuildConfig
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.ui.compose.contributors.ContributorsComposeActivity
import com.manimarank.spell4wiki.ui.about.ListItemActivity
import com.manimarank.spell4wiki.ui.compose.theme.Spell4WikiTheme
import com.manimarank.spell4wiki.utils.EdgeToEdgeUtils
import com.manimarank.spell4wiki.utils.constants.AppConstants

/**
 * Compose version of AboutActivity
 * Provides a modern about screen implementation using Jetpack Compose
 */
class AboutComposeActivity : ComponentActivity() {
    
    private val viewModel: AboutViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AboutViewModel(this@AboutComposeActivity) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup edge-to-edge display
        // EdgeToEdgeUtils.enableEdgeToEdge() // Not available in this version
        
        setContent {
            Spell4WikiTheme {
                AboutScreen(
                    viewModel = viewModel,
                    onBackPressed = { finish() },
                    onNavigateToContributors = {
                        startActivity(Intent(this, ContributorsComposeActivity::class.java))
                    },
                    onNavigateToThirdPartyLibs = {
                        val intent = Intent(this, ListItemActivity::class.java)
                        intent.putExtra(AppConstants.TITLE, getString(R.string.third_party_libraries))
                        startActivity(intent)
                    },
                    onNavigateToCredits = {
                        val intent = Intent(this, ListItemActivity::class.java)
                        intent.putExtra(AppConstants.TITLE, getString(R.string.credits))
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    viewModel: AboutViewModel,
    onBackPressed: () -> Unit,
    onNavigateToContributors: () -> Unit,
    onNavigateToThirdPartyLibs: () -> Unit,
    onNavigateToCredits: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.about),
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App Logo and Version
                AppInfoSection()
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Support Development Section
                SupportDevelopmentSection(
                    onHelpDevelopmentClick = { viewModel.openHelpDevelopment(context) }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // App Actions Section
                AppActionsSection(
                    onRateAppClick = { viewModel.rateApp(context) },
                    onShareAppClick = { viewModel.shareApp(context) },
                    onFeedbackClick = { viewModel.sendFeedback(context) }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Development Section
                DevelopmentSection(
                    onHowToContributeClick = { viewModel.openHowToContribute(context) },
                    onSourceCodeClick = { viewModel.openSourceCode(context) },
                    onContributorsClick = onNavigateToContributors,
                    onThirdPartyLibsClick = onNavigateToThirdPartyLibs,
                    onCreditsClick = onNavigateToCredits
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Legal Section
                LegalSection(
                    onPrivacyPolicyClick = { viewModel.openPrivacyPolicy(context) },
                    onLicenseClick = { viewModel.openLicense(context) }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Community Section
                CommunitySection(
                    onHelpTranslateClick = { viewModel.openHelpTranslate(context) },
                    onKaniyamClick = { viewModel.openKaniyam(context) },
                    onVglugClick = { viewModel.openVglug(context) }
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun AppInfoSection(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_spell4wiki_logo),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "${stringResource(R.string.version)}: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Text(
            text = "${stringResource(R.string.license)}: GPLv3",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun SupportDevelopmentSection(
    onHelpDevelopmentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pulse animation for heart icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onHelpDevelopmentClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier
                    .size(24.dp)
                    .scale(scale)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = stringResource(R.string.help_development),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
