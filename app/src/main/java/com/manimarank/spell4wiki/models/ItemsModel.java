package com.manimarank.spell4wiki.models;

public class ItemsModel {
    private int icon = -1;
    private String name, about, url;
    private boolean isLottie = false;

    public ItemsModel(String name, String about, String url) {
        this.name = name;
        this.about = about;
        this.url = url;
    }

    public ItemsModel(int icon,  String name, String about, String url) {
        this.icon = icon;
        this.name = name;
        this.about = about;
        this.url = url;
        this.isLottie = false;
    }

    public ItemsModel(int icon,  String name, String about, String url, Boolean isLottie) {
        this.icon = icon;
        this.name = name;
        this.about = about;
        this.url = url;
        this.isLottie = isLottie;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getIcon() {
        return icon;
    }

    public boolean isLottie() {
        return isLottie;
    }

    public void setLottie(boolean lottie) {
        isLottie = lottie;
    }
}
