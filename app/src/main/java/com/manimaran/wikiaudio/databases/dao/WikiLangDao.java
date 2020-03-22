package com.manimaran.wikiaudio.databases.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.manimaran.wikiaudio.databases.entities.WikiLang;

import java.util.List;

@Dao
public interface WikiLangDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WikiLang... wikiLangs);

    @Update
    public void update(WikiLang... wikiLangs);

    @Delete
    public void delete(WikiLang wikiLang);


    @Query("SELECT * FROM wiki_language")
    public List<WikiLang> getWikiLanguageList();

    @Query("SELECT * FROM wiki_language WHERE code = :code")
    public WikiLang getWikiLanguageWithCode(String code);

}
