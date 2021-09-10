package com.manimarank.spell4wiki.data.model

import com.google.gson.annotations.SerializedName

/**
 * Model Class for Client Login
 * @property status String?
 * @property username String?
 * @property message String?
 * @constructor
 */
data class ClientLogin(
    @SerializedName("status")
    var status: String? = null,
    @SerializedName("username")
    var username: String? = null,
    @SerializedName("message")
    var message: String? = null
)