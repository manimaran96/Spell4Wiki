package com.manimarank.spell4wiki.ui.compose.recording

import android.content.Context
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.record.wav.WAVPlayer
import com.manimarank.spell4wiki.record.wav.WAVRecorder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * ViewModel for managing recording UI state and audio operations
 * Centralizes all recording-related logic for reuse across different screens
 */
class RecordingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RecordingUIState())
    val uiState: StateFlow<RecordingUIState> = _uiState.asStateFlow()

    private val recorder: WAVRecorder = WAVRecorder()
    private val player: WAVPlayer = WAVPlayer()
    private var countDownTimer: CountDownTimer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var seekUpdateRunnable: Runnable? = null
    
    private var recordedSeconds: Long = 0
    private var lastProgress = 0
    
    // File paths
    private var tempRecorderFilePath: String = ""
    private var recordedFilePath: String = ""

    /**
     * Initialize the recording component with word and language information
     */
    fun initialize(
        context: Context,
        word: String,
        languageCode: String,
        languageName: String,
        tempFilePath: String,
        recordedFilePath: String
    ) {
        viewModelScope.launch {
            this@RecordingViewModel.tempRecorderFilePath = tempFilePath
            this@RecordingViewModel.recordedFilePath = recordedFilePath
            
            val pref = PrefManager(context)
            val fileName = generateFileName(languageCode, word)
            
            _uiState.value = _uiState.value.copy(
                word = word,
                language = languageName,
                fileName = fileName,
                recordingHint = context.getString(R.string.before_record)
            )
            
            setupCountDownTimer(context)
        }
    }

    /**
     * Start recording audio
     */
    fun startRecording() {
        if (!recorder.isRecording) {
            _uiState.value = _uiState.value.copy(
                isRecording = true,
                isRecorded = false
            )
            
            recorder.startRecording(tempRecorderFilePath)
            countDownTimer?.start()
            player.stopPlaying()
        }
    }

    /**
     * Stop recording audio
     */
    fun stopRecording(context: Context) {
        if (recorder.isRecording) {
            recorder.stopRecording(tempRecorderFilePath, recordedFilePath)
            countDownTimer?.cancel()
            
            _uiState.value = _uiState.value.copy(
                isRecording = false,
                isRecorded = true,
                recordingHint = context.getString(R.string.after_record)
            )
            
            player.stopPlaying()
        }
    }

    /**
     * Toggle play/pause for recorded audio
     */
    fun togglePlayPause() {
        if (!_uiState.value.isRecorded) return
        
        if (_uiState.value.isPlaying) {
            // Pause
            player.stopPlaying()
            _uiState.value = _uiState.value.copy(isPlaying = false)
            stopSeekUpdate()
        } else {
            // Play
            player.startPlaying(recordedFilePath) {
                // Play completed callback
                _uiState.value = _uiState.value.copy(
                    isPlaying = false,
                    currentPlayPosition = 0
                )
                lastProgress = 0
                stopSeekUpdate()
            }
            
            _uiState.value = _uiState.value.copy(
                isPlaying = true,
                maxPlayDuration = player.duration
            )
            
            player.seekTo(lastProgress)
            startSeekUpdate()
        }
    }

    /**
     * Seek to specific position in audio
     */
    fun seekTo(position: Int) {
        if (_uiState.value.isRecorded) {
            player.seekTo(position)
            lastProgress = position
            _uiState.value = _uiState.value.copy(currentPlayPosition = position)
        }
    }

    /**
     * Start upload process
     */
    fun startUpload() {
        _uiState.value = _uiState.value.copy(
            isUploading = true,
            uploadProgress = 0f
        )
    }

    /**
     * Update upload progress
     */
    fun updateUploadProgress(progress: Float) {
        _uiState.value = _uiState.value.copy(uploadProgress = progress)
    }

    /**
     * Complete upload process
     */
    fun completeUpload() {
        _uiState.value = _uiState.value.copy(
            isUploading = false,
            uploadProgress = 1f
        )
    }

    /**
     * Handle upload error
     */
    fun handleUploadError() {
        _uiState.value = _uiState.value.copy(
            isUploading = false,
            uploadProgress = 0f
        )
    }

    /**
     * Pause all audio operations (for lifecycle management)
     */
    fun pauseOperations() {
        if (_uiState.value.isPlaying) {
            togglePlayPause()
        }
        if (_uiState.value.isRecording) {
            // Note: Context needed for stopRecording, should be called from Activity
        }
    }

    /**
     * Clean up resources
     */
    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
        stopSeekUpdate()
        player.stopPlaying()
        if (recorder.isRecording) {
            recorder.stopRecording(tempRecorderFilePath, recordedFilePath)
        }
    }

    /**
     * Setup countdown timer for recording duration limit
     */
    private fun setupCountDownTimer(context: Context) {
        countDownTimer = object : CountDownTimer(AppConstants.MAX_SEC_FOR_RECORDING * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val remainingSeconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)
                recordedSeconds = AppConstants.MAX_SEC_FOR_RECORDING - remainingSeconds
                
                _uiState.value = _uiState.value.copy(
                    recordingHint = context.getString(
                        R.string.during_record,
                        formatDuration(remainingSeconds)
                    ),
                    recordedDuration = recordedSeconds
                )
            }

            override fun onFinish() {
                stopRecording(context)
            }
        }
    }

    /**
     * Start updating seek position during playback
     */
    private fun startSeekUpdate() {
        seekUpdateRunnable = object : Runnable {
            override fun run() {
                if (_uiState.value.isPlaying) {
                    val currentPosition = player.currentPosition
                    _uiState.value = _uiState.value.copy(currentPlayPosition = currentPosition)
                    lastProgress = currentPosition
                    handler.postDelayed(this, 100)
                }
            }
        }
        seekUpdateRunnable?.let { handler.post(it) }
    }

    /**
     * Stop updating seek position
     */
    private fun stopSeekUpdate() {
        seekUpdateRunnable?.let { handler.removeCallbacks(it) }
        seekUpdateRunnable = null
    }

    /**
     * Generate upload file name
     */
    private fun generateFileName(languageCode: String?, word: String?): String {
        if (languageCode.isNullOrEmpty() || word.isNullOrEmpty()) return ""
        
        // This should match the logic from getUploadName() in the original activity
        return "${languageCode}_${word}.wav"
    }

    /**
     * Format duration in MM:SS format
     */
    private fun formatDuration(seconds: Long): String {
        return String.format(java.util.Locale.ENGLISH, "00:%02d", seconds)
    }
}
