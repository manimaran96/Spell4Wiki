package com.manimaran.wikiaudio.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.manimaran.wikiaudio.BuildConfig;
import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.apis.ApiClient;
import com.manimaran.wikiaudio.apis.ApiInterface;
import com.manimaran.wikiaudio.constants.Constants;
import com.manimaran.wikiaudio.databases.DBHelper;
import com.manimaran.wikiaudio.databases.dao.WikiLangDao;
import com.manimaran.wikiaudio.databases.dao.WordsHaveAudioDao;
import com.manimaran.wikiaudio.databases.entities.WikiLang;
import com.manimaran.wikiaudio.databases.entities.WordsHaveAudio;
import com.manimaran.wikiaudio.models.WikiToken;
import com.manimaran.wikiaudio.models.WikiUpload;
import com.manimaran.wikiaudio.record.ogg.WavToOggConverter;
import com.manimaran.wikiaudio.record.wav.WAVPlayer;
import com.manimaran.wikiaudio.record.wav.WAVRecorder;
import com.manimaran.wikiaudio.utils.GeneralUtils;
import com.manimaran.wikiaudio.utils.PrefManager;
import com.manimaran.wikiaudio.utils.WikiLicense;

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


public class RecordAudioActivity extends AppCompatActivity {

    // Views
    private View layoutPopUp, layoutUploadPopUp, layoutRecordControls;
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

    private String TAG = RecordAudioActivity.class.getSimpleName();

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
        layoutPopUp = findViewById(R.id.layoutPopUp);
        layoutRecordControls = findViewById(R.id.layoutRecordControls);
        layoutUploadPopUp = findViewById(R.id.layoutUploadPopUp);
        btnClose = findViewById(R.id.btnClose);
        btnUpload = findViewById(R.id.btnUpload);
        btnRecord = findViewById(R.id.btnRecord);
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

