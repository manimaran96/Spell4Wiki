package com.manimarank.spell4wiki.data.apis

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.manimarank.spell4wiki.BuildConfig
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.utils.Print.error
import com.manimarank.spell4wiki.utils.constants.Urls
import com.manimarank.spell4wiki.utils.extensions.makeNullIfEmpty
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.KeyStore
import java.security.KeyStoreException
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

object ApiClient {
    private var retrofitCommons: Retrofit? = null
    private var retrofitApi: Retrofit? = null

    @SuppressLint("StaticFieldLeak")
    private var pref: PrefManager? = null

    fun getCommonsApi(context: Context): Retrofit {
        if (retrofitCommons == null) {
            retrofitCommons = Retrofit.Builder()
                    .baseUrl(Urls.COMMONS)
                    .client(getOkHttpClient(context))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
        }
        return retrofitCommons!!
    }

    fun getWiktionaryApi(context: Context, langCode: String): Retrofit {
        return Retrofit.Builder()
                .baseUrl(getWiktionaryApiUrl(langCode))
                .client(getOkHttpClient(context))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    val api: Retrofit
        get() {
            if (retrofitApi == null) {
                retrofitApi = Retrofit.Builder()
                        .baseUrl("https://github.com")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
            }
            return retrofitApi!!
        }

    private fun getWiktionaryApiUrl(langCode: String): String {
        return String.format(Urls.WIKTIONARY, langCode.makeNullIfEmpty() ?: pref?.languageCodeSpell4WikiAll)
    }


    private fun getOkHttpClient(context: Context): OkHttpClient {
        pref = PrefManager(context)
        val okHttpClient = OkHttpClient().newBuilder()
        okHttpClient.retryOnConnectionFailure(true)
        if (BuildConfig.DEBUG) {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            okHttpClient.addInterceptor(interceptor)
        }
        val cookieJar: ClearableCookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(context))
        okHttpClient.cookieJar(cookieJar)
        return enableTls12OnPreLollipop(okHttpClient).build()
    }

    private fun enableTls12OnPreLollipop(client: OkHttpClient.Builder): OkHttpClient.Builder {
        if (Build.VERSION.SDK_INT < 22) {
            try {
                val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                try {
                    trustManagerFactory.init(null as KeyStore?)
                } catch (e: KeyStoreException) {
                    e.printStackTrace()
                }
                val trustManagers = trustManagerFactory.trustManagers
                check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) { "Unexpected default trust managers:" + Arrays.toString(trustManagers) }
                val trustManager = trustManagers[0] as X509TrustManager
                val sc = SSLContext.getInstance("TLSv1.2")
                sc.init(null, null, null)
                client.sslSocketFactory(Tls12SocketFactory(sc.socketFactory), trustManager)
                val cs = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                        .tlsVersions(TlsVersion.TLS_1_2)
                        .build()
                val specs: MutableList<ConnectionSpec> = ArrayList()
                specs.add(cs)
                specs.add(ConnectionSpec.COMPATIBLE_TLS)
                specs.add(ConnectionSpec.CLEARTEXT)
                client.connectionSpecs(specs)
            } catch (exc: Exception) {
                error("OkHttpTLSCompat ---> Error while setting TLS 1.2 \n$exc")
            }
        }
        return client
    }
}