package com.gravatar.quickeditor.ui.time

internal interface Clock {
    fun getTimeMillis(): Long
}

internal class SystemClock : Clock {
    override fun getTimeMillis(): Long {
        return System.currentTimeMillis()
    }
}
