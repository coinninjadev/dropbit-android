package app.dropbit.commons.util

import android.os.Build
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC
import java.util.*

fun Long.decimalFormat(): String {
    return DecimalFormat("#,###.##").format(this)
}

fun Long.toRFC3339(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            toLocalDateTimeUTC().toIso8601TimeStamp()
        } else {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'", Locale.getDefault())
            formatter.timeZone = TimeZone.getTimeZone("GMT")
            formatter.format(Date(this))
        }

fun Long.toLocalDateTimeUTC(): LocalDateTime = Instant.ofEpochMilli(this).atZone(UTC).toLocalDateTime()
