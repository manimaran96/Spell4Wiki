package com.manimarank.spell4wiki.record.ogg

import com.arthenica.ffmpegkit.FFmpegKit
import com.manimarank.spell4wiki.utils.Print.error
import com.manimarank.spell4wiki.utils.Print.log
import java.io.File

/**
 * Audio converter for WAV to OGG conversion using FFmpeg.
 * Converts WAV audio files to OGG format
 */
class WavToOggConverter {

    /**
     * Converts a WAV file to OGG format using FFmpeg.
     *
     * @param recordedFilePath Path to the input WAV file
     * @param convertedFilePath Path where the converted OGG file will be saved
     * @return true if conversion was successful, false otherwise
     */
    fun convert(recordedFilePath: String, convertedFilePath: String): Boolean {
        return try {
            val sourceFile = File(recordedFilePath)
            if (!sourceFile.exists()) {
                error("Source audio file not found: $recordedFilePath")
                return false
            }

            // Delete existing converted file if it exists
            val targetFile = File(convertedFilePath)
            if (targetFile.exists()) {
                targetFile.delete()
            }

            // Execute FFmpeg command to convert WAV to OGG
            //val command = "-y -i $recordedFilePath -acodec libvorbis $convertedFilePath"
            val command = "-y -i $recordedFilePath -c:a libopus -b:a 24k $convertedFilePath"


            val session = FFmpegKit.execute(command)
            val returnCode = session.returnCode

            if (returnCode.isValueSuccess) {
                // Check if the output file was created successfully
                if (targetFile.exists() && targetFile.length() > 0) {
                    log("Audio conversion successful: $convertedFilePath")
                    true
                } else {
                    error("Audio conversion failed: Output file not created or is empty")
                    false
                }
            } else {
                error("FFmpeg execution failed with return code: ${returnCode.value} ${session.failStackTrace}")
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            error("Audio conversion failed: ${e.message}")
            false
        }
    }
}