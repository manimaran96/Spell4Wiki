package com.manimarank.spell4wiki.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WikiLanguage {

    @SerializedName("code")
    @Expose
    private String code;

    @SerializedName("lang")
    @Expose
    private String name;

    @SerializedName("dir")
    @Expose
    private String direction;
    @SerializedName("local_lang")
    @Expose
    private String localName;

    @SerializedName("title_words_without_audio")
    @Expose
    private String titleOfWordsWithoutAudio;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public String getTitleOfWordsWithoutAudio() {
        return titleOfWordsWithoutAudio;
    }

    public void setTitleOfWordsWithoutAudio(String titleOfWordsWithoutAudio) {
        this.titleOfWordsWithoutAudio = titleOfWordsWithoutAudio;
    }
}
