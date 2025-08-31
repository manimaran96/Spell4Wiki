package com.manimarank.spell4wiki.data.apis

import com.manimarank.spell4wiki.data.model.CodeContributors
import com.manimarank.spell4wiki.data.model.ContributorData
import com.manimarank.spell4wiki.data.model.WikiBaseData
import com.manimarank.spell4wiki.data.model.WikiCategoryListItemResponse
import com.manimarank.spell4wiki.data.model.WikiLogin
import com.manimarank.spell4wiki.data.model.WikiSearchWords
import com.manimarank.spell4wiki.data.model.WikiToken
import com.manimarank.spell4wiki.data.model.WikiUpload
import com.manimarank.spell4wiki.data.model.WikiWordsWithoutAudio
import com.manimarank.spell4wiki.utils.constants.Urls
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

/**
 * Media wiki API - https://mediawiki.org/wiki/API
 */
interface ApiInterface {
    // Login token - Commons
    @get:GET("w/api.php?action=query&meta=tokens&format=json&type=login")
    val loginToken: Call<WikiToken?>

    // Login - Commons
    @FormUrlEncoded
    @POST("w/api.php?action=clientlogin&format=json&rememberMe=&loginreturnurl=" + Urls.COMMONS)
    fun clientLogin(
            @Field("username") username: String?,
            @Field("password") password: String?,
            @Field("logintoken") token: String?
    ): Call<WikiLogin?>

    // Login with OTP - Commons
    @FormUrlEncoded
    @POST("w/api.php?action=clientlogin&format=json&rememberMe=&loginreturnurl=" + Urls.COMMONS)
    fun clientLoginWithOtp(
            @Field("username") username: String?,
            @Field("password") password: String?,
            @Field("logintoken") token: String?,
            @Field("logincontinue") logincontinue: String?,
            @Field("token") otpToken: String?
    ): Call<WikiLogin?>

    @POST("w/api.php?action=logout&format=json")
    fun logOut(
            @Field("token") csrfToken: String?
    ): Call<ResponseBody?>

    // Search Query - Wiktionary
    @GET("w/api.php?action=query&list=search&utf8=1&format=json")
    fun fetchRecords(
            @Query("srsearch") searchString: String?,
            @Query("sroffset") offSet: Int?
    ): Call<WikiSearchWords?>

    // Fetch un audio records - Wiktionary - https://www.mediawiki.org/wiki/API:Categorymembers
    @GET("w/api.php?action=query&format=json&list=categorymembers&utf8=1")
    fun fetchUnAudioRecords(
            @Query("cmtitle") noAudioTitle: String?,
            @Query("cmcontinue") offsetContinue: String?,
            @Query("cmlimit") limit: Int?,
            @Query("cmsort") sortBy: String?,
            @Query("cmdir") sortDirection: String?
    ): Call<WikiWordsWithoutAudio?>

    // Fetch Categories list - Wiktionary - https://www.mediawiki.org/wiki/API:allpages
    @GET("w/api.php?action=query&format=json&list=allpages&apnamespace=14&utf8=1")
    fun fetchCategoryList(
            @Query("apfrom") categorySearchTerm: String?,
            //@Query("apprefix") apPrefix: String?,
            @Query("aplimit") limit: Int?,
            @Query("apcontinue") apContinue: String?,
    ): Call<WikiCategoryListItemResponse?>

    // Edit Token - Commons
    @GET("w/api.php?action=query&meta=tokens&format=json&type=csrf")
    fun getCsrfEditToken(): Call<WikiToken?>

    // Upload - commons
    @Multipart
    @POST("w/api.php?action=upload&format=json")
    fun uploadFile(
            @Part("filename") filename: RequestBody?,
            @Part("token") token: RequestBody?,
            @Part file: MultipartBody.Part?,
            @Part("text") contentAndLicense: RequestBody?,
            @Part("comment") comment: RequestBody?
    ): Call<WikiUpload?>

    // Purge page - Wiktionary
    @POST("w/api.php?action=purge&forcelinksupdate=true&format=json")
    fun purgePage(
            @Query("titles") title: String?
    ): Call<ResponseBody?>

    @FormUrlEncoded
    @POST("w/api.php?")
    fun editPage(
            @Field("action") action: String?,
            @Field("title") title: String?,
            @Field("token") token: String?,
            @Field("format") format: String?,
            @Field("appendtext") appendText: String?
    ): Call<ResponseBody?>

    // Get Wikipedia language list JSON - Static Link
    @GET("https://raw.githubusercontent.com/manimaran96/Spell4Wiki/master/files/base_data/spell4wiki_base.json")
    fun fetchWikiBaseData(): Call<WikiBaseData?>

    // Contributor data - Static API
    @GET("https://raw.githubusercontent.com/manimaran96/Spell4Wiki/master/files/base_data/spell4wiki_contributors.json")
    fun fetchContributorData(): Call<ContributorData?>

    // Contributors List - Github API
    @GET("https://api.github.com/repos/manimaran96/Spell4Wiki/contributors")
    fun fetchCodeContributorsList(): Call<List<CodeContributors?>?>

    /**
     * Search category query
     * https://commons.wikimedia.org/w/api.php?format=json&action=opensearch&namespace=14&limit=30&search=Category:Files uploaded by spell4wiki in
     */
}