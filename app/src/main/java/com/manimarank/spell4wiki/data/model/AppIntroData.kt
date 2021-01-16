package com.manimarank.spell4wiki.data.model

import java.io.Serializable

/**
 * Model Class for App Intro Data Items
 * @property imgId Int
 * @property title String
 * @property description String
 * @constructor
 */
data class AppIntroData(
    val imgId: Int,
    val title: String,
    val description: String
) : Serializable