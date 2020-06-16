package com.manimarank.spell4wiki.apis;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.manimarank.spell4wiki.utils.PrefManager;
import com.manimarank.spell4wiki.utils.constants.Urls;

import java.util.Objects;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofitCommons = null;
    private static Retrofit retrofitApi = null;
    @SuppressLint("StaticFieldLeak")
    private static PrefManager pref;

    public static Retrofit getCommonsApi(Context context) {
        if (retrofitCommons == null) {
            retrofitCommons = new Retrofit.Builder()
                    .baseUrl(Urls.COMMONS)
                    .client(Objects.requireNonNull(getOkHttpClient(context)))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitCommons;
    }

    public static Retrofit getWiktionaryApi(Context context, String langCode) {
        return new Retrofit.Builder()
                .baseUrl(getWiktionaryApiUrl(langCode))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static Retrofit getApi() {
        if (retrofitApi == null) {
            retrofitApi = new Retrofit.Builder()
                    .baseUrl("https://github.com")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitApi;
    }

    private static String getWiktionaryApiUrl(String langCode) {
        if (TextUtils.isEmpty(langCode))
            langCode = pref.getLanguageCodeSpell4Wiki();
        return String.format(Urls.WIKTIONARY, langCode);
    }

    private static OkHttpClient getOkHttpClient(Context context) {

        pref = new PrefManager(context);
        OkHttpClient.Builder okHttpClient = new OkHttpClient().newBuilder();
        okHttpClient.retryOnConnectionFailure(true);

        ClearableCookieJar cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));
        okHttpClient.cookieJar(cookieJar);

        return okHttpClient.build();
    }
}
