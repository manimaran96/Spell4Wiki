package com.manimarank.spell4wiki.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data classes for Structured Data on Commons (SDC) API
 * Used for updating structured data on Wikimedia Commons files
 */

/**
 * Response from SDC update API
 */
data class SdcUpdateResponse(
    @SerializedName("success")
    val success: Int? = null,
    @SerializedName("entity")
    val entity: SdcEntity? = null,
    @SerializedName("error")
    val error: WikiError? = null
)

/**
 * Entity information in SDC response
 */
data class SdcEntity(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("labels")
    val labels: Map<String, SdcLabel>? = null,
    @SerializedName("statements")
    val statements: Map<String, List<SdcStatement>>? = null
)

/**
 * Label for SDC entity
 */
data class SdcLabel(
    @SerializedName("language")
    val language: String? = null,
    @SerializedName("value")
    val value: String? = null
)

/**
 * SDC Statement/Claim
 */
data class SdcStatement(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("mainsnak")
    val mainsnak: SdcMainSnak? = null,
    @SerializedName("qualifiers")
    val qualifiers: Map<String, List<SdcQualifier>>? = null,
    @SerializedName("rank")
    val rank: String? = null
)

/**
 * Main snak (value) of a statement
 */
data class SdcMainSnak(
    @SerializedName("snaktype")
    val snaktype: String,
    @SerializedName("property")
    val property: String,
    @SerializedName("datavalue")
    val datavalue: SdcDataValue? = null,
    @SerializedName("datatype")
    val datatype: String? = null
)

/**
 * Data value container
 */
data class SdcDataValue(
    @SerializedName("value")
    val value: Any,
    @SerializedName("type")
    val type: String
)

/**
 * Qualifier for a statement
 */
data class SdcQualifier(
    @SerializedName("snaktype")
    val snaktype: String,
    @SerializedName("property")
    val property: String,
    @SerializedName("datavalue")
    val datavalue: SdcDataValue? = null,
    @SerializedName("datatype")
    val datatype: String? = null
)

/**
 * Wikibase item value (for entity references)
 */
data class WikibaseItemValue(
    @SerializedName("entity-type")
    val entityType: String = "item",
    @SerializedName("numeric-id")
    val numericId: Long,
    @SerializedName("id")
    val id: String
)

/**
 * Time value for date properties
 */
data class TimeValue(
    @SerializedName("time")
    val time: String,
    @SerializedName("timezone")
    val timezone: Int = 0,
    @SerializedName("before")
    val before: Int = 0,
    @SerializedName("after")
    val after: Int = 0,
    @SerializedName("precision")
    val precision: Int = 11, // 11 = day precision
    @SerializedName("calendarmodel")
    val calendarmodel: String = "http://www.wikidata.org/entity/Q1985727"
)
