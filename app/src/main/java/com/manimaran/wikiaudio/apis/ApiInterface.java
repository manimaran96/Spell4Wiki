package com.manimaran.wikiaudio.apis;


import com.manimaran.wikiaudio.models.Contributors;
import com.manimaran.wikiaudio.models.WikiLogin;
import com.manimaran.wikiaudio.models.WikiSearchWords;
import com.manimaran.wikiaudio.models.WikiToken;
import com.manimaran.wikiaudio.models.WikiLanguage;
import com.manimaran.wikiaudio.models.WikiWordsWithoutAudio;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;


/**
 * Media wiki API - https://mediawiki.org/wiki/API
 */
public interface ApiInterface {

    // Login token - Commons
    @GET("w/api.php?action=query&meta=tokens&format=json&type=login")
    Call<WikiToken> getLoginToken();

    // Login - Commons
    @FormUrlEncoded
    @POST("w/api.php?action=clientlogin&format=json")
    Call<WikiLogin> clientLogin(
            @Field("username") String username,
            @Field("password") String password,
            @Field("logintoken") String token,
            @Field("loginreturnurl") String url
    );

    // Search Query - Wiktionary
    @GET("w/api.php?action=query&list=search&utf8=1&format=json")
    Call<WikiSearchWords> fetchRecords(
            @Query("srsearch") String searchString,
            @Query("sroffset") Integer offSet
    );


    // Fetch un audio records - Wiktionary
    @GET("w/api.php?action=query&format=json&list=categorymembers&utf8=1&cmlimit=50&cmsort=timestamp&cmdir=desc")
    Call<WikiWordsWithoutAudio> fetchUnAudioRecords(
            @Query("cmtitle") String noAudioTitle,
            @Query("cmcontinue") String offsetContinue
    );

    // Edit Token - Commons
    @GET("w/api.php?action=query&meta=tokens&format=json&type=csrf")
    Call<ResponseBody> getEditToken();

    // Upload - commons
    @Multipart
    @POST("w/api.php?action=upload")
    Call<ResponseBody> uploadFile(
            @Part("filename") RequestBody filename,
            @Part("token") RequestBody token,
            @Part MultipartBody.Part file,
            @Part("text") RequestBody text,
            @Part("comment") RequestBody comment
    );

    // Purge page - Wiktionary
    @POST("w/api.php?action=purge&forcelinksupdate=true&format=json")
    Call<ResponseBody> purgePage(
            @Query("titles") String title
    );

    @FormUrlEncoded
    @POST("w/api.php?")
    Call<ResponseBody> editPage(
            @Field("action") String action,
            @Field("title") String title,
            @Field("token") String token,
            @Field("format") String format,
            @Field("appendtext") String appendText
    );

    // Get Wikipedia language list JSON - Static Link
    @GET("https://raw.githubusercontent.com/manimaran96/Spell4Wiki/master/files/lang-json/language.json")
    Call<List<WikiLanguage>> fetchWikiLanguageList();

    // Contributors List - Github API
    @GET("https://api.github.com/repos/manimaran96/Spell4Wiki/contributors")
    Call<List<Contributors>> fetchContributorsList();
}
