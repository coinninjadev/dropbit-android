package com.coinninja.coinkeeper.util

import android.annotation.SuppressLint
import app.dropbit.annotations.Mockable
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@Mockable
class DateUtil @SuppressLint("SimpleDateFormat") @Inject constructor() {
    companion object {
        private const val DEFAULT_PATTERN: String = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    }

    val dateFormat: SimpleDateFormat
    val date: Date

    init {
        dateFormat = SimpleDateFormat(DEFAULT_PATTERN)
        dateFormat.timeZone = TimeZone.getTimeZone("GMT")
        date = Date()
    }

    fun getCurrentTimeInMillis(): Long {
        return System.currentTimeMillis()
    }

    fun getCurrentTimeFormatted(): String {
        return format(getCurrentTimeInMillis())
    }

    fun format(millis: Long): String {
        date.time = millis
        return dateFormat.format(date)
    }
}

