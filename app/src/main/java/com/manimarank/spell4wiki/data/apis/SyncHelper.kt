package com.manimarank.spell4wiki.data.apis

import android.text.TextUtils
import com.google.gson.Gson
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.Spell4WikiApp
import com.manimarank.spell4wiki.data.db.DBHelper
import com.manimarank.spell4wiki.data.db.entities.WikiLang
import com.manimarank.spell4wiki.data.model.WikiBaseData
import com.manimarank.spell4wiki.data.prefs.AppPref.INSTANCE.setCommonCategories
import com.manimarank.spell4wiki.data.prefs.AppPref.INSTANCE.setFetchBy
import com.manimarank.spell4wiki.data.prefs.AppPref.INSTANCE.setFetchDir
import com.manimarank.spell4wiki.data.prefs.AppPref.INSTANCE.setFetchLimit
import com.manimarank.spell4wiki.data.prefs.AppPref.INSTANCE.setUpdateAppType
import com.manimarank.spell4wiki.data.prefs.AppPref.INSTANCE.setUpdateAppVersion
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.Exception as KotlinException

/**
 * Sync Helper for sync the configurations - app, language, categories, app update
 */
class SyncHelper {
    private fun loadDataFromLocal(){
        try {
            val fileInString: String = Spell4WikiApp.getApplicationContext().resources.openRawResource(
                R.raw.spell4wiki_base
            ).bufferedReader().use {
                it.readText()
            }
            val data = Gson().fromJson(fileInString, WikiBaseData::class.java)
            processBaseData(data)
        } catch (e: KotlinException){
            e.printStackTrace()
        }
    }
    /**
     * Method to Sync Wiki Languages
     */
    fun syncWikiLanguages() {
        // Load data from local
        val dbHelper = DBHelper.getInstance(Spell4WikiApp.getApplicationContext())
        if ((dbHelper.appDatabase.wikiLangDao?.wikiLanguageList?.count() ?: 0) == 0) {
            loadDataFromLocal()
        }

        // Sync Wiki Language List
        val api = ApiClient.api.create(ApiInterface::class.java)
        val wikiBaseDataCall = api.fetchWikiBaseData()
        wikiBaseDataCall.enqueue(object : Callback<WikiBaseData?> {
            override fun onResponse(call: Call<WikiBaseData?>, response: Response<WikiBaseData?>) {
                try {
                    if (response.isSuccessful && response.body() != null) {
                        val data = response.body()!!
                        processBaseData(data)
                    }
                } catch (e: KotlinException) {
                    e.printStackTrace()
                    loadDataFromLocal()
                }
            }

            override fun onFailure(call: Call<WikiBaseData?>, t: Throwable) {
                t.printStackTrace()
                loadDataFromLocal()
            }
        })
    }

    private fun processBaseData(data: WikiBaseData) {
        try {
            val dbHelper = DBHelper.getInstance(Spell4WikiApp.getApplicationContext())
            setCommonCategories(data.categoryCommon ?: listOf())
            if (data.updateApp != null) {
                if (data.updateApp.type != null && data.updateApp.version != null) {
                    setUpdateAppType(data.updateApp.type)
                    setUpdateAppVersion(data.updateApp.version)
                }
            }
            if (data.fetchConfig != null) {
                if (data.fetchConfig.by != null) setFetchBy(data.fetchConfig.by)
                if (data.fetchConfig.dir != null) setFetchDir(data.fetchConfig.dir)
                if (data.fetchConfig.limit != null) setFetchLimit(data.fetchConfig.limit)
            }
            val languageList = data.languageWiseData
            if (languageList != null && languageList.isNotEmpty()) {
                dbHelper.appDatabase.wikiLangDao?.deleteAll()
                for ((code, name, direction, localName, titleOfWordsWithoutAudio1, category) in languageList) {
                    var titleOfWordsWithoutAudio: String? = null
                    if (!TextUtils.isEmpty(titleOfWordsWithoutAudio1)) titleOfWordsWithoutAudio = titleOfWordsWithoutAudio1
                    val isLeftToRightDirection = direction != null && direction == "ltr"
                    var categories: List<String?>? = ArrayList()
                    if (category != null && category.isNotEmpty()) categories = category
                    val dbLang = WikiLang(code!!, name, localName, titleOfWordsWithoutAudio, isLeftToRightDirection, categories)
                    dbHelper.appDatabase.wikiLangDao?.insert(dbLang)
                }
            }
        } catch (e: KotlinException) {
            e.printStackTrace()
        }
    }
}