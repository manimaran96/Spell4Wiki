package com.manimaran.wikiaudio.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WikiToken {
    @SerializedName("query")
    @Expose
    private Query query;

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }


    public static class Query {

        @SerializedName("tokens")
        @Expose
        private TokenValue tokenValue;

        public TokenValue getTokenValue() {
            return tokenValue;
        }

        public void setTokenValue(TokenValue tokenValue) {
            this.tokenValue = tokenValue;
        }
    }

    public static class TokenValue {

        @SerializedName("logintoken")
        @Expose
        private String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}
