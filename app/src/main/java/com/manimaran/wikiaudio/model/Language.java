package com.manimaran.wikiaudio.model;

public class Language {
    private String code, name, local;
    private Boolean isLeftDirection;

    public Language() {
    }

    public Language(String code, String name, String local, Boolean isLeftDirection) {
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

    @Override
    public String toString() {
        return "Language{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", local='" + local + '\'' +
                ", isLeftDirection=" + isLeftDirection +
                '}';
    }
}