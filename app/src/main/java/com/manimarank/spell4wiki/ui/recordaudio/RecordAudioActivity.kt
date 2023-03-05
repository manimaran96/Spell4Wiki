package com.manimarank.spell4wiki.ui.recordaudio

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.view.MotionEvent
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.gson.Gson
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.apis.ApiClient.getCommonsApi
import com.manimarank.spell4wiki.data.apis.ApiClient.getWiktionaryApi
import com.manimarank.spell4wiki.data.apis.ApiInterface
import com.manimarank.spell4wiki.data.auth.AccountUtils.password
import com.manimarank.spell4wiki.data.auth.AccountUtils.userName
import com.manimarank.spell4wiki.data.db.DBHelper
import com.manimarank.spell4wiki.data.db.dao.WikiLangDao
import com.manimarank.spell4wiki.data.db.dao.WordsHaveAudioDao
import com.manimarank.spell4wiki.data.db.entities.WordsHaveAudio
import com.manimarank.spell4wiki.data.model.WikiLogin
import com.manimarank.spell4wiki.data.model.WikiToken
import com.manimarank.spell4wiki.data.model.WikiUpload
import com.manimarank.spell4wiki.data.prefs.AppPref.INSTANCE.getCommonCategories
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.data.prefs.ShowCasePref
import com.manimarank.spell4wiki.data.prefs.ShowCasePref.isNotShowed
import com.manimarank.spell4wiki.data.prefs.ShowCasePref.showed
import com.manimarank.spell4wiki.record.ogg.WavToOggConverter
import com.manimarank.spell4wiki.record.wav.WAVPlayer
import com.manimarank.spell4wiki.record.wav.WAVRecorder
import com.manimarank.spell4wiki.ui.common.BaseActivity
import com.manimarank.spell4wiki.ui.dialogs.AppLanguageDialog
import com.manimarank.spell4wiki.ui.recordaudio.WikiDataUtils.getUploadName
import com.manimarank.spell4wiki.ui.settings.SettingsActivity
import com.manimarank.spell4wiki.utils.DateUtils.DF_YYYY_MM_DD
import com.manimarank.spell4wiki.utils.DateUtils.getDateToString
import com.manimarank.spell4wiki.utils.GeneralUtils
import com.manimarank.spell4wiki.utils.GeneralUtils.checkPermissionGranted
import com.manimarank.spell4wiki.utils.GeneralUtils.permissionDenied
import com.manimarank.spell4wiki.utils.GeneralUtils.showAppSettingsPageSnackBar
import com.manimarank.spell4wiki.utils.NetworkUtils.isConnected
import com.manimarank.spell4wiki.utils.Print.error
import com.manimarank.spell4wiki.utils.Print.log
import com.manimarank.spell4wiki.utils.RealPathUtil
import com.manimarank.spell4wiki.utils.ToastUtils.showLong
import com.manimarank.spell4wiki.utils.WikiLicense
import com.manimarank.spell4wiki.utils.WikiLicense.getLicenseTemplateInWiki
import com.manimarank.spell4wiki.utils.WikiLicense.licenseNameId
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.utils.constants.AppConstants.MAX_RETRIES_FOR_CSRF_TOKEN
import com.manimarank.spell4wiki.utils.constants.AppConstants.MAX_RETRIES_FOR_FORCE_LOGIN
import com.manimarank.spell4wiki.utils.constants.AppConstants.RC_EDIT_REQUEST_CODE
import com.manimarank.spell4wiki.utils.constants.AppConstants.RC_LICENCE_CHANGE
import com.manimarank.spell4wiki.utils.extensions.showLicenseChooseDialog
import kotlinx.android.synthetic.main.activity_record_audio_pop_up.*
import kotlinx.android.synthetic.main.activity_settings.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal
import java.io.File
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

class RecordAudioActivity : BaseActivity() {

    private var countDownTimer: CountDownTimer? = null
    private var recordedSecs: Long = 0
    
    private var langCode: String? = null
    private var word: String? = ""

