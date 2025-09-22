package com.manimarank.spell4wiki.ui.compose.recording

import android.content.Context
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.apis.ApiClient.getCommonsApi
import com.manimarank.spell4wiki.data.apis.ApiInterface
import com.manimarank.spell4wiki.data.model.WikiToken
import com.manimarank.spell4wiki.data.model.WikiUpload
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.record.ogg.WavToOggConverter
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.record.wav.WAVPlayer
import com.manimarank.spell4wiki.record.wav.WAVRecorder
import com.manimarank.spell4wiki.utils.NetworkUtils.isConnected
import com.manimarank.spell4wiki.utils.Print.log
import com.manimarank.spell4wiki.utils.Print.error
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.Locale
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

    // Upload related
    private var api: ApiInterface? = null
    private var retryCountForCsrf = 0
    private var retryCountForLogin = 0
    private var context: Context? = null
    private var pref: PrefManager? = null

    companion object {
        private const val TAG = "RecordingViewModel"
        private const val MAX_RETRIES_FOR_CSRF_TOKEN = 3
        private const val MAX_RETRIES_FOR_FORCE_LOGIN = 2
    }

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
            this@RecordingViewModel.context = context
            this@RecordingViewModel.pref = PrefManager(context)
            this@RecordingViewModel.tempRecorderFilePath = tempFilePath
            this@RecordingViewModel.recordedFilePath = recordedFilePath

            // Initialize API client
            api = getCommonsApi(context).create(ApiInterface::class.java)

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
     * Start upload process to Wiki server
     */
    fun uploadAudioToWikiServer(context: Context) {
        if (!isConnected(context)) {
            handleUploadError(context.getString(R.string.check_internet))
            return
        }

        _uiState.value = _uiState.value.copy(
            isUploading = true,
            uploadProgress = 0f
        )

        log("$TAG UPLOAD PROCESS INIT")

        if (pref?.csrfToken == null) {
            log("$TAG GETTING CSRF TOKEN")
            retryCountForCsrf++
            val call = api?.getCsrfEditToken()
            call?.enqueue(object : Callback<WikiToken?> {
                override fun onResponse(call: Call<WikiToken?>, response: Response<WikiToken?>) {
                    if (response.isSuccessful && response.body() != null) {
                        try {
                            val editToken = response.body()!!.query!!.tokenValue!!.csrfToken
                            if (editToken == AppConstants.INVALID_CSRF) {
                                pref?.csrfToken = null
                                handleUploadError(context.getString(R.string.invalid_csrf_try_again))
                            } else {
                                log("$TAG CSRF GETTING DONE")
                                pref?.csrfToken = editToken
                                completeUpload(editToken, context)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            handleUploadError("${context.getString(R.string.something_went_wrong)}\n${e.message}")
                        }
                    } else {
                        handleUploadError("${context.getString(R.string.invalid_response)}\nResponse code : ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<WikiToken?>, t: Throwable) {
                    handleUploadError("${context.getString(R.string.something_went_wrong)}\n${t.message}")
                    t.printStackTrace()
                }
            })
        } else {
            completeUpload(pref?.csrfToken, context)
        }
    }

    /**
     * Complete upload with CSRF token
     */
    private fun completeUpload(editToken: String?, context: Context) {
        val filePath = getFinalConvertedFilePath()
        if (filePath == null) {
            handleUploadError(context.getString(R.string.audio_conversion_failed))
            return
        }

        val uploadFileName = _uiState.value.fileName
        val file = File(filePath)
        val requestBody = file.asRequestBody("audio/ogg".toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("file", uploadFileName, requestBody)

        // Create content and license text
        val contentAndLicense = createContentAndLicense(context)

        // Execute the upload request
        val call = api?.uploadFile(
            uploadFileName.toRequestBody(MultipartBody.FORM),
            (editToken ?: "").toRequestBody(MultipartBody.FORM),
            filePart,
            contentAndLicense.toRequestBody(MultipartBody.FORM),
            AppConstants.UPLOAD_COMMENT.toRequestBody(MultipartBody.FORM)
        )

        call?.enqueue(object : Callback<WikiUpload?> {
            override fun onResponse(call: Call<WikiUpload?>, response: Response<WikiUpload?>) {
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val upload = response.body()!!.success
                        val result = upload?.result
                        completeUploadFinalProcess(result, context)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        handleUploadError("${context.getString(R.string.something_went_wrong)}\n${e.message}")
                    }
                } else {
                    handleUploadError("${context.getString(R.string.invalid_response)}\nResponse code : ${response.code()}")
                }
            }

            override fun onFailure(call: Call<WikiUpload?>, t: Throwable) {
                handleUploadError("${context.getString(R.string.something_went_wrong)}\n${t.message}")
                t.printStackTrace()
            }
        })
    }

    /**
     * Handle upload completion
     */
    private fun completeUploadFinalProcess(data: String?, context: Context) {
        log("$TAG COMPLETE UPLOAD FINAL PROCESS $data")
        when (data?.lowercase(Locale.ENGLISH)) {
            AppConstants.UPLOAD_SUCCESS -> {
                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    uploadProgress = 1f
                )
                // Show success message
            }
            AppConstants.UPLOAD_FILE_EXIST,
            AppConstants.UPLOAD_FILE_EXIST_FORBIDDEN,
            AppConstants.UPLOAD_WARNING -> {
                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    uploadProgress = 1f
                )
                // Show file exists message
            }
            AppConstants.UPLOAD_INVALID_TOKEN -> {
                pref?.csrfToken = null
                handleUploadError(context.getString(R.string.invalid_csrf_try_again))
            }
            else -> {
                handleUploadError("${context.getString(R.string.something_went_wrong_try_again)}\n$data")
            }
        }
    }

    /**
     * Handle upload error
     */
    private fun handleUploadError(message: String) {
        error("$TAG UPLOAD FAIL MESSAGE $message")
        _uiState.value = _uiState.value.copy(
            isUploading = false,
            uploadProgress = 0f
        )
    }

    /**
     * Toggle declaration checkbox
     */
    fun toggleDeclaration(isChecked: Boolean) {
        _uiState.value = _uiState.value.copy(isDeclarationChecked = isChecked)
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
     * Convert WAV to OGG and get final file path
     */
    private fun getFinalConvertedFilePath(): String? {
        val convertedFilePath = recordedFilePath.replace(".wav", ".ogg")
        val success = WavToOggConverter().convert(recordedFilePath, convertedFilePath)
        return if (success) convertedFilePath else null
    }

    /**
     * Create content and license text for upload
     */
    private fun createContentAndLicense(context: Context): String {
        val word = _uiState.value.word
        val language = _uiState.value.language
        val licenseName = pref?.uploadAudioLicense ?: "CC0"

        return """
            == {{int:filedesc}} ==
            {{Information
            |description={{en|1=Pronunciation of "$word" in $language}}
            |date={{subst:CURRENTYEAR}}-{{subst:CURRENTMONTH}}-{{subst:CURRENTDAY2}}
            |source={{own}}
            |author=[[User:${pref?.name ?: "Anonymous"}]]
            |permission=
            |other_versions=
            }}

            == {{int:license-header}} ==
            {{$licenseName}}

            [[Category:Pronunciation]]
            [[Category:$language pronunciation]]
        """.trimIndent()
    }

    /**
     * Generate upload file name
     */
    private fun generateFileName(languageCode: String?, word: String?): String {
        if (languageCode.isNullOrEmpty() || word.isNullOrEmpty()) return ""

        // This should match the logic from getUploadName() in the original activity
        return "${languageCode}_${word}.ogg"
    }

    /**
     * Format duration in MM:SS format
     */
    private fun formatDuration(seconds: Long): String {
        return String.format(java.util.Locale.ENGLISH, "00:%02d", seconds)
    }
}
