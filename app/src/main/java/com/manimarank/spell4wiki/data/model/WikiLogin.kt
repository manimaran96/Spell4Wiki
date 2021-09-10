package com.manimarank.spell4wiki.data.model

import com.google.gson.annotations.SerializedName

/**
 * Model Class for Wiki Login
 * @property clientLogin ClientLogin?
 * @constructor
 */
data class WikiLogin(
    @SerializedName("clientlogin")
    var clientLogin: ClientLogin? = null
)