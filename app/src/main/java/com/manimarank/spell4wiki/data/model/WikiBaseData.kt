package com.manimarank.spell4wiki.data.model

import com.google.gson.annotations.SerializedName

/**
 * Model Class for Wiki Base Data
 * @property categoryCommon List<String>?
 * @property languageWiseData List<WikiLanguage>?
 * @property updateApp UpdateApp?
 * @property fetchConfig FetchConfig?
 * @constructor
 */
data class WikiBaseData(
    @SerializedName("category_common")
    var categoryCommon: List<String>? = null,
    @SerializedName("language_wise_data")
    var languageWiseData: List<WikiLanguage>? = null,
    @SerializedName("update_content")
    val updateApp: UpdateApp? = null,
    @SerializedName("fetch_config")
    val fetchConfig: FetchConfig? = null
)
