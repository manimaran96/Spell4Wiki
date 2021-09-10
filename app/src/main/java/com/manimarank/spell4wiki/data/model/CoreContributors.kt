package com.manimarank.spell4wiki.data.model

import com.google.gson.annotations.SerializedName

/**
 * Model Class for Core Contributors Items
 * @property name String
 * @property contribution String
 * @property about String
 * @property link String
 * @property imgLink String?
 * @constructor
 */
data class CoreContributors(
    val name: String,
    val contribution: String,
    val about: String,
    val link: String,
    @SerializedName("img_link") val imgLink: String?
)