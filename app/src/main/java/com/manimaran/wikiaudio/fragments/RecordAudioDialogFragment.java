package com.manimaran.wikiaudio.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.arthenica.mobileffmpeg.FFmpeg;
import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.constants.Constants;
import com.manimaran.wikiaudio.listerners.CallBackListener;
import com.manimaran.wikiaudio.record.wav.WAVPlayer;
import com.manimaran.wikiaudio.record.wav.WAVRecorder;
import com.manimaran.wikiaudio.utils.GeneralUtils;
import com.manimaran.wikiaudio.utils.PrefManager;
import com.manimaran.wikiaudio.apis.ApiClient;
import com.manimaran.wikiaudio.apis.ApiInterface;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecordAudioDialogFragment extends DialogFragment {

    private static final String RECORDED_FILENAME = "record.wav";
    private static final String CONVERTED_FILENAME = "record.ogg";
    private Context ctx;
    private Activity activity;

    private Boolean isContributionMode;
    private WAVRecorder recorder = new WAVRecorder();
    private WAVPlayer player = new WAVPlayer();
    private Boolean isPlaying = false, isRecorded = false;

    private ImageView btnClose, btnRecord, btnPlayPause;
    private Button btnUpload;
    private TextView txtWord, txtSec;

    private SeekBar seekBar;

    private Integer lastProgress = 0;
    private Runnable runnable;
    private Handler mHandler = new Handler();

    private ProgressDialog progressDialog;
    private PrefManager pref;

    private ApiInterface api;
    private ApiInterface apiWiki;
    private String langCode;

    private String word = "";
    private String uploadName = null;
    private CallBackListener listener;

    private String TAG = RecordAudioDialogFragment.class.getSimpleName();

    static RecordAudioDialogFragment frag = null;

    public RecordAudioDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static RecordAudioDialogFragment newInstance(String word) {
        if(frag == null){
            frag = new RecordAudioDialogFragment();
        }
        Bundle args = new Bundle();
        args.putString(Constants.WORD, word);
        frag.setArguments(args);
        return frag;
    }

    private static String getMimeType(String url) {
        String type = null;
        String extension = url.substring(url.lastIndexOf(".") + 1);
        if (!TextUtils.isEmpty(extension)) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.RecordDialogTheme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pop_up_record_ui, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setCancelable(false);

        ctx = getContext();
        activity = getActivity();
        pref = new PrefManager(ctx);
        api = ApiClient.getCommonsApi(ctx).create(ApiInterface.class);
        // TODO - change lang code
        apiWiki = ApiClient.getWiktionaryApi(ctx, pref.getContributionLangCode()).create(ApiInterface.class);

        // View init
        btnClose = view.findViewById(R.id.btnClose);
        btnUpload = view.findViewById(R.id.upload_button);
        btnRecord = view.findViewById(R.id.btnRecord);
        btnPlayPause = view.findViewById(R.id.btnPlayPause);

        txtWord = view.findViewById(R.id.txtWord);
        txtSec = view.findViewById(R.id.txtSec);
        seekBar = view.findViewById(R.id.seekBar);


        if (getArguments() != null && getArguments().containsKey(Constants.WORD)) {
            word = getArguments().getString(Constants.WORD);
            txtWord.setText(word);
        }

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                isPlaying = false;
                player.stopPlaying();
            }
        });

        // Set 10 sec only for recording
        final CountDownTimer countDowntimer = new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                long sec = millisUntilFinished / 1000;
                txtSec.setText(("00:" + ((sec + "").length() == 2 ? sec : "0" + sec)));
            }

            @SuppressLint("SetTextI18n")
            public void onFinish() {
                if (recorder.isRecording()) {
                    player.stopPlaying();
                    txtSec.setText("00:10");
                    recorder.stopRecording(getFilePath());

                    // Reverse animation
                    btnRecord.animate()
                            .setDuration(100)
                            .scaleX(1.0f)
                            .scaleY(1.0f);
                    isRecorded = true;
                    cancel();
                }
            }
        };


        btnRecord.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (GeneralUtils.checkPermissionGranted(activity)) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Log.d(TAG, "Start Recording");
                        player.stopPlaying();
                        countDowntimer.start();
                        recorder.startRecording();

                        // Animation for scale
                        btnRecord.animate()
                                .scaleX(1.4f)
                                .scaleY(1.4f);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        Log.d(TAG, "Stop Recording");
                        countDowntimer.onFinish();
                    }
                } else
                    showMsg("Please give require permissions");
                return false;
            }
        });

        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRecorded)
                    onPlayStatusChanged();
                else
                    showMsg("Please record audio first");
            }

        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (player != null && fromUser) {
                    player.seekTo(progress);
                    lastProgress = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        runnable = new Runnable() {
            @Override
            public void run() {
                seekUpdation();
            }
        };

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRecorded) {
                    uploadName = pref.getContributionLangCode() + "-" + word + ".ogg";
                    uploadAudioToWikiServer(false);
                } else
                    GeneralUtils.showToast(ctx, "Please record audio first");
            }
        });
    }

    private void uploadAudioToWikiServer(Boolean recreateEditToken) {

        progressDialog = ProgressDialog.show(activity, ctx.getString(R.string.title_upload_audio), String.format(ctx.getString(R.string.message_upload_info), uploadName), true);
        if (pref.getCsrfToken() == null || recreateEditToken || true) {

            Call<ResponseBody> call = api.getEditToken();
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        try {
                            String responseStr = response.body().string();
                            String editToken;
                            JSONObject reader;
                            JSONObject tokenJSONObject;
                            try {
                                reader = new JSONObject(responseStr);
                                tokenJSONObject = reader.getJSONObject("query").getJSONObject("tokens");
                                //noinspection SpellCheckingInspection
                                editToken = tokenJSONObject.getString("csrftoken");
                                if (editToken.equals("+\\")) {
                                    dismissDialog("You are not logged in! \nPlease login to continue.");
                                    //if (myDialog != null && myDialog.isShowing())
                                    dismiss();
                                    //GeneralUtils.logoutAlert(activity);
                                } else {
                                    pref.setCsrfToken(editToken);
                                    completeUpload(editToken);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                dismissDialog("Server misbehaved! \nPlease try again later.");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            dismissDialog("Please check your connection!");
                        }
                    }
                }

                @Override
                public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                    dismissDialog("Please check your connection!");
                    t.printStackTrace();

                }
            });
        } else
            completeUpload(pref.getCsrfToken());


    }

    private void completeUpload(String editToken) {

        String filePath = getFilePathOfOgg();
        String uploadFileName = uploadName;
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
        Call<ResponseBody> call = api.uploadFile(
                RequestBody.create(MultipartBody.FORM, "upload"), // action //RequestBody.create("upload", MultipartBody.FORM), // action
                RequestBody.create(MultipartBody.FORM, uploadFileName), // filename
                RequestBody.create(MultipartBody.FORM, editToken), // edit token
                body, // Body file
                RequestBody.create(MultipartBody.FORM, "{{PD-self}}"), // License type - /* PD-self, CC-Zero, CC-BY-SA-4.0, CC-BY-SA-3.0*/
                RequestBody.create(MultipartBody.FORM, String.format(ctx.getString(R.string.upload_comment), uploadFileName)) // Comment

        );
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                try {
                    assert response.body() != null;
                    String responseStr = response.body().string();
                    Log.e(TAG, "Adding " + responseStr);
                    JSONObject reader;
                    JSONObject uploadJSONObject;
                    try {
                        reader = new JSONObject(responseStr);
                        if (!reader.has("error")) {
                            uploadJSONObject = reader.getJSONObject("upload");
                            String result = uploadJSONObject.getString("result");
                            if (result.toLowerCase().contains("warning")) {
                                String errMsg = "File name already exist";
                                dismissDialog(errMsg);
                                writeWordToFile();
                                updateOnWikiPage(errMsg);
                            } else {
                                dismissDialog("Upload: " + result);
                                writeWordToFile();
                                updateOnWikiPage("Upload Done");
                            }
                        } else {
                            if (reader.has("error")) {
                                String errMsg = reader.getJSONObject("error").getString("info");
                                dismissDialog(errMsg);
                            } else if (reader.has("warnings")) {
                                uploadJSONObject = reader.getJSONObject("upload");
                                String result = uploadJSONObject.getString("result");
                                String errMsg = "File name already exist";
                                dismissDialog(errMsg);
                                writeWordToFile();
                                updateOnWikiPage(errMsg);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        dismissDialog("Server misbehaved! Please try again later.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    dismissDialog("Please check your connection!");
                }
            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                dismissDialog("Please check your connection!");
                t.printStackTrace();
            }
        });
    }


    private void writeWordToFile() {
        /*
         * Word already have audio
         * Add to text file
         * Then remove from list
         */
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            GeneralUtils.writeAudioWordsToFile(String.format(ctx.getString(R.string.format_file_name_words_already_have_audio), pref.getContributionLangCode()), Collections.singletonList(itemList.get(pos)));
            itemList.remove(pos);
            notifyDataSetChanged();
        }

        if (myDialog != null && myDialog.isShowing())
            myDialog.dismiss();*/
        dismiss();
    }

    private void dismissDialog(String msg) {

        if (progressDialog != null)
            progressDialog.dismiss();

        if (msg != null) {
            if (msg.contains("CSRF")) {
                //reLoginReq();
                msg += "\nPlease Logout & Login Once";
            }
            Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
        }
    }

    private void onPlayStatusChanged() {
        if (isPlaying) {
            player.stopPlaying();
            btnPlayPause.setImageResource(R.drawable.ic_play);
        } else {
            player.startPlaying(getFilePath(), new Callable() {
                @Override
                public Object call() throws Exception {
                    onPlayStatusChanged();
                    // Play Done
                    btnPlayPause.setImageResource(R.drawable.ic_play);
                    isPlaying = false;
                    lastProgress = 0;
                    seekBar.setProgress(0);
                    player.seekTo(0);
                    return null;
                }
            });

            // Play
            seekBar.setProgress(lastProgress);
            player.seekTo(lastProgress);
            seekBar.setMax(player.getDuration());
            seekUpdation();
            btnPlayPause.setImageResource(R.drawable.ic_pause);
        }

        isPlaying = !isPlaying;
    }

    private void seekUpdation() {
        if (player != null) {
            int mCurrentPosition = player.getCurrentPosition();
            seekBar.setProgress(mCurrentPosition);
            lastProgress = mCurrentPosition;
        }
        mHandler.postDelayed(runnable, 100);

    }

    private void showMsg(String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
    }

    // Get record file name
    private String getFilePath() {
        String filePath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filePath, "/Spell4Wiki/Audios");
        if (!file.exists()) {
            if (!file.mkdirs())
                Log.d(TAG, "Not create directory!");
        }
        return file.getAbsolutePath() + "/" + Constants.RECORDED_FILENAME;
    }

    private String getFilePathOfOgg() {
        String filePath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filePath, "/Spell4Wiki/Audios");
        if (!file.exists()) {
            if (!file.mkdirs())
                Log.d(TAG, "Not create directory!");
        }

        File convertedFile = new File(file.getAbsolutePath() + "/" + Constants.CONVERTED_FILENAME);
        if (convertedFile.exists()) {
            if (convertedFile.delete()) {
                System.out.println("file Deleted :" + convertedFile.getPath());
            } else {
                System.out.println("file not Deleted :" + convertedFile.getPath());
            }
        }

        String recordedFile = file.getAbsolutePath() + "/" + Constants.RECORDED_FILENAME;

        Log.e("TAG", "--------- WAV " + recordedFile);
        FFmpeg.execute("-i " + recordedFile + " -acodec libvorbis " + convertedFile.getAbsolutePath());
        Log.e("TAG", "---------");

        return file.getAbsolutePath() + "/" + Constants.CONVERTED_FILENAME;
    }

    private void updateOnWikiPage(String msg){

        Call<ResponseBody> call = apiWiki.getEditToken();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseStr = response.body().string();
                        String editToken;
                        JSONObject reader;
                        JSONObject tokenJSONObject;
                        try {
                            reader = new JSONObject(responseStr);
                            tokenJSONObject = reader.getJSONObject("query").getJSONObject("tokens");
                            //noinspection SpellCheckingInspection
                            editToken = tokenJSONObject.getString("csrftoken");
                            if (editToken.equals("+\\")) {
                                //dismissDialog("You are not logged in! \nPlease login to continue.");
                                dismiss();
                                //GeneralUtils.logoutAlert(activity);
                            } else {
                                //pref.setCsrfToken(editToken);


                                //Call<ResponseBody> call = apiWiki.editPage("edit", title, pref.getCsrfToken(), "json","");
                                Call<ResponseBody> callEdit = apiWiki.editPage("edit", word, editToken, "json","");
                                callEdit.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                                        if (response.isSuccessful() && response.body() != null) {
                                            dismissDialog(msg);
                                            Log.e(TAG, " RES " + response.toString());
                                        }
                                    }

                                    @Override
                                    public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                                        showMsg("Please check your connection!");
                                        dismissDialog(msg);
                                    }
                                });

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            dismissDialog("Server misbehaved! \nPlease try again later.");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        dismissDialog("Please check your connection!");
                    }
                }else
                    dismissDialog("Something went wrong");
            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                dismissDialog("Please check your connection!");
                t.printStackTrace();

            }
        });


    }
}