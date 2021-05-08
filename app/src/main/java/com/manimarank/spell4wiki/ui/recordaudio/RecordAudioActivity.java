package com.manimarank.spell4wiki.ui.recordaudio;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.manimarank.spell4wiki.BuildConfig;
import com.manimarank.spell4wiki.R;
import com.manimarank.spell4wiki.ui.common.BaseActivity;
import com.manimarank.spell4wiki.apis.ApiClient;
import com.manimarank.spell4wiki.apis.ApiInterface;
import com.manimarank.spell4wiki.auth.AccountUtils;
import com.manimarank.spell4wiki.data.model.ClientLogin;
import com.manimarank.spell4wiki.data.model.WikiError;
import com.manimarank.spell4wiki.databases.DBHelper;
import com.manimarank.spell4wiki.databases.dao.WikiLangDao;
import com.manimarank.spell4wiki.databases.dao.WordsHaveAudioDao;
import com.manimarank.spell4wiki.databases.entities.WikiLang;
import com.manimarank.spell4wiki.databases.entities.WordsHaveAudio;
import com.manimarank.spell4wiki.data.model.WikiLogin;
import com.manimarank.spell4wiki.data.model.WikiToken;
import com.manimarank.spell4wiki.data.model.WikiUpload;
import com.manimarank.spell4wiki.record.ogg.WavToOggConverter;
import com.manimarank.spell4wiki.record.wav.WAVPlayer;
import com.manimarank.spell4wiki.record.wav.WAVRecorder;
import com.manimarank.spell4wiki.utils.pref.AppPref;
import com.manimarank.spell4wiki.utils.GeneralUtils;
import com.manimarank.spell4wiki.utils.NetworkUtils;
import com.manimarank.spell4wiki.utils.pref.PrefManager;
import com.manimarank.spell4wiki.utils.Print;
import com.manimarank.spell4wiki.utils.pref.ShowCasePref;
import com.manimarank.spell4wiki.utils.ToastUtils;
import com.manimarank.spell4wiki.utils.WikiLicense;
import com.manimarank.spell4wiki.utils.constants.AppConstants;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal;

import static com.manimarank.spell4wiki.utils.constants.AppConstants.MAX_RETRIES_FOR_CSRF_TOKEN;
import static com.manimarank.spell4wiki.utils.constants.AppConstants.MAX_RETRIES_FOR_FORCE_LOGIN;


public class RecordAudioActivity extends BaseActivity {

    // Views
    private View layoutUploadPopUp, layoutRecordControls;
    private ImageView btnRecord, btnPlayPause;
    private FloatingActionButton btnClose;
    private AppCompatButton btnUpload;
    private AppCompatTextView txtWord, txtLanguage, txtDuration, txtUploadMsg, txtRecordHint;
    private AppCompatCheckBox checkBoxDeclaration;
    private AppCompatSeekBar seekBar;
    private CountDownTimer countDownTimer;
    private long recordedSecs = 0;
    private PrefManager pref;
    private WordsHaveAudioDao wordsHaveAudioDao;
    private WikiLangDao wikiLangDao;
    private String langCode;
    private String word = "";
    private ApiInterface api;
    private ApiInterface apiWiki;
    private WAVRecorder recorder = new WAVRecorder();
    private WAVPlayer player = new WAVPlayer();
    private Boolean isPlaying = false, isRecorded = false;
    private Integer lastProgress = 0;
    private Runnable runnable;
    private Handler mHandler = new Handler();
    private int retryCountForLogin = 0;
    private int retryCountForCsrf = 0;

    private String TAG = RecordAudioActivity.class.getSimpleName() + " --> ";
    private Boolean showCaseShowed = true;

