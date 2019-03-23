package com.manimaran.wikiaudio.wiki;


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

public interface MediaWikiClient {

    // Login token - Commons
    @GET("w/api.php?action=query&meta=tokens&format=json&type=login")
    Call<ResponseBody> getLoginToken();

    // Edit Token - Commons
    @GET("w/api.php?action=query&meta=tokens&format=json&type=csrf")
    Call<ResponseBody> getEditToken();

    // Login - Commons
    @FormUrlEncoded
    @POST("w/api.php?")
    Call<ResponseBody> clientLogin(
            @Field("action") String action,
            @Field("format") String format,
            @Field("loginreturnurl") String url,
            @Field("logintoken") String token,
            @Field("username") String username,
            @Field("password") String password
    );

    // Search Query - Wiktionary
    @GET("w/api.php?action=query&list=search&utf8=&format=json")
    Call<ResponseBody> fetchRecords(
            @Query("srsearch") String searchString,
            @Query("sroffset") Integer offSet
    );


    // Fetch un audio records - Wiktionary
    @GET("w/api.php?action=query&format=json&list=categorymembers&utf8=1&cmlimit=50&cmsort=timestamp&cmdir=desc")
    Call<ResponseBody> fetchUnAudioRecords(
            @Query("cmtitle") String noAudioTitle,
            @Query("cmcontinue") String offsetContinue
    );

    // Upload - commons
    @Multipart
    @POST("w/api.php?")
    Call<ResponseBody> uploadFile(
            @Part("action") RequestBody action,
            @Part("filename") RequestBody filename,
            @Part("token") RequestBody token,
            @Part MultipartBody.Part file,
            @Part("text") RequestBody text,
            @Part("comment") RequestBody comment
    );

    /*

    https://commons.wikimedia.org/wiki/Commons:API/MediaWiki

    errorformat=plaintext --> show error as plain text format
    {{Listen
            | filename    = Accordion chords-01.ogg
            | title       = Accordion chords
            | description = Chords being played on an accordion
    }}
*/
}
