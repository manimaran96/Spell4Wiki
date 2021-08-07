package com.manimarank.spell4wiki.data.db.dao

import androidx.room.*
import com.manimarank.spell4wiki.data.db.entities.WikiLang

@Dao
interface WikiLangDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg wikiLangs: WikiLang?)

    @Update
    fun update(vararg wikiLangs: WikiLang?)

    @Delete
    fun delete(wikiLang: WikiLang?)

    @get:Query("SELECT * FROM wiki_language")
    val wikiLanguageList: List<WikiLang?>?

    @get:Query("SELECT * FROM wiki_language WHERE title_of_words_without_audio NOTNULL")
    val wikiLanguageListForWordsWithoutAudio: List<WikiLang?>?

    @Query("SELECT * FROM wiki_language WHERE code = :code")
    fun getWikiLanguageWithCode(code: String?): WikiLang?

    @Query("DELETE FROM wiki_language")
    fun deleteAll()
}