        pref = new PrefManager(getApplicationContext());
        wikiLangDao = DBHelper.getInstance(getApplicationContext()).getAppDatabase().getWikiLangDao();
        wordsHaveAudioDao = DBHelper.getInstance(getApplicationContext()).getAppDatabase().getWordsHaveAudioDao();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey(Constants.LANGUAGE_CODE))
                langCode = bundle.getString(Constants.LANGUAGE_CODE);
            if (bundle.containsKey(Constants.WORD))
                word = bundle.getString(Constants.WORD);
        }

        if (langCode == null)
            langCode = pref.getLanguageCodeSpell4Wiki();

        api = ApiClient.getCommonsApi(getApplicationContext()).create(ApiInterface.class);
        apiWiki = ApiClient.getWiktionaryApi(getApplicationContext(), langCode).create(ApiInterface.class);

        initViews();

        txtWord.setText(word);
        WikiLang wikiLang = wikiLangDao.getWikiLanguageWithCode(langCode);
        txtLanguage.setText(("(" + wikiLang.getLocalName() + " - " + wikiLang.getName() + ")"));
        txtRecordHint.setText(getString(R.string.hint_before_record));
        txtDuration.setText(getDurationValue(0));
        checkBoxDeclaration.setText(String.format(getString(R.string.hint_declaration_note), getString(WikiLicense.licenseNameId(pref.getUploadAudioLicense()))));


        // Set 10 sec only for recording
        countDownTimer = new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                long remainingSecs = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished);
                recordedSecs = 10 - remainingSecs;
                txtRecordHint.setText(String.format(getString(R.string.hint_during_record), getDurationValue(remainingSecs)));
            }

            public void onFinish() {
                stopRecording();
            }
        };

        btnRecord.setOnTouchListener((v, event) -> {
            if (GeneralUtils.checkPermissionGranted(RecordAudioActivity.this)) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    startRecording();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    stopRecording();
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
                GeneralUtils.showToast(getApplicationContext(), getString(R.string.record_audio_not_found));
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

        btnUpload.setOnClickListener(v -> uploadAudioProcess());

        btnClose.setOnClickListener(v -> closePopUp());

    }

    private void startRecording() {
        Log.d(TAG, "Start Recording");
        isRecorded = false;
        recorder.startRecording(getFilePath(Constants.AUDIO_TEMP_RECORDER_FILENAME));
        countDownTimer.start();
        txtDuration.setText(getDurationValue(0));
        player.stopPlaying();
        // Animation for scale
        btnRecord.animate().scaleX(1.4f).scaleY(1.4f);
    }

    private void stopRecording() {
        Log.d(TAG, "Stop Recording");
        txtRecordHint.setText(getString(R.string.hint_after_record));
        if (recorder.isRecording()) {
            recorder.stopRecording(getFilePath(Constants.AUDIO_TEMP_RECORDER_FILENAME), getFilePath(Constants.AUDIO_RECORDED_FILENAME));
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
            player.startPlaying(getFilePath(Constants.AUDIO_RECORDED_FILENAME), () -> {
                // Play Done
                btnPlayPause.setImageResource(R.drawable.ic_play);
                isPlaying = false;
                lastProgress = 0;
                seekBar.setProgress(0);
                player.seekTo(0);
                txtDuration.setText(getDurationValue(recordedSecs));
                return null;
            });

            //player.startPlaying(getFilePath(Constants.AUDIO_RECORDED_FILENAME), null);

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
        if (player != null) {
            int mCurrentPosition = player.getCurrentPosition();
            seekBar.setProgress(mCurrentPosition);
            lastProgress = mCurrentPosition;
        }
        if (mHandler != null)
            mHandler.postDelayed(runnable, 100);
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
        Snackbar.make(layoutPopUp, getString(R.string.permisstion_required), Snackbar.LENGTH_LONG)
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
                            GeneralUtils.showToast(getApplicationContext(), getString(R.string.confirm_declaration));// TODO
                    } else
                        GeneralUtils.showToast(getApplicationContext(), getString(R.string.recorded_audio_too_short));
                } else
                    GeneralUtils.showToast(getApplicationContext(), getString(R.string.record_audio_not_found));
            } else
                GeneralUtils.showToast(getApplicationContext(), getString(R.string.invalid_language));
        } else
            GeneralUtils.showToast(getApplicationContext(), getString(R.string.invalid_word));
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
            GeneralUtils.showToast(getApplicationContext(), getString(R.string.recording_under_process));
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    private void recordLayoutVisibility(boolean visible) {
        layoutRecordControls.setVisibility(visible ? View.VISIBLE : View.GONE);
        if(visible)
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

        if (pref.getCsrfToken() == null) {
            Call<WikiToken> call = api.getEditToken();
            call.enqueue(new Callback<WikiToken>() {
                @Override
                public void onResponse(@NotNull Call<WikiToken> call, @NotNull Response<WikiToken> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String editToken = response.body().getQuery().getTokenValue().getCsrfToken();
                            if (editToken.equals(Constants.INVALID_CSRF)) {
                                pref.setCsrfToken(null);
                                uploadFailed(getString(R.string.invalid_csrf_try_again));
                            } else {
                                pref.setCsrfToken(editToken);
                                completeUpload(editToken);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            uploadFailed(getString(R.string.something_went_wrong) + "\n" + e.getMessage());
                        }
                    }else {
                        uploadFailed(getString(R.string.something_went_wrong) + "\n" + response.code());
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
                RequestBody.create(MultipartBody.FORM, String.format(getString(R.string.upload_comment), uploadFileName)) // Comment
        );


        call.enqueue(new Callback<WikiUpload>() {
            @Override
            public void onResponse(@NotNull Call<WikiUpload> call, @NotNull Response<WikiUpload> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        WikiUpload wikiUpload = response.body();
                        if (wikiUpload.getSuccess() != null && wikiUpload.getSuccess().getResult() != null) {
                            completeUploadFinalProcess(wikiUpload.getSuccess().getResult());
                        } else if (wikiUpload.getError() != null && wikiUpload.getError().getCode() != null) {
                            WikiUpload.WikiError wikiError = wikiUpload.getError();
                            if (wikiError.getCode().equalsIgnoreCase(Constants.UPLOAD_FILE_EXIST) || wikiError.getCode().equalsIgnoreCase(Constants.UPLOAD_INVALID_TOKEN))
                                completeUploadFinalProcess(wikiError.getCode());
                            else
                                completeUploadFinalProcess(wikiError.getInfo());
                        } else
                            completeUploadFinalProcess("");
                    } catch (Exception e) {
                        completeUploadFinalProcess(TextUtils.isEmpty(e.getMessage()) ? "" : e.getMessage());
                        e.printStackTrace();
                    }
                } else
                    completeUploadFinalProcess(getString(R.string.invalid_response));
            }

            private void completeUploadFinalProcess(String data) {
                switch (data.toLowerCase()) {
                    case Constants.UPLOAD_SUCCESS:
                        purgeWiktionaryPage(String.format(getString(R.string.upload_success), word));
                        break;
                    case Constants.UPLOAD_FILE_EXIST:
                    case Constants.UPLOAD_WARNING:
                        purgeWiktionaryPage(getString(R.string.file_already_exist));
                        break;
                    case Constants.UPLOAD_INVALID_TOKEN:
                        pref.setCsrfToken(null);
                        uploadFailed(getString(R.string.invalid_csrf_try_again)); // TODO invalid
                        break;
                    default:
                        uploadFailed(getString(R.string.something_went_wrong_try_again) + "\n" + data);
                        break;
                }
            }

            @Override
            public void onFailure(@NotNull Call<WikiUpload> call, @NotNull Throwable t) {
                completeUploadFinalProcess(getString(R.string.upload_failed));
                t.printStackTrace();
            }
        });
    }

    private void uploadFailed(String msg) {
        recordLayoutVisibility(true);
        if (GeneralUtils.isNetworkConnected(getApplicationContext())) {
            if (pref.getCsrfToken() == null) {
                GeneralUtils.showToast(getApplicationContext(), getString(R.string.invalid_csrf_try_again));
            } else if (!TextUtils.isEmpty(msg))
                GeneralUtils.showToast(getApplicationContext(), msg);
            else
                GeneralUtils.showToast(getApplicationContext(), getString(R.string.something_went_wrong_try_again));
        } else
            GeneralUtils.showToast(getApplicationContext(), getString(R.string.check_internet));

        //txtUploadMsg.setText("Upload DONE " + msg);
    }

    private void uploadSuccess(String msg) {
        GeneralUtils.showToast(getApplicationContext(), msg);

        // Result back
        Intent resultIntent = new Intent();
        resultIntent.putExtra(Constants.WORD, word);
        setResult(Constants.RC_UPLOAD_DIALOG, resultIntent);

        closePopUp();
    }

    private void purgeWiktionaryPage(String msg) {

        // TODO write to db
        wordsHaveAudioDao.insert(new WordsHaveAudio(word, langCode));

        Call<ResponseBody> call = apiWiki.purgePage(word);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                uploadSuccess(msg);
            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                uploadSuccess(msg);
                Log.e("TAG", "PURGE RES ERR --> " + new Gson().toJson(t.getMessage()));
                t.printStackTrace();
            }
        });
    }

    // Get record file name
    private String getFilePath(String fileName) {
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), Constants.AUDIO_FILEPATH);
        if (!file.exists()) {
            if (!file.mkdirs())
                Log.d(TAG, "Not create directory!");
        }
        return file.getAbsolutePath() + "/" + fileName;
    }

    private String getRecordedFilePath() {
        new WavToOggConverter().convert(getFilePath(Constants.AUDIO_RECORDED_FILENAME), getFilePath(Constants.AUDIO_CONVERTED_FILENAME));
        return getFilePath(Constants.AUDIO_CONVERTED_FILENAME);
    }

    private String getContentAndLicense() {

        WikiLang wikiLang = wikiLangDao.getWikiLanguageWithCode(langCode);
        String currentLangDescription = getStringByLocalLang(R.string.file_content_description, langCode);
        return "== {{int:filedesc}} ==" + "\n" +

                // File summary or Information
                "{{Information" + "\n" +
                "|description=" +
                "{{en|1=" + String.format(getString(R.string.file_content_description), wikiLang.getName(), word) + "}}" +
                (TextUtils.isEmpty(currentLangDescription) ? "" : "{{" + langCode + "|1=" + String.format(currentLangDescription, wikiLang.getLocalName(), word) + "}}") +
                "\n" +
                "|source={{own}}" +
                "|author=[[User:" + pref.getName() + "|" + pref.getName() + "]]" + "\n" +
                "|date=" + getDateNow() + "\n" +
                "}}" + "\n" +

                // File License
                "== {{int:license-header}} ==" + "\n" +
                WikiLicense.getLicenseTemplateInWiki(pref.getUploadAudioLicense()) + "\n" +

                // File Category
                "[[Category:Pronunciation]]" + (langCode.equals("ta") ? "\n[[Category:Tamil pronunciation | " + word + "]]" : "");
        /*
        TODO Category may given common lang api in array list of categories then add into for loop. Given selection for category
        Category : [[Category:St. Thomas Mount]]
        [[Category:Pronunciation]]

        Template for Spell4wiki
        Template : {{Uploaded from Mobile|platform=Android|version=2.10.2~66e1539a1}}
         */
    }

    private String getDateNow() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(new Date());
    }

    public String getStringByLocalLang(int id, String locale) {
        Configuration config = new Configuration(getResources().getConfiguration());
        config.setLocale(new Locale(locale));
        String result = createConfigurationContext(config).getResources().getString(id);
        return result.equals(getString(R.string.file_content_description)) ? null : result;
    }
}
