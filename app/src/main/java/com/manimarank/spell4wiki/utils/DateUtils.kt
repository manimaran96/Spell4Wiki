package com.manimarank.spell4wiki.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class for Date related operations
 */
object DateUtils {
    val DF_YYYY_MM_DD = "yyyy-MM-dd"

    fun getDateToString(toFormat: String): String {
        return SimpleDateFormat(toFormat, Locale.ENGLISH).format(Date())
    }

}