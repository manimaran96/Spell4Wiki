package com.manimarank.spell4wiki.data.model

import com.google.gson.annotations.SerializedName

/**
 * Model Class for Wiki Upload
 * @property success WikiSuccess?
 * @property error WikiError?
 * @constructor
 */
data class WikiUpload(
    @SerializedName("upload")
    var success: WikiSuccess? = null,
    @SerializedName("error")
    var error: WikiError? = null
)

/**
 * Model Class for Wiki Success
 * @property result String?
 * @property filename String?
 * @constructor
 */
data class WikiSuccess(
    @SerializedName("result")
    var result: String? = null,
    @SerializedName("filename")
    var filename: String? = null
)

/**
 * Model Class for Wiki Error
 * @property code String?
 * @property info String?
 * @constructor
 */
data class WikiError(
    @SerializedName("code")
    var code: String? = null,
    @SerializedName("info")
    var info: String? = null
)