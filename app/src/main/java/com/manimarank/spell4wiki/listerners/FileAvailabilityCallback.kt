package com.manimarank.spell4wiki.listerners

interface FileAvailabilityCallback {
    fun status(fileExist : Boolean)
}