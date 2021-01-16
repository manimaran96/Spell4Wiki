package com.manimarank.spell4wiki.data.model

/**
 * Model Class for Listing Items
 * @property icon Int
 * @property name String
 * @property about String
 * @property url String
 * @property isLottie Boolean
 * @constructor
 */
data class ItemsModel(
    var name: String,
    var about: String,
    var url: String,
    var icon: Int = -1,
    var isLottie: Boolean = false
)