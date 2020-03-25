package com.manimaran.wikiaudio.databases.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.manimaran.wikiaudio.databases.entities.WordsHaveAudio;

import java.util.List;

@Dao
public interface WordsHaveAudioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WordsHaveAudio... wordsHaveAudios);

    @Update
    public void update(WordsHaveAudio... wordsHaveAudios);

    @Delete
    public void delete(WordsHaveAudio wordsHaveAudio);

    @Query("SELECT * FROM words_already_have_audio WHERE language_code = :code")
    public List<WordsHaveAudio> getWordsAlreadyHaveAudioByLanguage(String code);

}
