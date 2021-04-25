package com.manimarank.spell4wiki.record.wav

import android.media.MediaPlayer
import java.io.IOException
import java.util.concurrent.Callable

class WAVPlayer {
    private var mPlayer: MediaPlayer? = null
    fun startPlaying(sourceFilePath: String?, onCompletion: Callable<*>) {
        mPlayer = MediaPlayer()
        try {
            mPlayer?.setDataSource(sourceFilePath)
            mPlayer?.setOnCompletionListener {
                try {
                    onCompletion.call()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            mPlayer?.prepare()
            mPlayer?.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun stopPlaying() {
        if (mPlayer != null) {
            mPlayer?.release()
            mPlayer = null
        }
    }

    fun seekTo(seekDuration: Int?) {
        mPlayer?.seekTo(seekDuration ?: currentPosition)
    }

    val currentPosition: Int
        get() = mPlayer?.currentPosition ?: 0
    val duration: Int
        get() = mPlayer?.duration ?: 0
}