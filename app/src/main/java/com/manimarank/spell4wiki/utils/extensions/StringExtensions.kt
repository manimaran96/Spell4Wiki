package com.manimarank.spell4wiki.utils.extensions

/**
 * Make string as null if null/empty string
 */
fun String?.makeNullIfEmpty(): String? {
    return if (this.isNullOrBlank()) null else this
}