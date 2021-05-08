package com.manimarank.spell4wiki.ui.listerners

interface FileAvailabilityCallback {
    fun status(fileExist: Boolean)
}