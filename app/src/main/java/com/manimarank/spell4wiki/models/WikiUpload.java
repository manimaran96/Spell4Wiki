package com.manimarank.spell4wiki.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WikiUpload {

    @SerializedName("upload")
    @Expose
    private WikiSuccess success;

    @SerializedName("error")
    @Expose
    private WikiError error;

    public WikiSuccess getSuccess() {
        return success;
    }

    public void setSuccess(WikiSuccess success) {
        this.success = success;
    }

    public WikiError getError() {
        return error;
    }

    public void setError(WikiError error) {
        this.error = error;
    }

    public static class WikiSuccess{
        @SerializedName("result")
        @Expose
        private String result;

        @SerializedName("filename")
        @Expose
        private String filename;

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }
    }

    public static class WikiError {

        @SerializedName("code")
        @Expose
        private String code;

        @SerializedName("info")
        @Expose
        private String info;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }
    }



}
