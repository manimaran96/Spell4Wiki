package com.manimarank.spell4wiki.ui.compose.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.ui.compose.theme.Spell4WikiTheme
import com.manimarank.spell4wiki.ui.settings.LanguageSelectionActivity
import com.manimarank.spell4wiki.utils.EdgeToEdgeUtils
import kotlinx.coroutines.delay

/**
 * Compose version of SplashActivity
 * Provides a modern splash screen implementation using Jetpack Compose
 */
class SplashComposeActivity : ComponentActivity() {
    
    private lateinit var pref: PrefManager
    
    private val viewModel: SplashViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SplashViewModel(this@SplashComposeActivity) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize preferences
        pref = PrefManager(this)
        
        // Setup edge-to-edge display
        // EdgeToEdgeUtils.enableEdgeToEdge() // Not available in this version
        
        setContent {
            Spell4WikiTheme {
                SplashScreen(
                    viewModel = viewModel,
                    onNavigateNext = { navigateToNextScreen() },
                    onRetryConnection = { viewModel.checkConnection() }
                )
            }
        }
        
        // Start splash logic
        viewModel.startSplash()
    }
    
    private fun navigateToNextScreen() {
        val splashState = viewModel.splashState.value
        when (splashState.navigationDestination) {
            SplashState.NavigationDestination.LANGUAGE_SELECTION -> {
                val intent = Intent(this, LanguageSelectionActivity::class.java)
                startActivity(intent)
            }
            SplashState.NavigationDestination.LOGIN -> {
                com.manimarank.spell4wiki.ui.compose.migration.MigrationUtils.launchLoginActivity(this)
            }
            SplashState.NavigationDestination.MAIN -> {
                com.manimarank.spell4wiki.ui.compose.migration.MigrationUtils.launchMainActivity(this)
            }
        }
        finish()
    }
}

@Composable
fun SplashScreen(
    viewModel: SplashViewModel,
    onNavigateNext: () -> Unit,
    onRetryConnection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val splashState by viewModel.splashState.collectAsState()
    
    // Animation state
    var startAnimation by remember { mutableStateOf(false) }
    val scaleAnimation by animateFloatAsState(
        targetValue = if (startAnimation) 1.1f else 1.0f,
        animationSpec = tween(durationMillis = 1000),
        label = "splash_scale"
    )
    
    // Start animation when screen loads
    LaunchedEffect(Unit) {
        startAnimation = true
    }
    
    // Handle navigation when splash is complete
    LaunchedEffect(splashState.shouldNavigate) {
        if (splashState.shouldNavigate) {
            onNavigateNext()
        }
    }
    
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // App Logo with animation
                Image(
                    painter = painterResource(id = R.drawable.ic_spell4wiki_logo),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scaleAnimation)
                )
                
                // App Name
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
            
            // Next button (shown only when network fails)
            if (splashState.showRetryButton) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { onRetryConnection() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Retry",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/**
 * Preview function for development
 */
@Composable
fun SplashScreenPreview() {
    Spell4WikiTheme {
        // Preview implementation would go here
    }
}
