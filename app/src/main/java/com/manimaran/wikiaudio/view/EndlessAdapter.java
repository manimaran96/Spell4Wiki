package com.manimaran.wikiaudio.view;

/*
 * Copyright (C) 2012 Surviving with Android (http://www.survivingwithandroid.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.acticity.WebWikiActivity;
import com.manimaran.wikiaudio.util.GeneralUtils;
import com.manimaran.wikiaudio.util.WAVPlayer;
import com.manimaran.wikiaudio.util.WAVRecorder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class EndlessAdapter extends ArrayAdapter<String> {

    private static final int RECORD_AUDIO_REQUEST_CODE = 101;
    private static final String RECORDED_FILENAME = "record.wav";
    private List<String> itemList;
    private Context ctx;
    private Activity activity;
    private int layoutId;
    private Boolean isAudioMode = true;
    private String TAG = "Record";

    private WAVRecorder recorder = new WAVRecorder();
    private WAVPlayer player = new WAVPlayer();
    private Boolean isPlaying = false, isRecorded = false;
    private Dialog myDialog;

    private ImageView btnClose, btnRecord, btnPlayPause;
    private Button btnUpload;
    private TextView txtWord, txtSec;
    private SeekBar seekBar;
    private LinearLayout layoutPlayer;

    private Integer lastProgress = 0;
    private Runnable runnable;
    private Handler mHandler = new Handler();

    public EndlessAdapter(Context ctx, List<String> itemList, int layoutId, Boolean isAudioMode) {
        super(ctx, layoutId, itemList);
        this.itemList = itemList;
        this.ctx = ctx;
        this.activity = (Activity) ctx;
        this.layoutId = layoutId;
        this.isAudioMode = isAudioMode;

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

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View result = convertView;

        if (result == null) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            result = inflater.inflate(layoutId, parent, false);
        }

        // We should use class holder pattern
        TextView tv = (TextView) result.findViewById(R.id.txt1);
        tv.setText(itemList.get(position));

        tv.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {

                Activity activity1 = (Activity) ctx;

                if(isAudioMode)
                {

                    if(GeneralUtils.checkPermissionGranted(activity1))
                    {
                        showPopup(activity1, position);
                    }else
                        getPermissionToRecordAudio();
                }else
                {
                    Intent intent = new Intent(ctx, WebWikiActivity.class);
                    if(isAudioMode)
                        intent.putExtra("pos", position);
                    intent.putExtra("word", itemList.get(position));
                    activity1.startActivity(intent);
                }
            }
        });

        return result;

    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getPermissionToRecordAudio() {
        // 1) Use the support library version ContextCompat.checkSelfPermission(...) to avoid
        // checking the build version since Context.checkSelfPermission(...) is only available
        // in Marshmallow
        // 2) Always check for permission (even if permission has already been granted)
        // since the user can revoke permissions at any time through Settings
        Activity activity = (Activity) ctx;
        showMsg("Must need Microphone and Storage permissions.\nPlease grant those permissions");
        if (!GeneralUtils.checkPermissionGranted((activity))) {

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

    public void showPopup(final Activity activity, int pos) {

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
        layoutPlayer = (LinearLayout) myDialog.findViewById(R.id.linearLayoutPlay);
        seekBar = (SeekBar) myDialog.findViewById(R.id.seekBar);

        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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
                long sec = millisUntilFinished/1000;
                txtSec.setText(("00:"+ ((sec +"").length() == 2 ? sec : "0" +sec)));
            }

            public void onFinish() {
                if(recorder.isRecording())
                {
                    player.stopPlaying();
                    txtSec.setText("00:10");
                    recorder.stopRecording(getFilename());

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
                if(GeneralUtils.checkPermissionGranted(activity)) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Log.d(TAG, "Start Recording");
                        player.stopPlaying();
                        countDowntimer.start();
                        //btnPlayPause.setImageResource(R.drawable.play_button_start); hide play control
                        recorder.startRecording();

                        // Animation for scale
                        btnRecord.animate()
                                .setDuration(100)
                                .scaleX(1.4f)
                                .scaleY(1.4f);

                        //recordText.setText(R.string.now_recording);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        Log.d(TAG, "Stop Recording");
                        //recordText.setText("");
                        //isPlaying[0] = true;
                        countDowntimer.onFinish();
                        /*txtSec.setText("00.10");
                        layoutPlayer.setVisibility(View.VISIBLE);
                        btnRecord.setImageResource(R.drawable.ic_record);
                        recorder.stopRecording(getFilename());*/
                    }
                }else
                    showMsg("Please give require permissions");
                return false;
            }
        });

        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isRecorded)
                    player();
                else
                    showMsg("Please record audio first");
            }

            private void player() {
                onPlayStatusChanged();
            }

            private void onPlayStatusChanged() {
                if (isPlaying) {
                    player.stopPlaying();
                    btnPlayPause.setImageResource(R.drawable.ic_play);
                } else {
                    player.startPlaying(getFilename(), new Callable() {
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

        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if( player!=null && fromUser){
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


    }

    private void animateView(View view) {
        AnimationSet animationSet = new AnimationSet(true);

        ScaleAnimation growAnimation = new ScaleAnimation(1.0f, 3.0f, 1.0f, 3.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ScaleAnimation shrinkAnimation = new ScaleAnimation(3.0f, 1.0f, 3.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        animationSet.addAnimation(growAnimation);
        animationSet.addAnimation(shrinkAnimation);
        animationSet.setDuration(200);

        view.startAnimation(animationSet);
    }

    private void seekUpdation() {
        if(player != null){
            int mCurrentPosition = player.getCurrentPosition() ;
            seekBar.setProgress(mCurrentPosition);
            lastProgress = mCurrentPosition;
        }
        mHandler.postDelayed(runnable, 100);

    }

    private void showMsg(String msg)
    {
        Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
    }

    private String getFilename() {
        String filePath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filePath, "/Wiki/Audios");
        if (!file.exists()) {
            if(!file.mkdirs())
                Log.d(TAG, "Not create directory!");
        }
        return file.getAbsolutePath() + "/" +RECORDED_FILENAME;
    }

    private void processResult(String responseStr) throws JSONException {
        JSONObject reader = new JSONObject(responseStr);

        try {
            Toast.makeText(ctx, responseStr, Toast.LENGTH_SHORT).show();
            ArrayList<String> titleList = new ArrayList<>();
            Log.w("TAG", "WIKI api " + reader.toString());
            JSONArray searchResults = reader.getJSONObject("query").optJSONArray("categorymembers");
            for (int ii = 0; ii < searchResults.length(); ii++) {
                titleList.add(
                        searchResults.getJSONObject(ii).getString("title")
                        //+ " --> " + searchResults.getJSONObject(ii).getString("pageid")
                );
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}