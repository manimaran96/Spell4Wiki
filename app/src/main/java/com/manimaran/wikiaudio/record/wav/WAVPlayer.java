package com.manimaran.wikiaudio.record.wav;

import android.media.MediaPlayer;

import java.io.IOException;
import java.util.concurrent.Callable;

public class WAVPlayer {
    private MediaPlayer mPlayer;

    public void startPlaying(String sourceFilePath, final Callable onCompletion) {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(sourceFilePath);
            mPlayer.setOnCompletionListener(mediaPlayer -> {
                try {
                    onCompletion.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopPlaying() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    public void seekTo(Integer val) {
        if (mPlayer != null)
            mPlayer.seekTo(val);
    }

    public Integer getCurrentPosition() {
        return mPlayer != null ? mPlayer.getCurrentPosition() : 0;
    }

    public Integer getDuration() {
        return mPlayer != null ? mPlayer.getDuration() : 0;
    }
}
