package app.dropbit.commons.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

fun Long.decimalFormat(): String {
    return DecimalFormat("#,###.##").format(this)
}

fun Long.toRFC3339(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'").also {
        it.timeZone = TimeZone.getTimeZone("GMT")
    }
    return formatter.format(Date(this))
}