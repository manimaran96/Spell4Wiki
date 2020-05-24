package com.manimarank.spell4wiki.databases.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.manimarank.spell4wiki.databases.entities.WordsHaveAudio;

import java.util.List;

@Dao
public interface WordsHaveAudioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WordsHaveAudio... wordsHaveAudios);

    @Update
    public void update(WordsHaveAudio... wordsHaveAudios);

    @Delete
    public void delete(WordsHaveAudio wordsHaveAudio);

    @Query("SELECT word FROM words_already_have_audio WHERE language_code = :code")
    public List<String> getWordsAlreadyHaveAudioByLanguage(String code);

}
