package com.manimarank.spell4wiki.utils.constants

/**
 * App level constant values
 */
object AppConstants {
    const val URL = "url"
    const val TITLE = "title"
    const val IS_WIKTIONARY_WORD = "is_wiktionary_word"
    const val WORD = "word"
    const val DEFAULT_TITLE_FOR_WITHOUT_AUDIO = "பகுப்பு:தமிழ்-ஒலிக்கோப்புகளில்லை"
    const val DEFAULT_LANGUAGE_CODE = "ta"
    const val LANGUAGE_CODE = "language_code"
    const val UPLOAD_COMMENT = "Uploaded using [[:Commons:Spell4Wiki|Spell4Wiki]] app."

    // Intent
    const val SEARCH_TEXT = "search_text"

    // API related
    const val PASS = "PASS"
    const val FAIL = "FAIL"
    const val TWO_FACTOR = "UI"
    const val INVALID_CSRF = "+\\"
    const val UPLOAD_SUCCESS = "success"
    const val UPLOAD_WARNING = "warning"
    const val UPLOAD_FILE_EXIST = "fileexists-no-change"
    const val UPLOAD_FILE_EXIST_FORBIDDEN = "fileexists-forbidden"
    const val UPLOAD_INVALID_TOKEN = "badtoken"
    const val API_MAX_RETRY = 3
    const val API_MAX_FAIL_RETRY = 3
    const val API_LOOP_MAX_SECS = 40
    const val API_LOOP_MINIMUM_COUNT_IN_LIST = 15
    const val MAX_RETRIES_FOR_FORCE_LOGIN = 1
    const val MAX_RETRIES_FOR_CSRF_TOKEN = 2

    // Record
    const val MAX_SEC_FOR_RECORDING: Long = 10

    const val MAX_WORD_FILTER_COUNT = 25

    // Record & Storage related
    const val AUDIO_MAIN_PATH = "Spell4Wiki_Audios"
    const val AUDIO_FILEPATH = "/RecordedAudios"
    const val AUDIO_TEMP_RECORDER_FILENAME = "record_temp.raw"
    const val AUDIO_RECORDED_FILENAME = "record.wav"
    const val AUDIO_CONVERTED_FILENAME = "record.ogg"

    // Request Code
    const val RC_UPLOAD_DIALOG = 1001
    const val RC_PERMISSIONS = 1000
    const val RC_EDIT_REQUEST_CODE = 42
    const val RC_STORAGE_AUDIO_PERMISSION = 100

    // Mail Ids
    const val CONTACT_MAIL = "manimarankumar96@gmail.com"
}