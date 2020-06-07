package com.manimarank.spell4wiki.utils

import android.content.Context
import android.content.SharedPreferences
import com.manimarank.spell4wiki.Spell4WikiApp

object ShowCasePref {


    private var prefs: SharedPreferences

    private const val PREFS_NAME = "Show_Case_Preference"

    const val SPELL_4_WIKI = "spell4wiki"
    const val SPELL_4_WORD_LIST = "spell4word_list"
    const val SPELL_4_WORD = "spell4word"
    const val SPELL_4_WIKI_PAGE = "spell4wiki_page"
    const val SPELL_4_WORD_PAGE = "spell4word_page"
    const val WIKTIONARY_PAGE = "wiktionary_page"
    const val LIST_ITEM_SPELL_4_WIKI = "list_item_spell4wiki"
    const val RECORD_UPLOAD_UI = "record_upload_ui"
    const val CORE_CONTRIBUTORS_LIST_ITEM = "core_contributors_list_item"

    init {
        prefs = Spell4WikiApp.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isNotShowed(key: String): Boolean {
        return !prefs.getBoolean(key, false)
    }

    fun showed(key: String) {
        val prefsEditor = prefs.edit()
        with(prefsEditor) {
            putBoolean(key, true)
            apply()
        }
    }

    fun reset() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }

}