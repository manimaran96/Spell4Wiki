package com.manimarank.spell4wiki.record.wav;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class WAVRecorder {
    private static final int RECORDER_BPP = 16;
    private static final int RECORDER_SAMPLE_RATE = 16000; //44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final String TAG = WAVRecorder.class.getSimpleName();
    private static int bufferSize = AudioRecord.getMinBufferSize(
            RECORDER_SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
    );
    private AudioRecord recorder = null;
    private Thread recordingThread = null;
    private boolean isRecording = false;

    private static void copyWaveFile(String inFilename, String outFilename) {
        FileInputStream in;
        FileOutputStream out;
        long totalAudioLen;
        long totalDataLen;
        int channels = 1;
        long byteRate = RECORDER_BPP * RECORDER_SAMPLE_RATE * channels / 8;

        byte[] data = new byte[bufferSize];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            Log.d(TAG, "File size: " + totalDataLen);

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    channels, byteRate);

            while (in.read(data) != -1) {
                out.write(data);
            }

            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void WriteWaveFileHeader(
            FileOutputStream out, long totalAudioLen,
            long totalDataLen, int channels,
            long byteRate) throws IOException {

        byte[] header = new byte[44];

        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) ((long) WAVRecorder.RECORDER_SAMPLE_RATE & 0xff);
        header[25] = (byte) (((long) WAVRecorder.RECORDER_SAMPLE_RATE >> 8) & 0xff);
        header[26] = (byte) (((long) WAVRecorder.RECORDER_SAMPLE_RATE >> 16) & 0xff);
        header[27] = (byte) (((long) WAVRecorder.RECORDER_SAMPLE_RATE >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = RECORDER_BPP; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }

    public void startRecording(String recordingFilePath) {
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLE_RATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, bufferSize);

        int i = recorder.getState();
        if (i == 1)
            recorder.startRecording();

        isRecording = true;

        recordingThread = new Thread(() -> writeAudioDataToFile(recordingFilePath), "AudioRecorder Thread");

        recordingThread.start();
    }

    private void writeAudioDataToFile(String filename) {
        byte[] data = new byte[bufferSize];
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int read;

        if (null != os) {
            while (isRecording) {
                read = recorder.read(data, 0, bufferSize);

                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    try {
                        os.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopRecording(String recordingFilePath, String recordedFilePath) {
        if (null != recorder) {
            isRecording = false;

            int i = recorder.getState();
            if (i == 1)
                recorder.stop();
            recorder.release();

            recorder = null;
            recordingThread = null;
        }

        copyWaveFile(recordingFilePath, recordedFilePath);

        //  Delete temporary file
        File tempFile = new File(recordingFilePath);
        if (!tempFile.delete())
            Log.d(TAG, "Can not delete temporary file!");
    }

    public Boolean isRecording() {
        return isRecording;
    }
}
