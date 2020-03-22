package com.manimaran.wikiaudio.databases;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.manimaran.wikiaudio.databases.dao.WikiLangDao;
import com.manimaran.wikiaudio.databases.dao.WordsHaveAudioDao;
import com.manimaran.wikiaudio.databases.entities.WikiLang;
import com.manimaran.wikiaudio.databases.entities.WordsHaveAudio;

@Database(entities = {WikiLang.class, WordsHaveAudio.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract WikiLangDao getWikiLangDao();

    public abstract WordsHaveAudioDao getWordsHaveAudioDao();
}
