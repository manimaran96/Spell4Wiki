package com.manimarank.spell4wiki.utils

import android.content.Context
import com.google.gson.Gson
import com.manimarank.spell4wiki.R
import org.json.JSONObject
import retrofit2.Response

object ApiErrorUtils {

    /**
     * Extracts an error string directly from a standard Response object. 
     * Prioritizes the Retrofit errorBody, then attempts parsing the object's body logic generically.
     */
    fun <T> getErrorMessage(context: Context, response: Response<T>?): String {
        if (response == null) {
            return context.getString(R.string.something_went_wrong_try_again)
        }

        // Check if there is an errorBody natively
        try {
            response.errorBody()?.string()?.let { errorBodyString ->
                if (errorBodyString.isNotEmpty()) {
                    val extracted = extractWikiErrorFromJson(errorBodyString)
                    if (extracted != null) return extracted
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Check if response body naturally contains a "error" layer within a successful payload
        try {
            response.body()?.let { body ->
                if (body is okhttp3.ResponseBody) {
                    val bodyString = body.string()
                    val extracted = extractWikiErrorFromJson(bodyString)
                    if (extracted != null) return extracted
                } else {
                    val extracted = getErrorFromGenericResponse(body)
                    if (extracted != null) return extracted
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return "${context.getString(R.string.something_went_wrong)} (Code: ${response.code()})"
    }

    /**
     * Extracts an error message from a Throwable.
     */
    fun getErrorMessage(context: Context, t: Throwable?): String {
        val defaultMsg = context.getString(R.string.something_went_wrong_try_again)
        if (t == null) return defaultMsg
        
        if (t is java.net.UnknownHostException || t is java.net.SocketTimeoutException || t is java.net.ConnectException) {
            return context.getString(R.string.check_internet)
        }
        
        return "$defaultMsg\n${t.message}"
    }

    /**
     * Attempts to parse Wikimedia structure raw JSON to extract inner informative error info
     */
    fun extractWikiErrorFromJson(jsonString: String?): String? {
        if (jsonString.isNullOrEmpty()) return null
        try {
            val json = JSONObject(jsonString)
            if (json.has("error")) {
                val errorObj = json.optJSONObject("error")
                return errorObj?.optString("info") ?: errorObj?.optString("code")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Attempts to parse a mapped object via Gson to scan for Wikimedia properties.
     */
    private fun getErrorFromGenericResponse(body: Any?): String? {
        if (body == null) return null
        try {
            val element = Gson().toJsonTree(body)
            if (element.isJsonObject) {
                val obj = element.asJsonObject
                if (obj.has("error") && obj.get("error").isJsonObject) {
                    val errorObj = obj.getAsJsonObject("error")
                    return if (errorObj.has("info") && !errorObj.get("info").isJsonNull) {
                        errorObj.get("info").asString
                    } else if (errorObj.has("code") && !errorObj.get("code").isJsonNull) {
                        errorObj.get("code").asString
                    } else null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
