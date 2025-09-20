package com.manimarank.spell4wiki.ui.compose.intro

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.model.AppIntroData
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.ui.compose.migration.MigrationUtils
import com.manimarank.spell4wiki.ui.compose.theme.Spell4WikiTheme
import com.manimarank.spell4wiki.ui.dialogs.CommonDialog.showNotificationPermissionDialog
import com.manimarank.spell4wiki.utils.PermissionUtils
import com.manimarank.spell4wiki.utils.constants.AppConstants
import kotlinx.coroutines.launch

class AppIntroComposeActivity : ComponentActivity() {

    private lateinit var pref: PrefManager
    private var isDoneCalled = false

    private lateinit var introSlides: List<AppIntroData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pref = PrefManager(applicationContext)

        // Initialize intro slides with localized strings
        introSlides = listOf(
            AppIntroData(R.drawable.ic_spell4wiktionary, getString(R.string.app_intro_slide_1_title), getString(R.string.app_intro_slide_1_description)),
            AppIntroData(R.drawable.ic_spell4explore, getString(R.string.app_intro_slide_2_title), getString(R.string.app_intro_slide_2_description)),
            AppIntroData(R.drawable.ic_spell4word_list, getString(R.string.app_intro_slide_3_title), getString(R.string.app_intro_slide_3_description)),
            AppIntroData(R.drawable.ic_spell4word, getString(R.string.app_intro_slide_4_title), getString(R.string.app_intro_slide_4_description)),
            AppIntroData(R.drawable.ic_spell4wiktionary, getString(R.string.app_intro_slide_5_title), getString(R.string.app_intro_slide_5_description))
        )

        setContent {
            Spell4WikiTheme {
                AppIntroScreen(
                    slides = introSlides,
                    onDone = { 
                        if (!isDoneCalled) {
                            isDoneCalled = true
                            requestRequiredPermissions()
                        }
                    }
                )
            }
        }
    }

    private fun requestRequiredPermissions() {
        // First check and request audio recording permission
        if (!PermissionUtils.isAudioRecordingPermissionGranted(this) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PermissionUtils.requestAudioRecordingPermission(this)
        } else {
            // Audio permission granted or not needed, now check notification permission
            requestNotificationPermissionIfNeeded()
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        showNotificationPermissionDialog(
            onPermissionGranted = {
                openMainActivity()
            },
            onPermissionDenied = {
                openMainActivity()
            }
        )
    }

    private fun openMainActivity() {
        isDoneCalled = false
        pref.isFirstTimeLaunch = false
        MigrationUtils.launchLoginActivity(applicationContext)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            AppConstants.RC_STORAGE_AUDIO_PERMISSION -> {
                // Audio permission result - now check notification permission
                requestNotificationPermissionIfNeeded()
            }
            AppConstants.RC_PERMISSIONS -> {
                // Notification permission result - proceed to main activity
                openMainActivity()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIntroScreen(
    slides: List<AppIntroData>,
    onDone: () -> Unit,
    viewModel: AppIntroViewModel = viewModel()
) {
    val pagerState = rememberPagerState(pageCount = { slides.size })
    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                AppIntroSlide(
                    slide = slides[page],
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Bottom controls
            AppIntroControls(
                pagerState = pagerState,
                totalPages = slides.size,
                onNext = {
                    coroutineScope.launch {
                        if (pagerState.currentPage < slides.size - 1) {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                onDone = onDone
            )
        }
    }
}

@Composable
fun AppIntroSlide(
    slide: AppIntroData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        Text(
            text = slide.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 100.dp)
        )

        // Image
        Image(
            painter = painterResource(id = slide.imgId),
            contentDescription = slide.title,
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Description
        Text(
            text = slide.description,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIntroControls(
    pagerState: PagerState,
    totalPages: Int,
    onNext: () -> Unit,
    onDone: () -> Unit
) {
    val isLastPage = pagerState.currentPage == totalPages - 1

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        color = Color.White
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Page indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.Center)
            ) {
                repeat(totalPages) { index ->
                    val isSelected = index == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 12.dp else 8.dp)
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    )
                }
            }

            // Next/Done button
            if (isLastPage) {
                TextButton(
                    onClick = onDone,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Text(
                        text = stringResource(R.string.done),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp
                    )
                }
            } else {
                IconButton(
                    onClick = onNext,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_navigate_next),
                        contentDescription = "Next",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
