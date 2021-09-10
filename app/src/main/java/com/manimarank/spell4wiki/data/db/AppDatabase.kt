package com.manimarank.spell4wiki.data.db

import androidx.room.Database
import com.manimarank.spell4wiki.data.db.entities.WikiLang
import com.manimarank.spell4wiki.data.db.entities.WordsHaveAudio
import androidx.room.RoomDatabase
import com.manimarank.spell4wiki.data.db.dao.WikiLangDao
import com.manimarank.spell4wiki.data.db.dao.WordsHaveAudioDao

@Database(entities = [WikiLang::class, WordsHaveAudio::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val wikiLangDao: WikiLangDao?
    abstract val wordsHaveAudioDao: WordsHaveAudioDao?
}