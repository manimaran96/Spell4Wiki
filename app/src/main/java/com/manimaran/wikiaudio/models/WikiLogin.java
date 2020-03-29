package com.manimaran.wikiaudio.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WikiLogin {
    @SerializedName("clientlogin")
    @Expose
    private ClientLogin clientlogin;

    public ClientLogin getClientLogin() {
        return clientlogin;
    }

    public void setClientLogin(ClientLogin clientlogin) {
        this.clientlogin = clientlogin;
    }


    public static class ClientLogin {

        @SerializedName("status")
        @Expose
        private String status;
        @SerializedName("username")
        @Expose
        private String username;
        @SerializedName("message")
        @Expose
        private String message;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

}


