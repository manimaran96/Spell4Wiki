package com.manimarank.spell4wiki.data.db

import android.content.Context
import androidx.room.Room

class DBHelper private constructor(mCtx: Context) {
    //our app database object
    // creating the app database with Room database builder
    // Spell4Wiki-DB is the name of the database
    val appDatabase: AppDatabase = Room.databaseBuilder(mCtx, AppDatabase::class.java, "Spell4Wiki-DB")
        .allowMainThreadQueries().build()

    companion object {
        private var mInstance: DBHelper? = null
        @Synchronized
        fun getInstance(mCtx: Context): DBHelper {
            if (mInstance == null) {
                mInstance = DBHelper(mCtx)
            }
            return mInstance!!
        }
    }
}