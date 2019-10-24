package app.dropbit.commons.util

import android.os.Build
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter


fun LocalDateTime.toIso8601TimeStamp(): String {
    val formatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'")
    } else {
        TODO("VERSION.SDK_INT < O")
    }
    return formatter.format(this)
}


fun LocalDateTime.toMillis(): Long = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    this.toInstant(UTC).toEpochMilli()
} else {
    0
}