    private static String getMimeType(String url) {
        String type = null;
        String extension = url.substring(url.lastIndexOf(".") + 1);
        if (!TextUtils.isEmpty(extension)) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_audio_pop_up);
        init();
    }

    private void initViews() {
        // View init
        layoutRecordControls = findViewById(R.id.layoutRecordControls);
        layoutUploadPopUp = findViewById(R.id.layoutUploadPopUp);
        btnClose = findViewById(R.id.btnClose);
        btnUpload = findViewById(R.id.btnUpload);
        btnRecord = findViewById(R.id.btnRecord);
        btnRecord.requestFocus();
        btnPlayPause = findViewById(R.id.btnPlayPause);
        checkBoxDeclaration = findViewById(R.id.checkboxDeclaration);

        txtWord = findViewById(R.id.txtWord);
        txtDuration = findViewById(R.id.txtDuration);
        txtLanguage = findViewById(R.id.txtLanguage);
        txtRecordHint = findViewById(R.id.txtRecordHint);
        txtUploadMsg = findViewById(R.id.txtUploadMsg);
        seekBar = findViewById(R.id.seekBar);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        // Don't close outside click
        setFinishOnTouchOutside(false);

        pref = new PrefManager(this);
        wikiLangDao = DBHelper.getInstance(getApplicationContext()).getAppDatabase().getWikiLangDao();
        wordsHaveAudioDao = DBHelper.getInstance(getApplicationContext()).getAppDatabase().getWordsHaveAudioDao();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey(AppConstants.LANGUAGE_CODE))
                langCode = bundle.getString(AppConstants.LANGUAGE_CODE);
            if (bundle.containsKey(AppConstants.WORD))
                word = bundle.getString(AppConstants.WORD);
        }

        if (langCode == null)
            langCode = pref.getLanguageCodeSpell4Wiki();

        api = ApiClient.getCommonsApi(getApplicationContext()).create(ApiInterface.class);
        apiWiki = ApiClient.getWiktionaryApi(getApplicationContext(), langCode).create(ApiInterface.class);

        initViews();

        txtWord.setText(word);
        WikiLang wikiLang = wikiLangDao.getWikiLanguageWithCode(langCode);
        txtLanguage.setText(("(" + wikiLang.getLocalName() + " - " + wikiLang.getName() + ")"));
        txtRecordHint.setText(getString(R.string.before_record));
        txtDuration.setText(getDurationValue(0));
        checkBoxDeclaration.setText(String.format(getString(R.string.declaration_note), getString(WikiLicense.licenseNameId(pref.getUploadAudioLicense()))));


        // Set 10 sec only for recording
        countDownTimer = new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                long remainingSecs = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished);
                recordedSecs = 10 - remainingSecs;
                txtRecordHint.setText(String.format(getString(R.string.during_record), getDurationValue(remainingSecs)));
            }

            public void onFinish() {
                stopRecording();
            }
        };

        btnRecord.setOnTouchListener((v, event) -> {
            if (GeneralUtils.checkPermissionGranted(RecordAudioActivity.this)) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    startRecording();
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    stopRecording();
                    return true;
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getPermissionToRecordAudio();
            }
            return false;
        });

        btnPlayPause.setOnClickListener(view1 -> {
            if (isRecorded)
                playPauseRecordedAudio();
            else
                ToastUtils.INSTANCE.showLong(getString(R.string.record_audio_not_found));
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (player != null && fromUser) {
                    seekBar.setMax((int) TimeUnit.SECONDS.toMillis(recordedSecs));
                    player.seekTo(progress);
                    lastProgress = progress;
                }
                txtDuration.setText(getDurationValue(recordedSecs - (TimeUnit.MILLISECONDS.toSeconds(progress))));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        runnable = this::seekUpdate;

        btnUpload.setOnClickListener(v -> {
            if (NetworkUtils.INSTANCE.isConnected(getApplicationContext())) {
                if (ShowCasePref.INSTANCE.isNotShowed(ShowCasePref.RECORD_UPLOAD_UI)) {
                    showCaseShowed = false;
                    callShowCaseUI();
                } else if (!showCaseShowed) {
                    // For avoid re-click
                    showCaseShowed = true;
                } else
                    uploadAudioProcess();
            } else
                ToastUtils.INSTANCE.showLong(getString(R.string.check_internet));
        });

        btnClose.setOnClickListener(v -> closePopUp());
    }

    private void startRecording() {
        isRecorded = false;
        recorder.startRecording(getFilePath(AppConstants.AUDIO_TEMP_RECORDER_FILENAME));
        countDownTimer.start();
        txtDuration.setText(getDurationValue(0));
        player.stopPlaying();
        // Animation for scale
        btnRecord.animate().scaleX(1.4f).scaleY(1.4f);
    }

    private void stopRecording() {
        txtRecordHint.setText(getString(R.string.after_record));
        if (recorder.isRecording()) {
            recorder.stopRecording(getFilePath(AppConstants.AUDIO_TEMP_RECORDER_FILENAME), getFilePath(AppConstants.AUDIO_RECORDED_FILENAME));
            player.stopPlaying();

            txtDuration.setText(getDurationValue(recordedSecs));

            // Reverse animation
            btnRecord.animate().setDuration(100).scaleX(1.0f).scaleY(1.0f);
            isRecorded = true;
        }
        countDownTimer.cancel();
    }

    private void playPauseRecordedAudio() {
        if (isPlaying) {
            player.stopPlaying();
            btnPlayPause.setImageResource(R.drawable.ic_play);
        } else {
            player.startPlaying(getFilePath(AppConstants.AUDIO_RECORDED_FILENAME), () -> {
                // Play Done
                btnPlayPause.setImageResource(R.drawable.ic_play);
                isPlaying = false;
                lastProgress = 0;
                seekBar.setProgress(0);
                player.seekTo(0);
                txtDuration.setText(getDurationValue(recordedSecs));
                return null;
            });


            // Play
            seekBar.setProgress(lastProgress);
            player.seekTo(lastProgress);
            seekBar.setMax(player.getDuration());
            seekUpdate();
            btnPlayPause.setImageResource(R.drawable.ic_pause);
        }

        isPlaying = !isPlaying;
    }

    private void seekUpdate() {
        if (!isDestroyed() && !isFinishing()) {
            if (player != null) {
                int mCurrentPosition = player.getCurrentPosition();
                seekBar.setProgress(mCurrentPosition);
                lastProgress = mCurrentPosition;
            }
            if (mHandler != null)
                mHandler.postDelayed(runnable, 100);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseState();
    }

    private void resumeState() {

    }

    private void pauseState() {
        if (player != null && isPlaying)
            playPauseRecordedAudio();

        if (recorder != null && recorder.isRecording())
            stopRecording();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getPermissionToRecordAudio() {
        if (GeneralUtils.permissionDenied(this))
            showAppSettingsPageHint();

        requestPermissions(new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
    }

    private void showAppSettingsPageHint() {
        Snackbar.make(layoutRecordControls, getString(R.string.permission_required), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.go_settings), view -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                })
                .show();
    }

    private String getDurationValue(long sec) {
        return String.format(Locale.ENGLISH, "00:%02d", sec);
    }

    private void uploadAudioProcess() {
        if (!TextUtils.isEmpty(word)) {
            if (!TextUtils.isEmpty(langCode)) {
                if (isRecorded) {
                    if (recordedSecs > 1) {
                        if (checkBoxDeclaration.isChecked()) {
                            uploadAudioToWikiServer();
                        } else
                            ToastUtils.INSTANCE.showLong(getString(R.string.confirm_declaration));
                    } else
                        ToastUtils.INSTANCE.showLong(getString(R.string.recorded_audio_too_short));
                } else
                    ToastUtils.INSTANCE.showLong(getString(R.string.record_audio_not_found));
            } else
                ToastUtils.INSTANCE.showLong(getString(R.string.invalid_language));
        } else
            ToastUtils.INSTANCE.showLong(getString(R.string.provide_valid_word));
    }

    private String getUploadName() {
        String UPLOAD_FILE_NAME = "%s-%s.ogg";
        return String.format(UPLOAD_FILE_NAME, langCode, word);
    }

    private void closePopUp() {
        if (!recorder.isRecording()) {
            isRecorded = false;
            isPlaying = false;
            player.stopPlaying();
            finish();
        } else
            ToastUtils.INSTANCE.showLong(getString(R.string.recording_under_process));
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    private void recordLayoutVisibility(boolean visible) {
        layoutRecordControls.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (visible)
            btnClose.show();
        else
            btnClose.hide();
        layoutUploadPopUp.setVisibility(visible ? View.GONE : View.VISIBLE);
        if (!visible)
            txtUploadMsg.setText(String.format(getString(R.string.message_upload_info), getUploadName()));
    }

    private void uploadAudioToWikiServer() {
        // Background process
        recordLayoutVisibility(false);
        Print.log(TAG + "UPLOAD PROCESS INIT");
        if (pref.getCsrfToken() == null) {
            Print.log(TAG + "GETTING CSRF TOKEN");
            retryCountForCsrf++;
            Call<WikiToken> call = api.getEditToken();
            call.enqueue(new Callback<WikiToken>() {
                @Override
                public void onResponse(@NotNull Call<WikiToken> call, @NotNull Response<WikiToken> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String editToken = response.body().getQuery().getTokenValue().getCsrfToken();
                            if (editToken.equals(AppConstants.INVALID_CSRF)) {
                                pref.setCsrfToken(null);
                                uploadFailed(getString(R.string.invalid_csrf_try_again));
                            } else {
                                Print.log(TAG + "CSRF GETTING DONE");
                                pref.setCsrfToken(editToken);
                                completeUpload(editToken);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            uploadFailed(getString(R.string.something_went_wrong) + "\n" + e.getMessage());
                        }
                    } else {
                        uploadFailed(getString(R.string.invalid_response) + "\nResponse code : " + response.code());
                    }
                }

                @Override
                public void onFailure(@NotNull Call<WikiToken> call, @NotNull Throwable t) {
                    uploadFailed(getString(R.string.something_went_wrong) + "\n" + t.getMessage());
                    t.printStackTrace();
                }
            });
        } else
            completeUpload(pref.getCsrfToken());
    }

    private void completeUpload(String editToken) {

        String filePath = getRecordedFilePath();
        String uploadFileName = getUploadName();
        String contentAndLicense = getContentAndLicense();

        File file = new File(filePath);
        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse(getMimeType(filePath)),
                        file
                );

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", uploadFileName, requestFile);

        // finally, execute the request
        Call<WikiUpload> call = api.uploadFile(
                RequestBody.create(MultipartBody.FORM, uploadFileName), // filename
                RequestBody.create(MultipartBody.FORM, editToken), // edit/csrf token
                body, // original file source
                RequestBody.create(MultipartBody.FORM, contentAndLicense), // Text Content of the file.
                RequestBody.create(MultipartBody.FORM, AppConstants.UPLOAD_COMMENT) // Comment
        );


        Print.log(TAG + "COMPLETE UPLOAD INIT");
        call.enqueue(new Callback<WikiUpload>() {
            @Override
            public void onResponse(@NotNull Call<WikiUpload> call, @NotNull Response<WikiUpload> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        WikiUpload wikiUpload = response.body();
                        if (wikiUpload.getSuccess() != null && wikiUpload.getSuccess().getResult() != null) {
                            completeUploadFinalProcess(wikiUpload.getSuccess().getResult());
                        } else if (wikiUpload.getError() != null && wikiUpload.getError().getCode() != null) {
                            WikiError wikiError = wikiUpload.getError();
                            Print.error(TAG + "UPLOAD FAIL RESPONSE -- " + new Gson().toJson(wikiError));
                            if (wikiError.getCode().equalsIgnoreCase(AppConstants.UPLOAD_FILE_EXIST) || wikiError.getCode().equalsIgnoreCase(AppConstants.UPLOAD_FILE_EXIST_FORBIDDEN) || wikiError.getCode().equalsIgnoreCase(AppConstants.UPLOAD_INVALID_TOKEN))
                                completeUploadFinalProcess(wikiError.getCode());
                            else if (wikiError.getCode().contains("exists"))
                                completeUploadFinalProcess(AppConstants.UPLOAD_FILE_EXIST);
                            else
                                completeUploadFinalProcess(wikiError.getInfo());
                        } else
                            completeUploadFinalProcess("");
                    } catch (Exception e) {
                        completeUploadFinalProcess(TextUtils.isEmpty(e.getMessage()) ? "" : e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    Print.error(TAG + "COMPLETE UPLOAD RES ISSUE " + response.code());
                    completeUploadFinalProcess(getString(R.string.invalid_response) + "\nResponse code : " + response.code());
                }
            }

            private void completeUploadFinalProcess(String data) {
                Print.log(TAG + "COMPLETE UPLOAD FINAL PROCESS " + data);
                switch (data.toLowerCase()) {
                    case AppConstants.UPLOAD_SUCCESS:
                        purgeWiktionaryPage(String.format(getString(R.string.upload_success), word));
                        break;
                    case AppConstants.UPLOAD_FILE_EXIST:
                    case AppConstants.UPLOAD_FILE_EXIST_FORBIDDEN:
                    case AppConstants.UPLOAD_WARNING:
                        purgeWiktionaryPage(getString(R.string.file_already_exist));
                        break;
                    case AppConstants.UPLOAD_INVALID_TOKEN:
                        pref.setCsrfToken(null);
                        uploadFailed(getString(R.string.invalid_csrf_try_again));
                        break;
                    default:
                        uploadFailed(getString(R.string.something_went_wrong_try_again) + "\n" + data);
                        break;
                }
            }

            @Override
            public void onFailure(@NotNull Call<WikiUpload> call, @NotNull Throwable t) {
                Print.error(TAG + "COMPLETE UPLOAD FAIL - " + t.getMessage());
                completeUploadFinalProcess(getString(R.string.upload_failed));
                t.printStackTrace();
            }
        });
    }

    private void uploadFailed(String msg) {
        Print.error(TAG + "UPLOAD FAIL MESSAGE " + msg);
        if (NetworkUtils.INSTANCE.isConnected(getApplicationContext())) {
            if (pref.getCsrfToken() == null) {
                // CSRF Invalid then get new csrf and try again
                if (retryCountForCsrf < MAX_RETRIES_FOR_CSRF_TOKEN) {
                    uploadAudioToWikiServer();
                    return;
                } else if (retryCountForLogin < MAX_RETRIES_FOR_FORCE_LOGIN) { // Same issue after the new csrf also then do force login
                    retryWithForceLogin();
                    return;
                } else if (msg.equalsIgnoreCase(getString(R.string.login_expired)))
                    ToastUtils.INSTANCE.showLong(getString(R.string.login_expired));
                else
                    ToastUtils.INSTANCE.showLong(getString(R.string.invalid_csrf_try_again));
            } else if (!TextUtils.isEmpty(msg))
                ToastUtils.INSTANCE.showLong(msg);
            else
                ToastUtils.INSTANCE.showLong(getString(R.string.something_went_wrong_try_again));
        } else
            ToastUtils.INSTANCE.showLong(getString(R.string.check_internet));

        recordLayoutVisibility(true);
    }

    private void retryWithForceLogin() {
        Print.log(TAG + "RETRY WITH FORCE LOGIN " + retryCountForLogin);
        if (retryCountForLogin < MAX_RETRIES_FOR_FORCE_LOGIN && !TextUtils.isEmpty(AccountUtils.getUserName()) && !TextUtils.isEmpty(AccountUtils.getPassword())) {
            retryCountForLogin++;
            //Clear cache and login info temp
            forceLogin();
        } else {
            Print.error(TAG + "RETRY LOGIN FAIL");
            uploadFailed(getString(R.string.login_expired));
            if (retryCountForLogin >= MAX_RETRIES_FOR_FORCE_LOGIN) {
                failWithLogout();
            }
        }
    }

    private void failWithLogout() {
        recordLayoutVisibility(false);
        Print.error(TAG + "RETRY LOGIN FAIL -- LOGOUT & ASK RE-LOGIN");
        if (pref != null)
            pref.logoutUser();
    }

    private void forceLogin() {
        Print.log(TAG + "FORCE LOGIN INIT " + retryCountForLogin);
        Call<WikiToken> callLoginToken = api.getLoginToken();
        callLoginToken.enqueue(new Callback<WikiToken>() {
            @Override
            public void onResponse(@NotNull Call<WikiToken> call, @NotNull Response<WikiToken> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String loginToken = response.body().getQuery().getTokenValue().getLoginToken();
                        /*
                         * Once getting login token then call client login api
                         */
                        Call<WikiLogin> callLogin = api.clientLogin(AccountUtils.getUserName(), AccountUtils.getPassword(), loginToken);
                        callLogin.enqueue(new Callback<WikiLogin>() {
                            @Override
                            public void onResponse(@NonNull Call<WikiLogin> call, @NonNull Response<WikiLogin> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    try {
                                        ClientLogin login = response.body().getClientLogin();
                                        if (login != null && login.getStatus() != null && AppConstants.PASS.equals(login.getStatus())) {
                                            pref.setUserSession(login.getUsername());
                                            uploadAudioProcess();
                                        } else {
                                            retryWithForceLogin();
                                            Print.error(TAG + " LOGIN COMPLETE FAIL 1 " + new Gson().toJson(response.body()));
                                        }
                                    } catch (Exception e) {
                                        retryWithForceLogin();
                                        e.printStackTrace();
                                        Print.error(TAG + " LOGIN COMPLETE FAIL 2 " + e.getMessage());
                                    }
                                } else {
                                    retryWithForceLogin();
                                    Print.error(TAG + " LOGIN COMPLETE FAIL 3 " + response.toString());
                                }
                            }

                            @Override
                            public void onFailure(@NotNull Call<WikiLogin> call, @NotNull Throwable t) {
                                retryWithForceLogin();
                                Print.error(TAG + " LOGIN COMPLETE EXCEPTION " + t.getMessage());
                            }
                        });

                    } catch (Exception e) {
                        retryWithForceLogin();
                        e.printStackTrace();
                        Print.error(TAG + "LOGIN TOKEN FAIL 1 " + e.getMessage());
                    }
                } else {
                    retryWithForceLogin();
                    Print.error(TAG + "LOGIN TOKEN FAIL 2 " + response.toString());
                }
            }

            @Override
            public void onFailure(@NotNull Call<WikiToken> call, @NotNull Throwable t) {
                retryWithForceLogin();
                t.printStackTrace();
                Print.error(TAG + "LOGIN FAIL EXCEPTION " + t.getMessage());
            }
        });
    }

    private void uploadSuccess(String msg) {
        Print.log(TAG + "UPLOAD SUCCESS " + msg + " --  WORD : " + word);
        ToastUtils.INSTANCE.showLong(msg);

        // Result back
        Intent resultIntent = new Intent();
        resultIntent.putExtra(AppConstants.WORD, word);
        setResult(AppConstants.RC_UPLOAD_DIALOG, resultIntent);

        closePopUp();
    }

    private void purgeWiktionaryPage(String msg) {
        wordsHaveAudioDao.insert(new WordsHaveAudio(word, langCode));

        Call<ResponseBody> call = apiWiki.purgePage(word);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                Print.log(TAG + "PURGE WIKTIONARY PAGE SUCCESS " + response.toString());
                uploadSuccess(msg);
            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                uploadSuccess(msg);
                Print.error(TAG + "PURGE WIKTIONARY PAGE EXCEPTION " + t.getMessage());
                t.printStackTrace();
            }
        });
    }

    // Get record file name
    private String getFilePath(String fileName) {
        File file = new File(getExternalFilesDir(AppConstants.AUDIO_MAIN_PATH), AppConstants.AUDIO_FILEPATH);
        if (!file.exists()) {
            if (!file.mkdirs())
                Print.log(TAG + "Not create directory!");
        }
        return file.getAbsolutePath() + "/" + fileName;
    }

    private String getRecordedFilePath() {
        new WavToOggConverter().convert(getFilePath(AppConstants.AUDIO_RECORDED_FILENAME), getFilePath(AppConstants.AUDIO_CONVERTED_FILENAME));
        return getFilePath(AppConstants.AUDIO_CONVERTED_FILENAME);
    }

    private String getContentAndLicense() {

        return "== {{int:filedesc}} ==" + "\n" +

                // File summary or Information
                "{{Information" + "\n" +
                (getDescription() != null ? "|description=" + getDescription() + "\n" : "") +
                "|source={{own}}" +
                "|author=[[User:" + pref.getName() + "|" + pref.getName() + "]]" + "\n" +
                "|date=" + getDateNow() + "\n" +
                "}}" + "\n" +

                // File License
                "== {{int:license-header}} ==" + "\n" +
                WikiLicense.getLicenseTemplateInWiki(pref.getUploadAudioLicense()) + "\n" +

                // File Category
                getCategoryInfo();
    }

    private String getCategoryInfo() {
        StringBuilder sb = new StringBuilder();
        if (wikiLangDao.getWikiLanguageWithCode(langCode) != null && wikiLangDao.getWikiLanguageWithCode(langCode).getCategories() != null && wikiLangDao.getWikiLanguageWithCode(langCode).getCategories().size() > 0) {
            for (String category : wikiLangDao.getWikiLanguageWithCode(langCode).getCategories()) {
                if (!TextUtils.isEmpty(category))
                    sb.append("[[Category:").append(category).append("]]").append("\n");
            }
        } else {
            if (AppPref.INSTANCE.getCommonCategories() != null && AppPref.INSTANCE.getCommonCategories().size() > 0) {
                for (String category : AppPref.INSTANCE.getCommonCategories()) {
                    if (!TextUtils.isEmpty(category))
                        sb.append("[[Category:").append(category).append("]]").append("\n");
                }
            }
        }

        if (TextUtils.isEmpty(sb.toString())) {
            sb.append("[[Category:Files uploaded by spell4wiki]]").append("\n");
        }

        return sb.toString();
    }

    private String getDateNow() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(new Date());
    }

    private String getDescription() {
        StringBuilder sb = new StringBuilder();
        try {
            WikiLang wikiLang = wikiLangDao.getWikiLanguageWithCode(langCode);
            String enDescriptionFormat = getStringByLocalLang("en", R.string.file_content_description);
            String contributedLangDescriptionFormat = getStringByLocalLang(langCode, R.string.file_content_description);
            if (contributedLangDescriptionFormat != null) {
                if (!langCode.equals("en") && enDescriptionFormat != null && !contributedLangDescriptionFormat.equalsIgnoreCase(enDescriptionFormat))
                    sb.append("{{").append(langCode).append("|1=").append(String.format(contributedLangDescriptionFormat, word)).append("}}");
            }
            if (enDescriptionFormat != null) {
                sb.append("{{en|1=").append(String.format(enDescriptionFormat, word)).append(getLanguageWikipediaPage(wikiLang.getName())).append("}}");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return TextUtils.isEmpty(sb.toString()) ? null : sb.toString();
    }

    private String getLanguageWikipediaPage(String languageName){
        String langPage = getStringByLocalLang(langCode, R.string.language_page_in_wikipedia);
        String enLangPage = getStringByLocalLang("en", R.string.language_page_in_wikipedia);

        if(enLangPage != null && !enLangPage.equals(langPage)){
            return "([[w:" + langPage + "|" + languageName + " Language]])";
        }
        return "";
    }

    private String getStringByLocalLang(String locale, int stringRes) {
        String result = null;
        try {
            Configuration config = new Configuration(getResources().getConfiguration());
            config.setLocale(new Locale(locale));
            result = createConfigurationContext(config).getResources().getString(stringRes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return TextUtils.isEmpty(result) ? null : result;
    }

    private void callShowCaseUI() {
        if (!isFinishing() && !isDestroyed() && ShowCasePref.INSTANCE.isNotShowed(ShowCasePref.RECORD_UPLOAD_UI)) {
            ShowCasePref.INSTANCE.showed(ShowCasePref.RECORD_UPLOAD_UI);
            new MaterialTapTargetPrompt.Builder(RecordAudioActivity.this)
                    .setPromptFocal(new RectanglePromptFocal())
                    .setAnimationInterpolator(new FastOutSlowInInterpolator())
                    .setFocalPadding(R.dimen.show_case_focal_padding)
                    .setBackgroundColour(ContextCompat.getColor(getApplicationContext(), R.color.show_case_bg_record_upload))
                    .setTarget(R.id.btnUpload)
                    .setPrimaryText(R.string.sc_t_record_upload)
                    .setSecondaryText(R.string.sc_d_record_upload)
                    .setClipToView(findViewById(R.id.layoutRecordControls))
                    .show();
        }
    }

}
