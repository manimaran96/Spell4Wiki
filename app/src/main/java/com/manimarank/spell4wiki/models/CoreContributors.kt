package com.manimarank.spell4wiki.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CoreContributors(
        val name: String,
        val contribution: String,
        val about: String,
        val link: String,
        @SerializedName("img_link") val imgLink : String?
) : Serializable