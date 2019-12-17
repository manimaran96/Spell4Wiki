package com.manimaran.wikiaudio.adapter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.arthenica.mobileffmpeg.FFmpeg;
import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.activity.CommonWebActivity;
import com.manimaran.wikiaudio.activity.WiktionaryWebActivity;
import com.manimaran.wikiaudio.constant.Constants;
import com.manimaran.wikiaudio.listerner.CallBackListener;
import com.manimaran.wikiaudio.record.wav.WAVPlayer;
import com.manimaran.wikiaudio.record.wav.WAVRecorder;
import com.manimaran.wikiaudio.utils.GeneralUtils;
import com.manimaran.wikiaudio.utils.PrefManager;
import com.manimaran.wikiaudio.wiki_api.ApiInterface;
import com.manimaran.wikiaudio.wiki_api.ApiClient;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EndlessAdapter extends ArrayAdapter<String> {

    private static final int RECORD_AUDIO_REQUEST_CODE = 101;
    private static final String RECORDED_FILENAME = "record.wav";
    private static final String CONVERTED_FILENAME = "record.ogg";
    private List<String> itemList;
    private Context ctx;
    private Activity activity;
    private int layoutId;
    private Boolean isContributionMode;
    private String TAG = "Record";

    private WAVRecorder recorder = new WAVRecorder();
    private WAVPlayer player = new WAVPlayer();
    private Boolean isPlaying = false, isRecorded = false;
    private Dialog myDialog;

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

    private String uploadName = null;
    private CallBackListener listener;

    public EndlessAdapter(Context ctx, List<String> itemList, int layoutId, Boolean isContributionMode) {
        super(ctx, layoutId, itemList);
        this.itemList = itemList;
        this.ctx = ctx;
        this.activity = (Activity) ctx;
        this.layoutId = layoutId;
        this.isContributionMode = isContributionMode;
        this.pref = new PrefManager(ctx);
        this.api = ApiClient.getCommonsApi(ctx).create(ApiInterface.class);
    }

    private static String getMimeType(String url) {
        String type = null;
        String extension = url.substring(url.lastIndexOf(".") + 1);
        if (!TextUtils.isEmpty(extension)) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public void setCallbackListener(CallBackListener listener) {
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public String getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return itemList.get(position).hashCode();
    }

    @NotNull
    @Override
    public View getView(final int position, View convertView, @NotNull ViewGroup parent) {
        View mView = convertView;

        if (mView == null) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mView = inflater.inflate(layoutId, parent, false);
        }

        // We should use class holder pattern
        TextView tv = mView.findViewById(R.id.txt1);
        tv.setText(itemList.get(position));

        tv.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {

                Activity activity1 = (Activity) ctx;

                if (isContributionMode) {

                    if (GeneralUtils.checkPermissionGranted(activity1)) {
                        showPopup(activity1, position);
                    } else
                        getPermissionToRecordAudio();
                } else {
                    openWiktionaryWebView(position);
                }
            }
        });

        if (isContributionMode) {
            LinearLayout btnWiki = mView.findViewById(R.id.btn_wiki_meaning);
            btnWiki.setVisibility(View.VISIBLE);
            btnWiki.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openWiktionaryWebView(position);
                }
            });
        }


        return mView;

    }

    private void openWiktionaryWebView(int position) {
        Activity activity1 = (Activity) ctx;
        Intent intent = new Intent(ctx, CommonWebActivity.class);
        String word = itemList.get(position);
        String url = String.format(ctx.getString(R.string.url_wiktionary_web), isContributionMode ? pref.getContributionLangCode() : pref.getWiktionaryLangCode(), word);
        intent.putExtra(Constants.TITLE, word);
        intent.putExtra(Constants.URL, url);
        intent.putExtra(Constants.IS_CONTRIBUTION_MODE, isContributionMode);
        intent.putExtra(Constants.IS_WIKTIONARY_WORD, true);
        activity1.startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getPermissionToRecordAudio() {
        // 1) Use the support library version ContextCompat.checkSelfPermission(...) to avoid
        // checking the build version since Context.checkSelfPermission(...) is only available
        // in Marshmallow
        // 2) Always check for permission (even if permission has already been granted)
        // since the user can revoke permissions at any time through Settings
        Activity activity = (Activity) ctx;
        if (!GeneralUtils.checkPermissionGranted((activity))) {
            showMsg("Must need Microphone and Storage permissions.\nPlease grant those permissions");

            // The permission is NOT already granted.
            // Check if the user has been asked about this permission already and denied
            // it. If so, we want to give more explanation about why the permission is needed.
            // Fire off an async request to actually get the permission
            // This will show the standard permission request dialog UI
            activity.requestPermissions(
                    new String[]
                            {
                                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                            }
                    , RECORD_AUDIO_REQUEST_CODE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("ClickableViewAccessibility")
    private void showPopup(final Activity activity, final int pos) {

        isRecorded = false;
        // Dialog init
        myDialog = new Dialog(activity);
        myDialog.setContentView(R.layout.pop_up_record_ui);
        myDialog.setCancelable(false);

        // View init
        btnClose = (ImageView) myDialog.findViewById(R.id.btnClose);
        btnUpload = (Button) myDialog.findViewById(R.id.upload_button);
        btnRecord = (ImageView) myDialog.findViewById(R.id.btnRecord);
        btnPlayPause = (ImageView) myDialog.findViewById(R.id.btnPlayPause);

        txtWord = myDialog.findViewById(R.id.txtWord);
        txtSec = myDialog.findViewById(R.id.txtSec);
        seekBar = (SeekBar) myDialog.findViewById(R.id.seekBar);

        Objects.requireNonNull(myDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();

        // Set values
        txtWord.setText(itemList.get(pos));
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
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
                    uploadName = pref.getContributionLangCode() + "-" + itemList.get(pos) + ".ogg";
                    uploadAudioToWikiServer(false, pos);
                } else
                    GeneralUtils.showToast(ctx, "Please record audio first");
            }
        });

    }

    private void uploadAudioToWikiServer(Boolean recreateEditToken, final int pos) {

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
                                    if (myDialog != null && myDialog.isShowing())
                                        myDialog.dismiss();
                                    //GeneralUtils.logoutAlert(activity);
                                } else {
                                    pref.setCsrfToken(editToken);
                                    completeUpload(editToken, pos);
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
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    dismissDialog("Please check your connection!");
                    t.printStackTrace();

                }
            });
        } else
            completeUpload(pref.getCsrfToken(), pos);


    }

    private void completeUpload(String editToken, final int pos) {

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
                RequestBody.create(MultipartBody.FORM, "upload"), // action
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
                                writeWordToFile(pos);
                            } else {
                                dismissDialog("Upload: " + result);
                                writeWordToFile(pos);
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
                                writeWordToFile(pos);
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

    private void writeWordToFile(int pos) {
        /*
         * Word already have audio
         * Add to text file
         * Then remove from list
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            GeneralUtils.writeAudioWordsToFile(String.format(ctx.getString(R.string.format_file_name_words_already_have_audio), pref.getContributionLangCode()), Collections.singletonList(itemList.get(pos)));
            itemList.remove(pos);
            notifyDataSetChanged();
        }

        if (myDialog != null && myDialog.isShowing())
            myDialog.dismiss();
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

    public void destroyView() {
        /*
        if(player != null)
        {
            player.stopPlaying();
        }*/

       /* btnPlayPause.performClick();
        if(isPlaying && btnPlayPause !=null)
        {
            btnPlayPause.performClick();
            btnPlayPause.callOnClick();
        }*/

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
        return file.getAbsolutePath() + "/" + EndlessAdapter.RECORDED_FILENAME;
    }

    private String getFilePathOfOgg() {
        String filePath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filePath, "/Spell4Wiki/Audios");
        if (!file.exists()) {
            if (!file.mkdirs())
                Log.d(TAG, "Not create directory!");
        }

        File convertedFile = new File(file.getAbsolutePath() + "/" + EndlessAdapter.CONVERTED_FILENAME);
        if (convertedFile.exists()) {
            if (convertedFile.delete()) {
                System.out.println("file Deleted :" + convertedFile.getPath());
            } else {
                System.out.println("file not Deleted :" + convertedFile.getPath());
            }
        }

        String recordedFile = file.getAbsolutePath() + "/" + EndlessAdapter.RECORDED_FILENAME;

        Log.e("TAG", "--------- WAV " + recordedFile);
        FFmpeg.execute("-i " + recordedFile + " -acodec libvorbis "+ convertedFile.getAbsolutePath());
        Log.e("TAG", "---------");

        return file.getAbsolutePath() + "/" + EndlessAdapter.CONVERTED_FILENAME;
    }
}

