package com.manimaran.wikiaudio.databases;

import android.content.Context;

import androidx.room.Room;

public class DBHelper {

    private static DBHelper mInstance;

    //our app database object
    private AppDatabase appDatabase;

    private DBHelper(Context mCtx) {
        //creating the app database with Room database builder
        //Spell4Wiki-DB is the name of the database
        appDatabase = Room.databaseBuilder(mCtx, AppDatabase.class, "Spell4Wiki-DB").allowMainThreadQueries().build();
    }

    public static synchronized DBHelper getInstance(Context mCtx) {
        if (mInstance == null) {
            mInstance = new DBHelper(mCtx);
        }
        return mInstance;
    }

    public AppDatabase getAppDatabase() {
        return appDatabase;
    }
}
