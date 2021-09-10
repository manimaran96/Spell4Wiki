package com.manimarank.spell4wiki.data.model

import com.google.gson.annotations.SerializedName

/**
 * Model Class for Contributors Items
 * @property name String?
 * @property id Int?
 * @property avatarUrl String?
 * @property htmlUrl String?
 * @property contributions Int?
 * @constructor
 */
data class CodeContributors(
    @SerializedName("login")
    var name: String? = null,
    @SerializedName("id")
    var id: Int? = null,
    @SerializedName("avatar_url")
    var avatarUrl: String? = null,
    @SerializedName("html_url")
    var htmlUrl: String? = null,
    @SerializedName("contributions")
    var contributions: Int? = null
)