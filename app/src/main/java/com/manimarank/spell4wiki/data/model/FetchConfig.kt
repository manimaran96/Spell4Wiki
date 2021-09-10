package com.manimarank.spell4wiki.data.model

/**
 * Model Class for Fetch Configuration Items
 * @property `by` String?
 * @property dir String?
 * @property limit Int?
 * @constructor
 */
data class FetchConfig(
    val `by`: String?,
    val dir: String?,
    val limit: Int?
)