package com.manimaran.wikiaudio.constants;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class EnumTypeDef {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LanguageSelectionMode.SPELL_4_WIKI, LanguageSelectionMode.SPELL_4_WORD_LIST, LanguageSelectionMode.SPELL_4_WORD, LanguageSelectionMode.WIKTIONARY, LanguageSelectionMode.TEMP})
    public  @interface LanguageSelectionMode {
        int SPELL_4_WIKI = 0;
        int SPELL_4_WORD_LIST = 1;
        int SPELL_4_WORD = 2;
        int WIKTIONARY = 3;
        int TEMP = 4;
    }
    /*
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({UrlType.COMMONS, UrlType.WIKTIONARY, UrlType.WIKTIONARY_WEB})
    public  @interface UrlType {
        int COMMONS = 0; // For Uploading Audio
        int WIKTIONARY = 1;
        int WIKTIONARY_WEB = 2;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({WebViewOpenMode.INTERNAL, WebViewOpenMode.EXTERNAL})
    public  @interface WebViewOpenMode {
        int INTERNAL = 0;
        int EXTERNAL = 1;
    }*/
}