    private var isPlaying = false
    private var isRecorded = false
    private var lastProgress = 0
    private lateinit var runnable: Runnable
    private val mHandler: Handler = Handler()
    private var retryCountForLogin = 0
    private var retryCountForCsrf = 0

    private lateinit var pref: PrefManager
    private var wikiLangDao: WikiLangDao? = null
    private var wordsHaveAudioDao: WordsHaveAudioDao? = null

    private lateinit var api: ApiInterface
    private lateinit var apiWiki: ApiInterface
    private val recorder: WAVRecorder = WAVRecorder()
    private val player: WAVPlayer = WAVPlayer()

    private val TAG = RecordAudioActivity::class.java.simpleName + " --> "
    private var showCaseShowed = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_audio_pop_up)
        init()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        // Don't close outside click
        setFinishOnTouchOutside(false)
        pref = PrefManager(this)
        wikiLangDao = DBHelper.getInstance(applicationContext).appDatabase.wikiLangDao
        wordsHaveAudioDao = DBHelper.getInstance(applicationContext).appDatabase.wordsHaveAudioDao

        langCode = intent?.extras?.getString(AppConstants.LANGUAGE_CODE, pref.languageCodeSpell4Wiki)
        word = intent?.extras?.getString(AppConstants.WORD, "")
        
        api = getCommonsApi(applicationContext).create(ApiInterface::class.java)
        apiWiki = getWiktionaryApi(applicationContext, langCode!!).create(ApiInterface::class.java)
        txtWord.text = word
        val wikiLang = wikiLangDao?.getWikiLanguageWithCode(langCode)
        txtLanguage.text = GeneralUtils.getLanguageInfo(applicationContext, wikiLang, strResId = R.string.selected_language)
        txtRecordHint.text = getString(R.string.before_record)
        txtDuration.text = getDurationValue(0)
        checkboxDeclaration.text = String.format(getString(R.string.declaration_note), getString(licenseNameId(pref.uploadAudioLicense)))



        btnSettings.setOnClickListener {
            showLicenseChooseDialog({
                checkboxDeclaration.text = String.format(getString(R.string.declaration_note), getString(licenseNameId(pref.uploadAudioLicense)))
            })

        }



