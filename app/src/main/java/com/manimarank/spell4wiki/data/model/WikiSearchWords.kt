package com.manimarank.spell4wiki.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Model class for Wiki Search Words
 * @property offset Offset?
 * @property query SearchQuery?
 * @constructor
 */
data class WikiSearchWords(
    @SerializedName("continue")
    var offset: Offset? = null,
    @SerializedName("query")
    var query: SearchQuery? = null
)

/**
 * Model class for Offset Object
 * @property nextOffset Int?
 * @constructor
 */
data class Offset(
    @SerializedName("sroffset")
    var nextOffset: Int? = null
)

/**
 * Model Class for Search Query
 * @property wikiTitleList List<WikiWord>
 * @constructor
 */
data class SearchQuery(
    @SerializedName("search")
    @Expose
    var wikiTitleList: List<WikiWord> = ArrayList()
)

/**
 * Model Class for Wiki Word
 * @property pageId Int?
 * @property title String?
 * @constructor
 */
data class WikiWord(
    @SerializedName("pageid")
    var pageId: Int? = null,
    @SerializedName("title")
    var title: String? = null
)
