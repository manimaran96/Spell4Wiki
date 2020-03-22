package com.manimaran.wikiaudio.databases.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;


@Entity(tableName = "words_already_have_audio", indices = {@Index(value = {"word"}, unique = true)})
public class WordsHaveAudio implements Serializable {

    @PrimaryKey
    @NonNull
    private String word;

    public WordsHaveAudio(@NonNull String word) {
        this.word = word;
    }

    @NonNull
    public String getWord() {
        return word;
    }

    public void setWord(@NonNull String word) {
        this.word = word;
    }
}
