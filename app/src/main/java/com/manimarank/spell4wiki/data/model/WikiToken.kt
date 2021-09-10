package com.manimarank.spell4wiki.data.model

import com.google.gson.annotations.SerializedName

/**
 * Model Class for Wiki Token
 * @property query Query?
 */
data class WikiToken(
    @SerializedName("query")
    var query: QueryToken? = null
)

/**
 * Model Class for Query Object
 * @property tokenValue TokenValue?
 * @constructor
 */
data class QueryToken(
    @SerializedName("tokens")
    var tokenValue: TokenValue? = null
)

/**
 *
 * Model Class for Token Values
 * @property loginToken String?
 * @property csrfToken String?
 * @constructor
 */
data class TokenValue(
    @SerializedName("logintoken")
    var loginToken: String? = null,
    @SerializedName("csrftoken")
    var csrfToken: String? = null
)