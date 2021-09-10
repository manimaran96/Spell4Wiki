package com.manimarank.spell4wiki.data.model

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Model class for Wiki words without audio(i.e : list of words don't have audio)
 * @property offset OffsetWords?
 * @property query QueryWords?
 * @constructor
 */
data class WikiWordsWithoutAudio(
    @SerializedName("continue")
    var offset: OffsetWords? = null,
    @SerializedName("query")
    var query: QueryWords? = null
)

/**
 * Model class for Next off set information for pagination
 * @property nextOffset String?
 * @constructor
 */
data class OffsetWords(
    @SerializedName("cmcontinue")
    var nextOffset: String? = null
)

/**
 * Model class for Words list
 * @property wikiTitleList List<WikiTitle>
 * @constructor
 */
data class QueryWords(
    @SerializedName("categorymembers")
    var wikiTitleList: List<WikiTitle> = ArrayList()
)

/**
 * Model class for Words Title in wiktionary
 * @property pageId Int?
 * @property title String?
 * @constructor
 */
data class WikiTitle(
    @SerializedName("pageid")
    var pageId: Int? = null,
    @SerializedName("title")
    var title: String? = null
)