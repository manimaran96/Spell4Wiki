package com.manimaran.wikiaudio;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class SwipeFragment extends Fragment {
    private String word;

    private ImageView imgRecord, imgPlay, imgSend;
    private SeekBar seekBar;
    private LinearLayout layoutPlayControl;
    private MediaRecorder mRecorder;
    private MediaPlayer mPlayer;
    private String fileName = null;
    private int lastProgress = 0;
    private Handler mHandler = new Handler();
    private int RECORD_AUDIO_REQUEST_CODE =123 ;
    private boolean isPlaying = false, isRecording = false;

    static SwipeFragment newInstance(int position) {
        SwipeFragment swipeFragment = new SwipeFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        swipeFragment.setArguments(bundle);
        return swipeFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.page, container, false);

        layoutPlayControl = (LinearLayout) view.findViewById(R.id.linearLayoutPlay);
        layoutPlayControl.setVisibility(View.INVISIBLE);

        imgRecord = (ImageView) view.findViewById(R.id.record);
        imgRecord.setColorFilter(Color.BLACK);
        imgSend = (ImageView) view.findViewById(R.id.send);
        imgPlay = (ImageView) view.findViewById(R.id.imagePlay);
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);


        imgRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!checkPermission())
                {
                    if(!isRecording) {
                        isRecording = true;
                        prepareForRecording();
                        startRecording();
                        imgRecord.setImageResource(R.drawable.ic_recording);
                    }else
                    {
                        isRecording = false;
                        imgRecord.setImageResource(R.drawable.ic_retry);
                        prepareForStop();
                        stopRecording();
                    }
                }else
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        getPermissionToRecordAudio();
                    }
                }
            }
        });

        imgPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( !isPlaying && fileName != null ){
                    isPlaying = true;
                    startPlaying();
                }else{
                    isPlaying = false;
                    stopPlaying();
                }
            }
        });

        TextView txtWord = view.findViewById(R.id.word);
        if(word != null)
            txtWord.setText(word);

        existAudioFileToLoad();

        return view;
    }

    public void setWord(String s) {
        this.word = s;
    }

    public Boolean checkPermission()
    {
        return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getPermissionToRecordAudio() {
        // 1) Use the support library version ContextCompat.checkSelfPermission(...) to avoid
        // checking the build version since Context.checkSelfPermission(...) is only available
        // in Marshmallow
        // 2) Always check for permission (even if permission has already been granted)
        // since the user can revoke permissions at any time through Settings
        if (checkPermission()) {

            // The permission is NOT already granted.
            // Check if the user has been asked about this permission already and denied
            // it. If so, we want to give more explanation about why the permission is needed.
            // Fire off an async request to actually get the permission
            // This will show the standard permission request dialog UI
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    RECORD_AUDIO_REQUEST_CODE);

        }
    }

    // Callback with the request from calling requestPermissions(...)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            if (grantResults.length == 3 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED){

                Toast.makeText(getContext(), "Record Audio permission granted", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getContext(), "You must give permissions to record audio.", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void prepareForStop() {
        imgRecord.setColorFilter(Color.BLACK);
        layoutPlayControl.setVisibility(View.VISIBLE);
    }


    private void prepareForRecording() {
        imgRecord.setColorFilter(Color.MAGENTA);
        layoutPlayControl.setVisibility(View.INVISIBLE);
    }

    private void stopPlaying() {
        try{
            if(mPlayer != null)
                mPlayer.release();
        }catch (Exception e){
            e.printStackTrace();
        }
        mPlayer = null;
        //showing the play button
        imgPlay.setImageResource(R.drawable.ic_play);
    }

    private void existAudioFileToLoad()
    {
        if(!checkPermission()) {
            try {
                File file = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Wiki/Audios/" + word + ".mp3");
                if(file.exists()) {
                    fileName = file.getAbsolutePath();
                    layoutPlayControl.setVisibility(fileName != null ? View.VISIBLE : View.INVISIBLE);
                    imgRecord.setImageResource(R.drawable.ic_retry);
                    /*lastProgress = 0;
                    seekBar.setProgress(0);
                    stopPlaying();*/
                }

            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        File root = android.os.Environment.getExternalStorageDirectory();
        File file = new File(root.getAbsolutePath() + "/Wiki/Audios");
        if (!file.exists()) {
            file.mkdirs();
        }

        fileName =  root.getAbsolutePath() + "/Wiki/Audios/" + word + ".wav";
        Log.d("filename",fileName);
        mRecorder.setOutputFile(fileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        lastProgress = 0;
        seekBar.setProgress(0);
        stopPlaying();
        // making the imageview a stop button
        //starting the chronometer
    }


    private void stopRecording() {

        try{
            mRecorder.stop();
            mRecorder.release();
        }catch (Exception e){
            e.printStackTrace();
        }
        mRecorder = null;
        Toast.makeText(getContext(), "Recording saved successfully.", Toast.LENGTH_SHORT).show();
    }


    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(fileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("LOG_TAG", "prepare() failed");
        }
        //making the imageview pause button
        imgPlay.setImageResource(R.drawable.ic_pause);

        seekBar.setProgress(lastProgress);
        mPlayer.seekTo(lastProgress);
        seekBar.setMax(mPlayer.getDuration());
        seekUpdation();

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                imgPlay.setImageResource(R.drawable.ic_play);
                isPlaying = false;
                lastProgress = 0;
                seekBar.setProgress(0);
                mPlayer.seekTo(0);
            }
        });



        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if( mPlayer!=null && fromUser ){
                    mPlayer.seekTo(progress);
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
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            seekUpdation();
        }
    };

    private void seekUpdation() {
        if(mPlayer != null){
            int mCurrentPosition = mPlayer.getCurrentPosition() ;
            seekBar.setProgress(mCurrentPosition);
            lastProgress = mCurrentPosition;
        }
        mHandler.postDelayed(runnable, 100);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //destroy();
    }

    public void destroy()
    {

        isPlaying = false;
        isRecording = false;
        stopPlaying();
        prepareForStop();
        stopRecording();
    }
}