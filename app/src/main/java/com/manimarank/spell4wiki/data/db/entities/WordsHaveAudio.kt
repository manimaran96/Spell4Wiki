package com.manimarank.spell4wiki.data.db.entities

import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Entity
import java.io.Serializable

@Entity(tableName = "words_already_have_audio")
class WordsHaveAudio(
    var word: String?,
    @ColumnInfo(name = "language_code") var languageCode: String?
) : Serializable {
    @PrimaryKey
    @ColumnInfo(name = "combine_word_with_code")
    var combineWordWithCode: String = languageCode?.lowercase() + "_###_" + word?.lowercase()

}