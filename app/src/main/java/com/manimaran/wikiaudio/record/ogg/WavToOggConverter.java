package com.manimaran.wikiaudio.record.ogg;

import com.arthenica.mobileffmpeg.FFmpeg;

public class WavToOggConverter {

    public WavToOggConverter() {
    }

    public void convert(String recordedFilePath, String convertedFilePath){
        try {
            FFmpeg.execute("-y -i " + recordedFilePath + " -acodec libvorbis " + convertedFilePath);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
