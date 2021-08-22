package com.manimarank.spell4wiki.data.db.dao

import androidx.room.*
import com.manimarank.spell4wiki.data.db.entities.WordsHaveAudio

@Dao
interface WordsHaveAudioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg wordsHaveAudios: WordsHaveAudio?)

    @Update
    fun update(vararg wordsHaveAudios: WordsHaveAudio?)

    @Delete
    fun delete(wordsHaveAudio: WordsHaveAudio?)

    @Query("SELECT word FROM words_already_have_audio WHERE language_code = :code")
    fun getWordsAlreadyHaveAudioByLanguage(code: String?): ArrayList<String>?
}