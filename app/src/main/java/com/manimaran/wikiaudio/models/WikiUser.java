package com.manimaran.wikiaudio.models;

public class WikiUser {
    private String userName, password;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public WikiUser(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }
}
