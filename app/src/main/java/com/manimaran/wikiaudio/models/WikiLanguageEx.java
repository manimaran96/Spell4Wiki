package com.manimaran.wikiaudio.models;

import org.jetbrains.annotations.NotNull;

public class WikiLanguageEx {
    private String code, name, local, titleWordsNoAudio;
    private Boolean isLeftDirection;

    public WikiLanguageEx() {
    }

    public WikiLanguageEx(String code, String name, String local, Boolean isLeftDirection) {
        this.code = code;
        this.name = name;
        this.local = local;
        this.isLeftDirection = isLeftDirection;
    }

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

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public Boolean getIsLeftDirection() {
        return isLeftDirection;
    }

    public void setIsLeftDirection(Boolean leftDirection) {
        isLeftDirection = leftDirection;
    }

    public String getTitleWordsNoAudio() {
        return titleWordsNoAudio;
    }

    public void setTitleWordsNoAudio(String titleWordsNoAudio) {
        this.titleWordsNoAudio = titleWordsNoAudio;
    }

    @NotNull
    @Override
    public String toString() {
        return "WikiLanguage{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", local='" + local + '\'' +
                ", isLeftDirection=" + isLeftDirection +
                '}';
    }
}