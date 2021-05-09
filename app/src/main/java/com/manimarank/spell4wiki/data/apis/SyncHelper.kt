package com.manimarank.spell4wiki.data.apis

import android.text.TextUtils
import com.manimarank.spell4wiki.Spell4WikiApp
import com.manimarank.spell4wiki.data.model.WikiBaseData
import com.manimarank.spell4wiki.data.db.DBHelper
import com.manimarank.spell4wiki.data.db.entities.WikiLang
import com.manimarank.spell4wiki.data.prefs.AppPref.INSTANCE.setCommonCategories
import com.manimarank.spell4wiki.data.prefs.AppPref.INSTANCE.setFetchBy
import com.manimarank.spell4wiki.data.prefs.AppPref.INSTANCE.setFetchDir
import com.manimarank.spell4wiki.data.prefs.AppPref.INSTANCE.setFetchLimit
import com.manimarank.spell4wiki.data.prefs.AppPref.INSTANCE.setUpdateAppType
import com.manimarank.spell4wiki.data.prefs.AppPref.INSTANCE.setUpdateAppVersion
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.Exception as KotlinException

/**
 * Sync Helper for sync the configurations - app, language, categories, app update
 */
class SyncHelper {
    /**
     * Method to Sync Wiki Languages
     */
    fun syncWikiLanguages() {
        // Sync Wiki Language List
        val dbHelper = DBHelper.getInstance(Spell4WikiApp.getApplicationContext())
        val api = ApiClient.api.create(ApiInterface::class.java)
        val wikiBaseDataCall = api.fetchWikiBaseData()
        wikiBaseDataCall.enqueue(object : Callback<WikiBaseData?> {
            override fun onResponse(call: Call<WikiBaseData?>, response: Response<WikiBaseData?>) {
                try {
                    if (response.isSuccessful && response.body() != null) {
                        val data = response.body()!!
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
                        if (languageList != null && languageList.size > 0) {
                            dbHelper.appDatabase.wikiLangDao.deleteAll()
                            for ((code, name, direction, localName, titleOfWordsWithoutAudio1, category) in languageList) {
                                var titleOfWordsWithoutAudio: String? = null
                                if (!TextUtils.isEmpty(titleOfWordsWithoutAudio1)) titleOfWordsWithoutAudio = titleOfWordsWithoutAudio1
                                val isLeftToRightDirection = direction != null && direction == "ltr"
                                var categories: List<String?>? = ArrayList()
                                if (category != null && category.size > 0) categories = category
                                val dbLang = WikiLang(code!!, name, localName, titleOfWordsWithoutAudio, isLeftToRightDirection, categories)
                                dbHelper.appDatabase.wikiLangDao.insert(dbLang)
                            }
                        }
                    }
                } catch (e: KotlinException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<WikiBaseData?>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }
}