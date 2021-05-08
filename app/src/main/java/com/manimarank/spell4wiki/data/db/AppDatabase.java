package com.manimarank.spell4wiki.data.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.manimarank.spell4wiki.data.db.dao.WikiLangDao;
import com.manimarank.spell4wiki.data.db.dao.WordsHaveAudioDao;
import com.manimarank.spell4wiki.data.db.entities.WikiLang;
import com.manimarank.spell4wiki.data.db.entities.WordsHaveAudio;

@Database(entities = {WikiLang.class, WordsHaveAudio.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract WikiLangDao getWikiLangDao();

    public abstract WordsHaveAudioDao getWordsHaveAudioDao();
}
