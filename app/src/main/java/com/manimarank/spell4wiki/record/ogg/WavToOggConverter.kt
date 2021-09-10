package com.manimarank.spell4wiki.record.ogg

import com.arthenica.mobileffmpeg.FFmpeg
import java.lang.Exception

class WavToOggConverter {
    fun convert(recordedFilePath: String, convertedFilePath: String) {
        try {
            FFmpeg.execute("-y -i $recordedFilePath -acodec libvorbis $convertedFilePath")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}