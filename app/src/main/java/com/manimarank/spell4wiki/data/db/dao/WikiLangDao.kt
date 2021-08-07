package com.manimarank.spell4wiki.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.manimarank.spell4wiki.data.db.entities.WikiLang;

import java.util.List;

@Dao
public interface WikiLangDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WikiLang... wikiLangs);

    @Update
    void update(WikiLang... wikiLangs);

    @Delete
    void delete(WikiLang wikiLang);


    @Query("SELECT * FROM wiki_language")
    List<WikiLang> getWikiLanguageList();

    @Query("SELECT * FROM wiki_language WHERE title_of_words_without_audio NOTNULL")
    List<WikiLang> getWikiLanguageListForWordsWithoutAudio();

    @Query("SELECT * FROM wiki_language WHERE code = :code")
    WikiLang getWikiLanguageWithCode(String code);

    @Query(("DELETE FROM wiki_language"))
    void deleteAll();
}
