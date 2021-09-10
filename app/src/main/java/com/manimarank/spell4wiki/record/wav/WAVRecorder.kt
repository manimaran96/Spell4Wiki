package com.manimarank.spell4wiki.record.wav

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.manimarank.spell4wiki.utils.Print.log
import java.io.*

class WAVRecorder {
    private var recorder: AudioRecord? = null
    private var recordingThread: Thread? = null
    var isRecording = false
        private set

    fun startRecording(recordingFilePath: String) {
        recorder = AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLE_RATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize)
        val i = recorder?.state
        if (i == 1) recorder?.startRecording()
        isRecording = true
        recordingThread = Thread({ writeAudioDataToFile(recordingFilePath) }, "AudioRecorder Thread")
        recordingThread?.start()
    }

    private fun writeAudioDataToFile(filename: String) {
        val data = ByteArray(bufferSize)
        var os: FileOutputStream? = null
        try {
            os = FileOutputStream(filename)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        var read: Int
        if (null != os) {
            while (isRecording) {
                read = recorder?.read(data, 0, bufferSize) ?:  AudioRecord.ERROR_INVALID_OPERATION
                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    try {
                        os.write(data)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            try {
                os.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun stopRecording(recordingFilePath: String, recordedFilePath: String) {
        if (null != recorder) {
            isRecording = false
            val i = recorder?.state
            if (i == 1) recorder?.stop()
            recorder?.release()
            recorder = null
            recordingThread = null
        }
        copyWaveFile(recordingFilePath, recordedFilePath)

        //  Delete temporary file
        val tempFile = File(recordingFilePath)
        if (!tempFile.delete()) log(TAG + " --> Can not delete temporary file!")
    }

    companion object {
        private const val RECORDER_BPP = 16
        private const val RECORDER_SAMPLE_RATE = 16000 //44100;
        private const val RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO
        private const val RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT
        private val TAG = WAVRecorder::class.java.simpleName
        private val bufferSize = AudioRecord.getMinBufferSize(
                RECORDER_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        )

        private fun copyWaveFile(inFilename: String, outFilename: String) {
            val `in`: FileInputStream
            val out: FileOutputStream
            val totalAudioLen: Long
            val totalDataLen: Long
            val channels = 1
            val byteRate = (RECORDER_BPP * RECORDER_SAMPLE_RATE * channels / 8).toLong()
            val data = ByteArray(bufferSize)
            try {
                `in` = FileInputStream(inFilename)
                out = FileOutputStream(outFilename)
                totalAudioLen = `in`.channel.size()
                totalDataLen = totalAudioLen + 36
                log(TAG + " --> File size: " + totalDataLen)
                WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                        channels, byteRate)
                while (`in`.read(data) != -1) {
                    out.write(data)
                }
                `in`.close()
                out.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        @Throws(IOException::class)
        private fun WriteWaveFileHeader(out: FileOutputStream, totalAudioLen: Long, totalDataLen: Long, channels: Int, byteRate: Long
        ) {
            val header = ByteArray(44)
            header[0] = 'R'.toByte() // RIFF/WAVE header
            header[1] = 'I'.toByte()
            header[2] = 'F'.toByte()
            header[3] = 'F'.toByte()
            header[4] = (totalDataLen and 0xff).toByte()
            header[5] = (totalDataLen shr 8 and 0xff).toByte()
            header[6] = (totalDataLen shr 16 and 0xff).toByte()
            header[7] = (totalDataLen shr 24 and 0xff).toByte()
            header[8] = 'W'.toByte()
            header[9] = 'A'.toByte()
            header[10] = 'V'.toByte()
            header[11] = 'E'.toByte()
            header[12] = 'f'.toByte() // 'fmt ' chunk
            header[13] = 'm'.toByte()
            header[14] = 't'.toByte()
            header[15] = ' '.toByte()
            header[16] = 16 // 4 bytes: size of 'fmt ' chunk
            header[17] = 0
            header[18] = 0
            header[19] = 0
            header[20] = 1 // format = 1
            header[21] = 0
            header[22] = channels.toByte()
            header[23] = 0
            header[24] = (RECORDER_SAMPLE_RATE.toLong() and 0xff).toByte()
            header[25] = (RECORDER_SAMPLE_RATE.toLong() shr 8 and 0xff).toByte()
            header[26] = (RECORDER_SAMPLE_RATE.toLong() shr 16 and 0xff).toByte()
            header[27] = (RECORDER_SAMPLE_RATE.toLong() shr 24 and 0xff).toByte()
            header[28] = (byteRate and 0xff).toByte()
            header[29] = (byteRate shr 8 and 0xff).toByte()
            header[30] = (byteRate shr 16 and 0xff).toByte()
            header[31] = (byteRate shr 24 and 0xff).toByte()
            header[32] = (2 * 16 / 8).toByte() // block align
            header[33] = 0
            header[34] = RECORDER_BPP.toByte() // bits per sample
            header[35] = 0
            header[36] = 'd'.toByte()
            header[37] = 'a'.toByte()
            header[38] = 't'.toByte()
            header[39] = 'a'.toByte()
            header[40] = (totalAudioLen and 0xff).toByte()
            header[41] = (totalAudioLen shr 8 and 0xff).toByte()
            header[42] = (totalAudioLen shr 16 and 0xff).toByte()
            header[43] = (totalAudioLen shr 24 and 0xff).toByte()
            out.write(header, 0, 44)
        }
    }
}