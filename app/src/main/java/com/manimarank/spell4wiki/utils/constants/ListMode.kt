package com.manimarank.spell4wiki.utils.constants

import androidx.annotation.IntDef

/**
 * Enum constant for words listing mode
 */
class ListMode {
    /**
     * Singleton class for List mode
     */
    companion object {
        const val SPELL_4_WIKI = 0
        const val SPELL_4_WORD_LIST = 1
        const val SPELL_4_WORD = 2
        const val WIKTIONARY = 3
        const val TEMP = 4

        @Retention(AnnotationRetention.SOURCE)
        @IntDef(SPELL_4_WIKI, SPELL_4_WORD_LIST, SPELL_4_WORD, WIKTIONARY, TEMP)
        annotation class EnumListMode
    }
}