package app.dropbit.commons.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter


@RequiresApi(Build.VERSION_CODES.O)
fun LocalDateTime.toIso8601TimeStamp(): String = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(this)


@RequiresApi(Build.VERSION_CODES.O)
fun LocalDateTime.toMillis(): Long = this.toInstant(UTC).toEpochMilli()
