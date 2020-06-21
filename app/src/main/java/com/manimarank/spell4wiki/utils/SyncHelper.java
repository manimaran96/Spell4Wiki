package com.manimarank.spell4wiki.utils;

import android.app.Activity;
import android.text.TextUtils;

import com.manimarank.spell4wiki.apis.ApiClient;
import com.manimarank.spell4wiki.apis.ApiInterface;
import com.manimarank.spell4wiki.databases.DBHelper;
import com.manimarank.spell4wiki.databases.entities.WikiLang;
import com.manimarank.spell4wiki.models.WikiBaseData;
import com.manimarank.spell4wiki.models.WikiLanguage;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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

                        AppPref.INSTANCE.setCommonCategories(data.getCategoryCommon());

                        if(data.getUpdateApp() != null){
                            if(data.getUpdateApp().getType() != null && data.getUpdateApp().getVersion() != null){
                                AppPref.INSTANCE.setUpdateAppType(data.getUpdateApp().getType());
                                AppPref.INSTANCE.setUpdateAppVersion(data.getUpdateApp().getVersion());
                            }
                        }

                        if(data.getFetchConfig() != null){
                            if(data.getFetchConfig().getBy() != null)
                                AppPref.INSTANCE.setFetchBy(data.getFetchConfig().getBy());

                            if(data.getFetchConfig().getDir() != null)
                                AppPref.INSTANCE.setFetchDir(data.getFetchConfig().getDir());

                            if(data.getFetchConfig().getLimit() != null)
                                AppPref.INSTANCE.setFetchLimit(data.getFetchConfig().getLimit());
                        }

                        List<WikiLanguage> languageList = data.getLanguageWiseData();
                        if (languageList != null && languageList.size() > 0) {
                            dbHelper.getAppDatabase().getWikiLangDao().deleteAll();
                            for (WikiLanguage lang : languageList) {
                                String titleOfWordsWithoutAudio = null;
                                if (!TextUtils.isEmpty(lang.getTitleOfWordsWithoutAudio()))
                                    titleOfWordsWithoutAudio = lang.getTitleOfWordsWithoutAudio();
                                Boolean isLeftToRightDirection = lang.getDirection() != null && lang.getDirection().equals("ltr");
                                List<String> categories = new ArrayList<>();
                                if(lang.getCategory() != null && lang.getCategory().size() > 0)
                                    categories = lang.getCategory();
                                WikiLang dbLang = new WikiLang(lang.getCode(), lang.getName(), lang.getLocalName(), titleOfWordsWithoutAudio, isLeftToRightDirection, categories);
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
