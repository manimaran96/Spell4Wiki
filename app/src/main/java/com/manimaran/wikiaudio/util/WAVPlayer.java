package com.manimaran.wikiaudio.util;

import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.Callable;

public class WAVPlayer {
    private MediaPlayer mPlayer;

    public void startPlaying(String sourceFilePath, final Callable onCompletion) {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(sourceFilePath);
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    try {
                        onCompletion.call();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("error", "prepare() failed");
        }
    }

    public void stopPlaying() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }
}