//        btnSettings.setOnClickListener {
//
//            startActivityForResult(Intent(applicationContext, SettingsActivity::class.java), RC_LICENCE_CHANGE)
//        }

        // Set 10 sec only for recording
        countDownTimer = object : CountDownTimer(AppConstants.MAX_SEC_FOR_RECORDING * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val remainingSecs = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)
                recordedSecs = 10 - remainingSecs
                txtRecordHint.text = String.format(getString(R.string.during_record), getDurationValue(remainingSecs))
            }

            override fun onFinish() = stopRecording()
        }
        btnRecord.setOnTouchListener { _: View?, event: MotionEvent ->
            if (checkPermissionGranted(this@RecordAudioActivity)) {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    startRecording()
                    return@setOnTouchListener true
                } else if (event.action == MotionEvent.ACTION_UP) {
                    stopRecording()
                    return@setOnTouchListener true
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                askPermissionToRecordAudio()
            }
            false
        }
        btnPlayPause.setOnClickListener {
            if (isRecorded)
                playPauseRecordedAudio()
            else
                showLong(getString(R.string.record_audio_not_found))
        }
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    seekBar.max = TimeUnit.SECONDS.toMillis(recordedSecs).toInt()
                    player.seekTo(progress)
                    lastProgress = progress
                }
                txtDuration.text = getDurationValue(recordedSecs - TimeUnit.MILLISECONDS.toSeconds(progress.toLong()))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
        })
        runnable = Runnable { seekUpdate() }

        btnUpload.setOnClickListener {
            if (isConnected(applicationContext)) {
                if (isNotShowed(ShowCasePref.RECORD_UPLOAD_UI)) {
                    showCaseShowed = false
                    callShowCaseUI()
                } else if (!showCaseShowed) {
                    // For avoid re-click
                    showCaseShowed = true
                } else
                    uploadAudioProcess()
            } else
                showLong(getString(R.string.check_internet))
        }
        btnClose.setOnClickListener { closePopUp() }
    }

    private fun callShowCaseUI() {
        if (!isFinishing && !isDestroyed && isNotShowed(ShowCasePref.RECORD_UPLOAD_UI)) {
            showed(ShowCasePref.RECORD_UPLOAD_UI)
            MaterialTapTargetPrompt.Builder(this@RecordAudioActivity)
                .setPromptFocal(RectanglePromptFocal())
                .setAnimationInterpolator(FastOutSlowInInterpolator())
                .setFocalPadding(R.dimen.show_case_focal_padding)
                .setBackgroundColour(ContextCompat.getColor(applicationContext, R.color.show_case_bg_record_upload))
                .setTarget(R.id.btnUpload)
                .setPrimaryText(R.string.sc_t_record_upload)
                .setSecondaryText(R.string.sc_d_record_upload)
                .setClipToView(findViewById(R.id.layoutRecordControls))
                .show()
        }
    }

    private fun startRecording() {
        isRecorded = false
        recorder.startRecording(getFilePath(AppConstants.AUDIO_TEMP_RECORDER_FILENAME))
        countDownTimer?.start()
        txtDuration.text = getDurationValue(0)
        player.stopPlaying()
        // Animation for scale
        btnRecord.animate().scaleX(1.4f).scaleY(1.4f)
    }

    private fun stopRecording() {
        txtRecordHint.text = getString(R.string.after_record)
        if (recorder.isRecording) {
            recorder.stopRecording(getFilePath(AppConstants.AUDIO_TEMP_RECORDER_FILENAME), getFilePath(AppConstants.AUDIO_RECORDED_FILENAME))
            player.stopPlaying()
            txtDuration.text = getDurationValue(recordedSecs)

            // Reverse animation
            btnRecord.animate().setDuration(100).scaleX(1.0f).scaleY(1.0f)
            isRecorded = true
        }
        countDownTimer?.cancel()
    }

    private fun playPauseRecordedAudio() {
        if (isPlaying) {
            player.stopPlaying()
            btnPlayPause.setImageResource(R.drawable.ic_play)
        } else {
            player.startPlaying(
                getFilePath(AppConstants.AUDIO_RECORDED_FILENAME),
                Callable<Any?> {
                    // Play Done
                    btnPlayPause.setImageResource(R.drawable.ic_play)
                    isPlaying = false
                    lastProgress = 0
                    seekBar.progress = 0
                    player.seekTo(0)
                    txtDuration.text = getDurationValue(recordedSecs)
                    null
                })


            // Play
            seekBar.progress = lastProgress
            player.seekTo(lastProgress)
            seekBar.max = player.duration
            seekUpdate()
            btnPlayPause.setImageResource(R.drawable.ic_pause)
        }
        isPlaying = !isPlaying
    }

    private fun seekUpdate() {
        if (!isDestroyed && !isFinishing) {
            val mCurrentPosition = player.currentPosition
            seekBar.progress = mCurrentPosition
            lastProgress = mCurrentPosition
            mHandler.postDelayed(runnable, 100)
        }
    }

    override fun onResume() {
        super.onResume()
        resumeState()
    }

    override fun onPause() {
        super.onPause()
        pauseState()
    }

    private fun resumeState() {}

    private fun pauseState() {
        if (isPlaying)
            playPauseRecordedAudio()
        if (recorder.isRecording)
            stopRecording()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun askPermissionToRecordAudio() {
            if (permissionDenied(this))
                showAppSettingsPageSnackBar(layoutRecordControls)
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), AppConstants.RC_STORAGE_AUDIO_PERMISSION
            )
        }

    private fun getDurationValue(sec: Long): String {
        return String.format(Locale.ENGLISH, "00:%02d", sec)
    }

    private fun uploadAudioProcess() {
        if (!TextUtils.isEmpty(word)) {
            if (!TextUtils.isEmpty(langCode)) {
                if (isRecorded) {
                    if (recordedSecs > 1) {
                        if (checkboxDeclaration.isChecked) {
                            uploadAudioToWikiServer()
                        } else showLong(getString(R.string.confirm_declaration))
                    } else showLong(getString(R.string.recorded_audio_too_short))
                } else showLong(getString(R.string.record_audio_not_found))
            } else showLong(getString(R.string.invalid_language))
        } else showLong(getString(R.string.provide_valid_word))
    }

    private fun closePopUp() {
        if (!recorder.isRecording) {
            isRecorded = false
            isPlaying = false
            player.stopPlaying()
            finish()
        } else showLong(getString(R.string.recording_under_process))
    }

    override fun onBackPressed() {
        //super.onBackPressed();
    }

    private fun recordLayoutVisibility(visible: Boolean) {
        layoutRecordControls.visibility = if (visible) View.VISIBLE else View.GONE
        if (visible)
            btnClose.show()
        else
            btnClose.hide()
        layoutUploadPopUp.visibility = if (visible) View.GONE else View.VISIBLE

        if (!visible)
            txtUploadMsg.text = String.format(getString(R.string.message_upload_info), getUploadName(langCode, word))
    }

    private fun uploadAudioToWikiServer() {
        // Background process
        recordLayoutVisibility(false)
        log(TAG + "UPLOAD PROCESS INIT")
        if (pref.csrfToken == null) {
            log(TAG + "GETTING CSRF TOKEN")
            retryCountForCsrf++
            val call = api.getCsrfEditToken()
            call.enqueue(object : Callback<WikiToken?> {
                override fun onResponse(call: Call<WikiToken?>, response: Response<WikiToken?>) {
                    if (response.isSuccessful && response.body() != null) {
                        try {
                            val editToken = response.body()!!.query!!.tokenValue!!.csrfToken
                            if (editToken == AppConstants.INVALID_CSRF) {
                                pref.csrfToken = null
                                uploadFailed(getString(R.string.invalid_csrf_try_again))
                            } else {
                                log(TAG + "CSRF GETTING DONE")
                                pref.csrfToken = editToken
                                completeUpload(editToken)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            uploadFailed("""${getString(R.string.something_went_wrong)}
                                    ${e.message}
                                    """.trimIndent())
                        }
                    } else {
                        uploadFailed("""${getString(R.string.invalid_response)}
                            Response code : ${response.code()}
                            """.trimIndent())
                    }
                }

                override fun onFailure(call: Call<WikiToken?>, t: Throwable) {
                    uploadFailed("""${getString(R.string.something_went_wrong)}
                            ${t.message}
                            """.trimIndent())
                    t.printStackTrace()
                }
            })
        } else
            completeUpload(pref.csrfToken)
    }

    private fun completeUpload(editToken: String?) {
        val filePath = getFinalConvertedFilePath()
        val uploadFileName = getUploadName(langCode, word)
        val contentAndLicense = getCommonsContentAndLicense()
        val file = File(filePath)
        // create RequestBody instance from file
        val requestFile = RequestBody.create(MediaType.parse(getMimeType(filePath)), file)

        // MultipartBody.Part is used to send also the actual file name
        val body = MultipartBody.Part.createFormData("file", uploadFileName, requestFile)

        // finally, execute the request
        val call = api.uploadFile(
            RequestBody.create(MultipartBody.FORM, uploadFileName),  // filename
            RequestBody.create(MultipartBody.FORM, editToken ?: ""),  // edit/csrf token
            body,  // original file source
            RequestBody.create(MultipartBody.FORM, contentAndLicense),  // Text Content of the file.
            RequestBody.create(MultipartBody.FORM, AppConstants.UPLOAD_COMMENT) // Comment
        )
        log(TAG + "COMPLETE UPLOAD INIT")
        call.enqueue(object : Callback<WikiUpload?> {
            override fun onResponse(call: Call<WikiUpload?>, response: Response<WikiUpload?>) {
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val wikiUpload = response.body()
                        when {
                            wikiUpload?.success?.result != null -> {
                                completeUploadFinalProcess(wikiUpload.success?.result)
                            }
                            wikiUpload?.error?.code != null -> {
                                val wikiError = wikiUpload.error
                                error(TAG + "UPLOAD FAIL RESPONSE -- " + Gson().toJson(wikiError))
                                val uploadError = listOf(AppConstants.UPLOAD_FILE_EXIST, AppConstants.UPLOAD_FILE_EXIST_FORBIDDEN, AppConstants.UPLOAD_INVALID_TOKEN)
                                when {
                                    uploadError.map { it.toLowerCase(Locale.ENGLISH) }.contains(wikiError?.code) -> completeUploadFinalProcess(wikiError?.code)
                                    wikiError?.code?.contains("exists") == true -> completeUploadFinalProcess(AppConstants.UPLOAD_FILE_EXIST)
                                    else -> completeUploadFinalProcess(wikiError?.info)
                                }
                            }
                            else -> completeUploadFinalProcess("")
                        }
                    } catch (e: Exception) {
                        completeUploadFinalProcess(if (TextUtils.isEmpty(e.message)) "" else e.message)
                        e.printStackTrace()
                    }
                } else {
                    error(TAG + "COMPLETE UPLOAD RES ISSUE " + response.code())
                    completeUploadFinalProcess("""${getString(R.string.invalid_response)}
                            Response code : ${response.code()}
                            """.trimIndent())
                }
            }

            private fun completeUploadFinalProcess(data: String?) {
                log(TAG + "COMPLETE UPLOAD FINAL PROCESS " + data)
                when (data?.toLowerCase(Locale.ENGLISH)) {
                    AppConstants.UPLOAD_SUCCESS -> purgeWiktionaryPage(String.format(getString(R.string.upload_success), word))
                    AppConstants.UPLOAD_FILE_EXIST, AppConstants.UPLOAD_FILE_EXIST_FORBIDDEN, AppConstants.UPLOAD_WARNING -> purgeWiktionaryPage(getString(R.string.file_already_exist))
                    AppConstants.UPLOAD_INVALID_TOKEN -> {
                        pref.csrfToken = null
                        uploadFailed(getString(R.string.invalid_csrf_try_again))
                    }
                    else -> uploadFailed("""${getString(R.string.something_went_wrong_try_again)}
                        $data
                        """.trimIndent())
                }
            }

            override fun onFailure(call: Call<WikiUpload?>, t: Throwable) {
                error(TAG + "COMPLETE UPLOAD FAIL - " + t.message)
                completeUploadFinalProcess(getString(R.string.upload_failed))
                t.printStackTrace()
            }
        })
    }

    private fun getMimeType(url: String): String {
        var type: String? = null
        val extension = url.substring(url.lastIndexOf(".") + 1)
        if (!TextUtils.isEmpty(extension)) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type ?: ".ogg"
    }

    private fun uploadFailed(msg: String) {
        error(TAG + "UPLOAD FAIL MESSAGE " + msg)
        if (isConnected(applicationContext)) {
            if (pref.csrfToken == null) {
                // CSRF Invalid then get new csrf and try again
                when {
                    retryCountForCsrf < MAX_RETRIES_FOR_CSRF_TOKEN -> {
                        uploadAudioToWikiServer()
                        return
                    }
                    retryCountForLogin < MAX_RETRIES_FOR_FORCE_LOGIN -> {
                        // Same issue after the new csrf also then do force login
                        retryWithForceLogin()
                        return
                    }
                    msg.equals(getString(R.string.login_expired), ignoreCase = true) -> showLong(getString(R.string.login_expired))
                    else -> showLong(getString(R.string.invalid_csrf_try_again))
                }
            }
            else if (!TextUtils.isEmpty(msg))
                showLong(msg)
            else
                showLong(getString(R.string.something_went_wrong_try_again))
        } else
            showLong(getString(R.string.check_internet))
        recordLayoutVisibility(true)
    }

    private fun retryWithForceLogin() {
        log(TAG + "RETRY WITH FORCE LOGIN " + retryCountForLogin)
        if (retryCountForLogin < MAX_RETRIES_FOR_FORCE_LOGIN && !TextUtils.isEmpty(userName) && !TextUtils.isEmpty(password)) {
            retryCountForLogin++
            //Clear cache and login info temp
            forceLogin()
        } else {
            error(TAG + "RETRY LOGIN FAIL")
            uploadFailed(getString(R.string.login_expired))
            if (retryCountForLogin >= MAX_RETRIES_FOR_FORCE_LOGIN) {
                failWithLogout()
            }
        }
    }

    private fun failWithLogout() {
        recordLayoutVisibility(false)
        error(TAG + "RETRY LOGIN FAIL -- LOGOUT & ASK RE-LOGIN")
        pref.logoutUser()
    }

    private fun forceLogin() {
        log(TAG + "FORCE LOGIN INIT " + retryCountForLogin)
        val callLoginToken = api.loginToken
        callLoginToken.enqueue(object : Callback<WikiToken?> {
            override fun onResponse(call: Call<WikiToken?>, response: Response<WikiToken?>) {
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val loginToken = response.body()?.query?.tokenValue?.loginToken
                        /*
                         * Once getting login token then call client login api
                         */
                        val callLogin = api.clientLogin(userName, password, loginToken)
                        callLogin.enqueue(object : Callback<WikiLogin?> {
                            override fun onResponse(call: Call<WikiLogin?>, response: Response<WikiLogin?>) {
                                if (response.isSuccessful && response.body() != null) {
                                    try {
                                        val login = response.body()?.clientLogin
                                        if (login?.status != null && AppConstants.PASS == login.status) {
                                            pref.setUserSession(login.username)
                                            uploadAudioProcess()
                                        } else {
                                            retryWithForceLogin()
                                            error("$TAG LOGIN COMPLETE FAIL 1 " + Gson().toJson(response.body()))
                                        }
                                    } catch (e: Exception) {
                                        retryWithForceLogin()
                                        e.printStackTrace()
                                        error(TAG + " LOGIN COMPLETE FAIL 2 " + e.message)
                                    }
                                } else {
                                    retryWithForceLogin()
                                    error("$TAG LOGIN COMPLETE FAIL 3 $response")
                                }
                            }

                            override fun onFailure(call: Call<WikiLogin?>, t: Throwable) {
                                retryWithForceLogin()
                                error(TAG + " LOGIN COMPLETE EXCEPTION " + t.message)
                            }
                        })
                    } catch (e: Exception) {
                        retryWithForceLogin()
                        e.printStackTrace()
                        error(TAG + "LOGIN TOKEN FAIL 1 " + e.message)
                    }
                } else {
                    retryWithForceLogin()
                    error(TAG + "LOGIN TOKEN FAIL 2 " + response.toString())
                }
            }

            override fun onFailure(call: Call<WikiToken?>, t: Throwable) {
                retryWithForceLogin()
                t.printStackTrace()
                error(TAG + "LOGIN FAIL EXCEPTION " + t.message)
            }
        })
    }

    private fun uploadSuccess(msg: String) {
        log(TAG + "UPLOAD SUCCESS " + msg + " --  WORD : " + word)
        showLong(msg)

        // Result back
        val resultIntent = Intent()
        resultIntent.putExtra(AppConstants.WORD, word)
        setResult(AppConstants.RC_UPLOAD_DIALOG, resultIntent)
        closePopUp()
    }

    private fun purgeWiktionaryPage(msg: String) {
        wordsHaveAudioDao?.insert(WordsHaveAudio(word, langCode))
        val call = apiWiki.purgePage(word)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                log(TAG + "PURGE WIKTIONARY PAGE SUCCESS " + response.toString())
                uploadSuccess(msg)
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                uploadSuccess(msg)
                error(TAG + "PURGE WIKTIONARY PAGE EXCEPTION " + t.message)
                t.printStackTrace()
            }
        })
    }

    // Get record file path
    private fun getFilePath(fileName: String): String {
        val file = File(getExternalFilesDir(AppConstants.AUDIO_MAIN_PATH), AppConstants.AUDIO_FILEPATH)
        if (!file.exists()) {
            if (!file.mkdirs())
                log(TAG + "Not create directory!")
        }
        return file.absolutePath + "/" + fileName
    }

    private fun getFinalConvertedFilePath(): String {
        WavToOggConverter().convert(getFilePath(AppConstants.AUDIO_RECORDED_FILENAME), getFilePath(AppConstants.AUDIO_CONVERTED_FILENAME))
        return getFilePath(AppConstants.AUDIO_CONVERTED_FILENAME)
    }

    /**
     * Wikimedia Commons - Page information for uploaded file
     */

    // File summary or Information
    // File License
    // File Category
    private fun getCommonsContentAndLicense(): String {
        val sb:StringBuilder = StringBuilder()
        sb.append("== {{int:filedesc}} ==").append("\n")
        sb.append("{{Information").append("\n")
        sb.append(if (getFileDescription() != null) "|description=${getFileDescription()}\n" else "")
        sb.append("|source={{own}}").append("|author=[[User:${pref.name}|${pref.name}]]").append("|date=${getDateToString(DF_YYYY_MM_DD)}").append("\n")
        sb.append("}}").append("\n")
        sb.append("== {{int:license-header}} ==").append("\n")
        sb.append(getLicenseTemplateInWiki(pref.uploadAudioLicense)).append("\n")
        sb.append(getCategoryInfo())
        return sb.toString()
    }

    private fun getFileDescription(): String? {
        val sb = StringBuilder()
        try {
            val wikiLang = wikiLangDao?.getWikiLanguageWithCode(langCode)
            val enDescriptionFormat = getStringByLocalLang("en", R.string.file_content_description)
            val contributedLangDescriptionFormat = getStringByLocalLang(langCode, R.string.file_content_description)
            if (contributedLangDescriptionFormat != null) {
                if (langCode != "en" && enDescriptionFormat != null && !contributedLangDescriptionFormat.equals(
                        enDescriptionFormat,
                        ignoreCase = true
                    )
                ) sb.append("{{").append(langCode).append("|1=").append(
                    String.format(contributedLangDescriptionFormat, word)
                ).append("}}")
            }
            if (enDescriptionFormat != null) {
                sb.append("{{en|1=").append(String.format(enDescriptionFormat, word))
                    .append(getLanguageWikipediaPage(wikiLang?.name ?: "")).append("}}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return if (TextUtils.isEmpty(sb.toString())) null else sb.toString()
    }

    private fun getLanguageWikipediaPage(languageName: String): String {
        val langPage = getStringByLocalLang(langCode, R.string.language_page_in_wikipedia)
        val enLangPage = getStringByLocalLang("en", R.string.language_page_in_wikipedia)
        return if (enLangPage != null && enLangPage != langPage) {
            "([[w:$langPage|$languageName Language]])"
        } else ""
    }

    private fun getCategoryInfo(): String {
        val sb = StringBuilder()
        if (wikiLangDao?.getWikiLanguageWithCode(langCode)?.categories?.size ?: 0 > 0) {
            wikiLangDao?.getWikiLanguageWithCode(langCode)?.categories?.forEach { category ->
                if (!TextUtils.isEmpty(category))
                    sb.append("[[Category:").append(category).append("]]").append("\n")
            }
        } else {
            getCommonCategories()?.forEach { category ->
                if (!TextUtils.isEmpty(category))
                    sb.append("[[Category:").append(category).append("]]").append("\n")
            }
        }
        if (TextUtils.isEmpty(sb.toString())) {
            sb.append("[[Category:Files uploaded by spell4wiki]]").append("\n")
        }
        return sb.toString()
    }

    private fun getStringByLocalLang(locale: String?, stringRes: Int): String? {
        var result: String? = null
        try {
            val config = Configuration(resources.configuration)
            config.setLocale(Locale(locale ?: "en"))
            result = createConfigurationContext(config).resources.getString(stringRes)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return if (TextUtils.isEmpty(result)) null else result
    }

    public override fun onActivityResult(
        requestCode: Int, resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (!isDestroyed && !isFinishing) {
            if (requestCode == RC_LICENCE_CHANGE) {
                checkboxDeclaration.text = String.format(getString(R.string.declaration_note), getString(licenseNameId(pref.uploadAudioLicense)))
            }
        }
    }


}