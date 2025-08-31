package com.manimarank.spell4wiki.data.model

import com.google.gson.annotations.SerializedName

/**
 * Model Class for Client Login
 * @property status String?
 * @property username String?
 * @property message String?
 * @property messagecode String?
 * @property requests List<AuthRequest>?
 * @constructor
 */
data class ClientLogin(
    @SerializedName("status")
    var status: String? = null,
    @SerializedName("username")
    var username: String? = null,
    @SerializedName("message")
    var message: String? = null,
    @SerializedName("messagecode")
    var messagecode: String? = null,
    @SerializedName("requests")
    var requests: List<AuthRequest>? = null
)

/**
 * Model Class for Authentication Request (OTP/2FA)
 * @property id String?
 * @property metadata Map<String, Any>?
 * @property required String?
 * @property provider String?
 * @property account String?
 * @property fields Map<String, AuthField>?
 * @constructor
 */
data class AuthRequest(
    @SerializedName("id")
    var id: String? = null,
    @SerializedName("metadata")
    var metadata: Map<String, Any>? = null,
    @SerializedName("required")
    var required: String? = null,
    @SerializedName("provider")
    var provider: String? = null,
    @SerializedName("account")
    var account: String? = null,
    @SerializedName("fields")
    var fields: Map<String, AuthField>? = null
)

/**
 * Model Class for Authentication Field
 * @property type String?
 * @property label String?
 * @property help String?
 * @constructor
 */
data class AuthField(
    @SerializedName("type")
    var type: String? = null,
    @SerializedName("label")
    var label: String? = null,
    @SerializedName("help")
    var help: String? = null
)