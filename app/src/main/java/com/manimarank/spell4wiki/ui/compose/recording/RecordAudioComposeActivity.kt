package com.manimarank.spell4wiki.ui.compose.recording

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.db.DBHelper
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.ui.compose.dialogs.DialogMigrationUtils.showLicenseSelectionDialog
import com.manimarank.spell4wiki.ui.compose.theme.Spell4WikiTheme
import com.manimarank.spell4wiki.utils.EdgeToEdgeUtils.setupStatusBarHandling
import com.manimarank.spell4wiki.utils.GeneralUtils
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.utils.ToastUtils.showLong
import java.io.File

/**
 * Compose implementation of RecordAudioActivity
 * Uses the centralized RecordingUIComponent for consistent recording experience
 */
class RecordAudioComposeActivity : ComponentActivity() {

    private val viewModel: RecordingViewModel by viewModels()
    private lateinit var pref: PrefManager
    
    private var langCode: String? = null
    private var word: String? = null

    // Permission launcher for audio recording
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startRecording()
        } else {
            showLong(getString(R.string.permission_denied_record_audio))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup proper status bar handling
        setupStatusBarHandling(window.decorView)
        
        // Initialize preferences and data
        initializeData()
        
        setContent {
            Spell4WikiTheme {
                RecordAudioScreen(
                    viewModel = viewModel,
                    onStartRecording = { handleStartRecording() },
                    onStopRecording = { viewModel.stopRecording(this@RecordAudioComposeActivity) },
                    onPlayPause = { viewModel.togglePlayPause() },
                    onSeekTo = { position -> viewModel.seekTo(position) },
                    onUpload = { handleUpload() },
                    onClose = { finish() },
                    onSettingsClick = { showLicenseDialog() }
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.pauseOperations()
    }

    /**
     * Initialize data from intent and setup ViewModel
     */
    private fun initializeData() {
        pref = PrefManager(this)
        
        // Get data from intent
        langCode = intent?.extras?.getString(AppConstants.LANGUAGE_CODE, pref.languageCodeSpell4WikiAll)
        word = intent?.extras?.getString(AppConstants.WORD, "")
        
        // Get language information
        val wikiLangDao = DBHelper.getInstance(applicationContext).appDatabase.wikiLangDao
        val wikiLang = wikiLangDao?.getWikiLanguageWithCode(langCode)
        val languageName = GeneralUtils.getLanguageInfo(
            applicationContext, 
            wikiLang, 
            strResId = R.string.selected_language
        )
        
        // Initialize ViewModel
        viewModel.initialize(
            context = this,
            word = word ?: "",
            languageCode = langCode ?: "",
            languageName = languageName,
            tempFilePath = getFilePath(AppConstants.AUDIO_TEMP_RECORDER_FILENAME),
            recordedFilePath = getFilePath(AppConstants.AUDIO_RECORDED_FILENAME)
        )
    }

    /**
     * Handle start recording with permission check
     */
    private fun handleStartRecording() {
        if (checkAudioPermission()) {
            viewModel.startRecording()
        } else {
            requestAudioPermission()
        }
    }

    /**
     * Handle upload process
     */
    private fun handleUpload() {
        // TODO: Implement upload logic similar to original RecordAudioActivity
        // This would include network calls, progress tracking, etc.
        viewModel.startUpload()
        
        // Simulate upload progress for now
        // In real implementation, this would be handled by upload API calls
        showLong(getString(R.string.upload_feature_coming_soon))
    }

    /**
     * Show license selection dialog
     */
    private fun showLicenseDialog() {
        showLicenseSelectionDialog {
            // License updated, could refresh UI if needed
        }
    }

    /**
     * Check if audio recording permission is granted
     */
    private fun checkAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Request audio recording permission
     */
    private fun requestAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    /**
     * Get file path for audio recording
     */
    private fun getFilePath(fileName: String): String {
        val file = File(getExternalFilesDir(AppConstants.AUDIO_MAIN_PATH), AppConstants.AUDIO_FILEPATH)
        if (!file.exists()) {
            file.mkdirs()
        }
        return file.absolutePath + "/" + fileName
    }
}

/**
 * Main recording screen composable
 */
@Composable
fun RecordAudioScreen(
    viewModel: RecordingViewModel,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onPlayPause: () -> Unit,
    onSeekTo: (Int) -> Unit,
    onUpload: () -> Unit,
    onClose: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (uiState.isUploading) {
            // Show upload progress overlay
            UploadProgressOverlay(
                uploadProgress = uiState.uploadProgress,
                fileName = uiState.fileName
            )
        } else {
            // Show main recording UI
            RecordingUIComponent(
                state = uiState,
                onStartRecording = onStartRecording,
                onStopRecording = onStopRecording,
                onPlayPause = onPlayPause,
                onSeekTo = onSeekTo,
                onUpload = onUpload,
                onClose = onClose,
                onSettingsClick = onSettingsClick
            )
        }
    }
}

/**
 * Upload progress overlay
 */
@Composable
private fun UploadProgressOverlay(
    uploadProgress: Float,
    fileName: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                progress = uploadProgress,
                modifier = Modifier.size(64.dp),
                strokeWidth = 6.dp
            )
            
            Text(
                text = stringResource(R.string.uploading),
                style = MaterialTheme.typography.headlineSmall
            )
            
            Text(
                text = fileName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            LinearProgressIndicator(
                progress = uploadProgress,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
