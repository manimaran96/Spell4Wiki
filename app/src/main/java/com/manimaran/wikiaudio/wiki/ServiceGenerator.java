package com.manimaran.wikiaudio.wiki;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.google.gson.Gson;
import com.manimaran.wikiaudio.util.PrefManager;
import com.manimaran.wikiaudio.util.UrlType;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator {

    private static Retrofit retrofit;

    private static PrefManager pref;

    // Retrofit builder init with api url
    private static Retrofit.Builder builder = new Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create());

    // Logging method
    private static HttpLoggingInterceptor logging = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY); // Log the response body

    private static Interceptor queryParamInterceptor = new Interceptor() {
        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request original = chain.request();
            HttpUrl originalHttpUrl = original.url();

            HttpUrl url = originalHttpUrl.newBuilder().addQueryParameter("format", "json").build();

            // Request customization: add request headers
            Request.Builder requestBuilder = original.newBuilder().url(url);

            Request request = requestBuilder.build();
            return chain.proceed(request);
        }
    };

    /**
     * A persistent CookieJar implementation for OkHttp 3 based on SharedPreferences.
     * OkHttp 2/HTTPUrlConnection persistent CookieStore
     */
    private static PersistentCookieJar cookieJar = null;

    // OkHTTP Client builder init
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    // Create service
    public static <S> S createService(Class<S> serviceClass, Context context, int urlType) {



        builder.baseUrl(getUrl(urlType, context));

        // Setting cookie
        if (cookieJar == null) {
            cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));
            httpClient.cookieJar(cookieJar);
            httpClient.connectTimeout(30, TimeUnit.SECONDS);
            httpClient.readTimeout(30, TimeUnit.SECONDS);
        }
        boolean rebuild = false;
        if (!httpClient.interceptors().contains(logging)) {
            httpClient.addInterceptor(logging);
            rebuild = true;
        }
        if (!httpClient.interceptors().contains(queryParamInterceptor)) {
            httpClient.addInterceptor(queryParamInterceptor);
            rebuild = true;
        }

        //if (rebuild) {
            builder.client(httpClient.build());
            retrofit = builder.build();
        //}

        return retrofit.create(serviceClass);
    }

    /*public void setBaseUrl(String url) {
        retrofit.baseUrl(url).build();
    }*/

    public static String getUrl(int urlType, Context context) {

        String url = "";
        switch (urlType)
        {
            case UrlType.COMMONS :
                url = "https://commons.wikimedia.org/";
                break;
            case UrlType.WIKTIONARY:
                pref = new PrefManager(context);
                url = String.format("https://%s.wiktionary.org/", pref.getLangCode());
                break;
            default:
                url = "https://commons.wikimedia.org/";
        }

        return url;
    }

    // Clear cookies after logout
    public static void clearCookies() {
        if (cookieJar != null)
            cookieJar.clear();
    }

    public static void checkCookies()
    {
        if(cookieJar != null)
        {
            Log.e("TAG", "Wiki log cookie " + new Gson().toJson(cookieJar));
        }
    }
}
