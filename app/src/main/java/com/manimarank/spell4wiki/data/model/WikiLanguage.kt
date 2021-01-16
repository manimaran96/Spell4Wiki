package com.manimarank.spell4wiki.data.model

import com.google.gson.annotations.SerializedName

/**
 * Model Class for Wiki Language
 * @property code String?
 * @property name String?
 * @property direction String?
 * @property localName String?
 * @property titleOfWordsWithoutAudio String?
 * @property category List<String>?
 * @constructor
 */
data class WikiLanguage(
    @SerializedName("code")
    var code: String? = null,
    @SerializedName("lang")
    var name: String? = null,
    @SerializedName("dir")
    var direction: String? = null,
    @SerializedName("local_lang")
    var localName: String? = null,
    @SerializedName("title_of_words_list")
    var titleOfWordsWithoutAudio: String? = null,
    @SerializedName("category")
    var category: List<String>? = null
)