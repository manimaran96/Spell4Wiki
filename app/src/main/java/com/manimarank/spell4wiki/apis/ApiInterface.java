package com.manimarank.spell4wiki.apis;


import com.manimarank.spell4wiki.models.ContributorData;
import com.manimarank.spell4wiki.models.Contributors;
import com.manimarank.spell4wiki.models.WikiBaseData;
import com.manimarank.spell4wiki.models.WikiLogin;
import com.manimarank.spell4wiki.models.WikiSearchWords;
import com.manimarank.spell4wiki.models.WikiToken;
import com.manimarank.spell4wiki.models.WikiUpload;
import com.manimarank.spell4wiki.models.WikiWordsWithoutAudio;
import com.manimarank.spell4wiki.utils.constants.Urls;

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
    @POST("w/api.php?action=clientlogin&format=json&rememberMe=&loginreturnurl=" + Urls.COMMONS)
    Call<WikiLogin> clientLogin(
            @Field("username") String username,
            @Field("password") String password,
            @Field("logintoken") String token
    );

    @POST("w/api.php?action=logout&format=json")
    Call<ResponseBody> logOut(
            @Field("token") String csrfToken
    );

    // Search Query - Wiktionary
    @GET("w/api.php?action=query&list=search&utf8=1&format=json")
    Call<WikiSearchWords> fetchRecords(
            @Query("srsearch") String searchString,
            @Query("sroffset") Integer offSet
    );


    // Fetch un audio records - Wiktionary - https://www.mediawiki.org/wiki/API:Categorymembers
    @GET("w/api.php?action=query&format=json&list=categorymembers&utf8=1")
    Call<WikiWordsWithoutAudio> fetchUnAudioRecords(
            @Query("cmtitle") String noAudioTitle,
            @Query("cmcontinue") String offsetContinue,
            @Query("cmlimit") Integer limit,
            @Query("cmsort") String sortBy,
            @Query("cmdir") String sortDirection
    );

    // Edit Token - Commons
    @GET("w/api.php?action=query&meta=tokens&format=json&type=csrf")
    Call<WikiToken> getEditToken();

    // Edit Token - Commons
    @GET("w/api.php?action=query&meta=tokens&format=json&type=csrf")
    Call<ResponseBody> getEditTokenEx();

    // Upload - commons
    @Multipart
    @POST("w/api.php?action=upload&format=json")
    Call<WikiUpload> uploadFile(
            @Part("filename") RequestBody filename,
            @Part("token") RequestBody token,
            @Part MultipartBody.Part file,
            @Part("text") RequestBody contentAndLicense,
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
    @GET("https://raw.githubusercontent.com/manimaran96/Spell4Wiki/master/files/base_data/spell4wiki_base.json")
    Call<WikiBaseData> fetchWikiBaseData();

    // Contributor data - Static API
    @GET("https://raw.githubusercontent.com/manimaran96/Spell4Wiki/master/files/base_data/spell4wiki_contributors.json")
    Call<ContributorData> fetchContributorData();

    // Contributors List - Github API
    @GET("https://api.github.com/repos/manimaran96/Spell4Wiki/contributors")
    Call<List<Contributors>> fetchCodeContributorsList();
}
