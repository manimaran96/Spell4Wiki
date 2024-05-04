package com.manimarank.spell4wiki.data.model

import com.google.gson.annotations.SerializedName

data class WikiCategoryListItemResponse(
    var batchcomplete: String?,
    @SerializedName("continue")
    var continue_field: Continue?,
    var query: QueryRes?,
)

data class Continue(
    var apcontinue: String?,
    @SerializedName("continue")
    var continue_field: String?,
)

data class QueryRes(
    var allpages: List<CategoryItem>?,
)

data class CategoryItem(
    var pageid: Long?,
    var ns: Long?,
    var title: String?,
)
