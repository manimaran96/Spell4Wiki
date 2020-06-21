package com.manimarank.spell4wiki.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WikiBaseData {
    @SerializedName("category_common")
    @Expose
    private List<String> categoryCommon = null;
    @SerializedName("language_wise_data")
    @Expose
    private List<WikiLanguage> languageWiseData = null;

    @SerializedName("update_content")
    @Expose
    private UpdateApp updateApp = null;

    @SerializedName("fetch_config")
    @Expose
    private FetchConfig fetchConfig = null;

    public List<String> getCategoryCommon() {
        return categoryCommon;
    }

    public void setCategoryCommon(List<String> categoryCommon) {
        this.categoryCommon = categoryCommon;
    }

    public List<WikiLanguage> getLanguageWiseData() {
        return languageWiseData;
    }

    public void setLanguageWiseData(List<WikiLanguage> languageWiseData) {
        this.languageWiseData = languageWiseData;
    }

    public UpdateApp getUpdateApp() {
        return updateApp;
    }

    public FetchConfig getFetchConfig() {
        return fetchConfig;
    }
}
