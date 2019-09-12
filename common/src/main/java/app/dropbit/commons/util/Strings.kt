package app.dropbit.commons.util

import java.text.SimpleDateFormat
import java.util.*

fun String.hexToBytes(): ByteArray =
        ByteArray(this.length / 2) { this.substring(it * 2, it * 2 + 2).toInt(16).toByte() }

fun String?.isNotNullOrEmpty(): Boolean = !this.isNullOrEmpty()

fun String.asDateOrNull(): Date? {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'")
    formatter.timeZone = TimeZone.getTimeZone("GMT")
    try {
        return formatter.parse(this)
    } catch (e: Exception) {
        return null
    }
}
