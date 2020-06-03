package com.manimarank.spell4wiki.utils.constants;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class EnumTypeDef {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ListMode.SPELL_4_WIKI, ListMode.SPELL_4_WORD_LIST, ListMode.SPELL_4_WORD, ListMode.WIKTIONARY, ListMode.TEMP})
    public  @interface ListMode {
        int SPELL_4_WIKI = 0;
        int SPELL_4_WORD_LIST = 1;
        int SPELL_4_WORD = 2;
        int WIKTIONARY = 3;
        int TEMP = 4;
    }
}
