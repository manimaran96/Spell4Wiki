package com.manimarank.spell4wiki.ui.recordaudio

object WikiDataUtils {

    fun getUploadName(langCode: String?, word: String?): String {
        val UPLOAD_FILE_NAME = "%s-%s.ogg"
        return String.format(UPLOAD_FILE_NAME, langCode, word)
    }
}
