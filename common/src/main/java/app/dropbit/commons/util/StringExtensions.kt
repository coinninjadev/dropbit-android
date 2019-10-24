package app.dropbit.commons.util

import android.os.Build
import java.net.URLDecoder
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

fun String.hexToBytes(): ByteArray =
        ByteArray(this.length / 2) { this.substring(it * 2, it * 2 + 2).toInt(16).toByte() }

fun String?.isNotNullOrEmpty(): Boolean = !this.isNullOrEmpty()

fun String.asDateOrNow(): Date? {
    if (isNullOrEmpty()) {
        return Date(System.currentTimeMillis())
    } else {
        return asDateOrNull()
    }
}

fun String.asDateOrNull(): Date? {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'", Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("GMT")
    try {
        return formatter.parse(this)
    } catch (e: Exception) {
        return null
    }
}

fun String.urlEncode(): String {
    return URLEncoder.encode(this, "UTF-8")
}

fun String.urlDecode(): String {
    return URLDecoder.decode(this, "UTF-8")
}

fun String.toTimeInMillis(): Long = toDate()?.toMillis() ?: 0

fun String.toDate(): LocalDateTime? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'")
    LocalDateTime.parse(this, formatter)
} else {
    null
}
