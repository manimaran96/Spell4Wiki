package com.manimarank.spell4wiki.databases;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.manimarank.spell4wiki.databases.dao.WikiLangDao;
import com.manimarank.spell4wiki.databases.dao.WordsHaveAudioDao;
import com.manimarank.spell4wiki.databases.entities.WikiLang;
import com.manimarank.spell4wiki.databases.entities.WordsHaveAudio;

@Database(entities = {WikiLang.class, WordsHaveAudio.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract WikiLangDao getWikiLangDao();

    public abstract WordsHaveAudioDao getWordsHaveAudioDao();
}
