package com.manimaran.wikiaudio.utils;

import android.app.Activity;
import android.text.TextUtils;

import com.manimaran.wikiaudio.apis.ApiClient;
import com.manimaran.wikiaudio.apis.ApiInterface;
import com.manimaran.wikiaudio.databases.DBHelper;
import com.manimaran.wikiaudio.databases.entities.WikiLang;
import com.manimaran.wikiaudio.models.WikiLanguage;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncHelper {

    private Activity mActivity;

    public SyncHelper(Activity activity) {
        mActivity = activity;
    }

    public void syncWikiLanguages() {
        // Sync Wiki Language List

        DBHelper dbHelper = new DBHelper(mActivity);

        ApiInterface api = ApiClient.getApi().create(ApiInterface.class);
        Call<List<WikiLanguage>> wikiLanguageCall = api.fetchWikiLanguageList();

        wikiLanguageCall.enqueue(new Callback<List<WikiLanguage>>() {
            @Override
            public void onResponse(@NotNull Call<List<WikiLanguage>> call, @NotNull Response<List<WikiLanguage>> response) {

                if (response.isSuccessful() && mActivity != null && !mActivity.isDestroyed()) {
                    List<WikiLanguage> languageList = response.body();
                    if (languageList != null && languageList.size() > 0) {
                        for (WikiLanguage lang : languageList) {
                            WikiLang dbLang = new WikiLang();
                            dbLang.setCode(lang.getCode());
                            dbLang.setName(lang.getName());
                            dbLang.setLocalName(lang.getLocalName());
                            if (!TextUtils.isEmpty(lang.getTitleOfWordsWithoutAudio()))
                                dbLang.setTitleOfWordsWithoutAudio(lang.getTitleOfWordsWithoutAudio());
                            if (lang.getDirection() != null && lang.getDirection().equals("rtr"))
                                dbLang.setIsLeftDirection(false);
                            else
                                dbLang.setIsLeftDirection(true);
                            dbHelper.getAppDatabase().getWikiLangDao().insert(dbLang);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<List<WikiLanguage>> call, @NotNull Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
