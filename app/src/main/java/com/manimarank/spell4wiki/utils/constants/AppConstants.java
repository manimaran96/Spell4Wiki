package com.manimarank.spell4wiki.utils.constants;

public class AppConstants {

    public static final String URL = "url";
    public static final String TITLE = "title";
    public static final String IS_WIKTIONARY_WORD = "is_wiktionary_word";
    public static final String WORD = "word";
    public static final String DEFAULT_TITLE_FOR_WITHOUT_AUDIO = "பகுப்பு:தமிழ்-ஒலிக்கோப்புகளில்லை";
    public static final String DEFAULT_LANGUAGE_CODE = "ta";
    public static final String LANGUAGE_CODE = "language_code";
    public static final String UPLOAD_COMMENT = "Uploaded using [[:Commons:Spell4Wiki|Spell4Wiki]] app.";

    // Intent
    public static final String SEARCH_TEXT = "search_text";

    // API related
    public static final String PASS = "PASS";
    public static final String FAIL = "FAIL";
    public static final String TWO_FACTOR = "UI";
    public static final String INVALID_CSRF = "+\\";
    public static final String UPLOAD_SUCCESS = "success";
    public static final String UPLOAD_WARNING = "warning";
    public static final String UPLOAD_FILE_EXIST = "fileexists-no-change";
    public static final String UPLOAD_FILE_EXIST_FORBIDDEN = "fileexists-forbidden";
    public static final String UPLOAD_INVALID_TOKEN = "badtoken";

    public static final int API_MAX_RETRY = 3;
    public static final int API_MAX_FAIL_RETRY = 3;
    public static final int API_LOOP_MAX_SECS = 40;
    public static final int API_LOOP_MINIMUM_COUNT_IN_LIST = 15;

    public static final int MAX_RETRIES_FOR_FORCE_LOGIN = 1;
    public static final int MAX_RETRIES_FOR_CSRF_TOKEN = 2;

    // Record & Storage related
    public static final String AUDIO_MAIN_PATH = "Spell4Wiki_Audios";
    public static final String AUDIO_FILEPATH = "/RecordedAudios";
    public static final String AUDIO_TEMP_RECORDER_FILENAME = "record_temp.raw";
    public static final String AUDIO_RECORDED_FILENAME = "record.wav";
    public static final String AUDIO_CONVERTED_FILENAME = "record.ogg";

    // Request Code
    public static final int RC_UPLOAD_DIALOG = 1001;
    public static final int RC_PERMISSIONS = 1000;
    public static final int RC_EDIT_REQUEST_CODE = 42;

    // Mail Ids
    public static final String CONTACT_MAIL = "manimarankumar96@gmail.com";
}
