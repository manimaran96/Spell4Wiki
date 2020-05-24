package com.manimarank.spell4wiki.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

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

    @SerializedName("title_of_words_list")
    @Expose
    private String titleOfWordsWithoutAudio;

    @SerializedName("category")
    @Expose
    private List<String> category = null;

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

    public List<String> getCategory() {
        return category;
    }

    public void setCategory(List<String> category) {
        this.category = category;
    }
}
