package com.manimarank.spell4wiki.data.apis;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.manimarank.spell4wiki.data.prefs.PrefManager;
import com.manimarank.spell4wiki.utils.Print;
import com.manimarank.spell4wiki.utils.constants.Urls;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;
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
                    .client(getOkHttpClient(context))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitCommons;
    }

    public static Retrofit getWiktionaryApi(Context context, String langCode) {
        return new Retrofit.Builder()
                .baseUrl(getWiktionaryApiUrl(langCode))
                .client(getOkHttpClient(context))
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

        return enableTls12OnPreLollipop(okHttpClient).build();
    }

    private static OkHttpClient.Builder enableTls12OnPreLollipop(OkHttpClient.Builder client) {
        if (Build.VERSION.SDK_INT < 22) {
            try {

                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                try {
                    trustManagerFactory.init((KeyStore) null);
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                }
                TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
                if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                    throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
                }
                X509TrustManager trustManager = (X509TrustManager) trustManagers[0];


                SSLContext sc = SSLContext.getInstance("TLSv1.2");
                sc.init(null, null, null);
                client.sslSocketFactory(new Tls12SocketFactory(sc.getSocketFactory()), trustManager);

                ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                        .tlsVersions(TlsVersion.TLS_1_2)
                        .build();

                List<ConnectionSpec> specs = new ArrayList<>();
                specs.add(cs);
                specs.add(ConnectionSpec.COMPATIBLE_TLS);
                specs.add(ConnectionSpec.CLEARTEXT);

                client.connectionSpecs(specs);
            } catch (Exception exc) {
                Print.error("OkHttpTLSCompat ---> Error while setting TLS 1.2 \n" + exc.toString());
            }
        }

        return client;
    }
}
