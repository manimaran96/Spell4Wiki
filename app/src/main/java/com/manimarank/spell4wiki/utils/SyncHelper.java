package com.manimarank.spell4wiki.utils;

import android.app.Activity;
import android.text.TextUtils;

import com.manimarank.spell4wiki.Spell4WikiApp;
import com.manimarank.spell4wiki.apis.ApiClient;
import com.manimarank.spell4wiki.apis.ApiInterface;
import com.manimarank.spell4wiki.databases.DBHelper;
import com.manimarank.spell4wiki.databases.entities.WikiLang;
import com.manimarank.spell4wiki.models.WikiBaseData;
import com.manimarank.spell4wiki.models.WikiLanguage;

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

        DBHelper dbHelper = DBHelper.getInstance(mActivity);

        ApiInterface api = ApiClient.getApi().create(ApiInterface.class);
        Call<WikiBaseData> wikiBaseDataCall = api.fetchWikiBaseData();

        wikiBaseDataCall.enqueue(new Callback<WikiBaseData>() {
            @Override
            public void onResponse(@NotNull Call<WikiBaseData> call, @NotNull Response<WikiBaseData> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        WikiBaseData data = response.body();
                        PrefManager prefManager = new PrefManager(Spell4WikiApp.Companion.getApplicationContext());
                        prefManager.setCommonCategories(data.getCategoryCommon());


                        List<WikiLanguage> languageList = data.getLanguageWiseData();
                        if (languageList != null && languageList.size() > 0) {
                            dbHelper.getAppDatabase().getWikiLangDao().deleteAll();
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
                                if(lang.getCategory() != null && lang.getCategory().size() > 0)
                                    dbLang.setCategories(lang.getCategory());
                                dbHelper.getAppDatabase().getWikiLangDao().insert(dbLang);
                            }
                        }
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NotNull Call<WikiBaseData> call, @NotNull Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
