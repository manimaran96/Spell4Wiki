package com.manimaran.wikiaudio.apis;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.manimaran.wikiaudio.constants.Urls;
import com.manimaran.wikiaudio.utils.PrefManager;

import java.util.Objects;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofitCommons = null;
    private static Retrofit retrofitApi = null;
    @SuppressLint("StaticFieldLeak")
    private static PrefManager pref;
    private static Interceptor queryParamInterceptor = chain -> {
        Request original = chain.request();
        HttpUrl originalHttpUrl = original.url();

        HttpUrl url = originalHttpUrl.newBuilder().addQueryParameter("format", "json").build();

        // Request customization: add request headers
        Request.Builder requestBuilder = original.newBuilder().url(url);

        Request request = requestBuilder.build();
        return chain.proceed(request);
    };

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

        if (!okHttpClient.interceptors().contains(queryParamInterceptor)) {
            okHttpClient.addInterceptor(queryParamInterceptor);
        }

        return okHttpClient.build();
    }
}
