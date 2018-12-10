package com.manimaran.wikiaudio.model;

public class Words {
    private Integer id;
    private String word, info, audioFile;
    private Boolean isAudioHave;
 
    public Words() {
    }

    public Words(String word, String info, Boolean isAudioHave) {
        this.word = word;
        this.info = info;
        this.isAudioHave = isAudioHave;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Boolean getIsAudioHave() {
        return isAudioHave;
    }

    public void setIsAudioHave(Boolean audioHave) {
        isAudioHave = audioHave;
    }
}