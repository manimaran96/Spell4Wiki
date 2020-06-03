package com.manimarank.spell4wiki.databases.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;


@Entity(tableName = "words_already_have_audio")
public class WordsHaveAudio implements Serializable {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "combine_word_with_code")
    private String combineWordWithCode;

    private String word;

    @ColumnInfo(name = "language_code")
    private String languageCode;

    public WordsHaveAudio(String word, String languageCode) {
        this.word = word;
        this.languageCode = languageCode;
        this.combineWordWithCode = languageCode.toLowerCase() + "_###_" + word.toLowerCase();
    }

    @NonNull
    public String getWord() {
        return word;
    }

    public void setWord(@NonNull String word) {
        this.word = word;
    }


    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    @NotNull
    public String getCombineWordWithCode() {
        return combineWordWithCode;
    }

    public void setCombineWordWithCode(@NotNull String combineWordWithCode) {
        this.combineWordWithCode = combineWordWithCode;
    }
}
