package app.dropbit.commons.util

const val SECOND_MILLIS = 1000
const val MINUTE_MILLIS = 60 * SECOND_MILLIS
const val HOUR_MILLIS = 60 * MINUTE_MILLIS
const val DAY_MILLIS = 24 * HOUR_MILLIS

fun Long.makeRelativeTime(): String {


    var time = this
    if (time < 1000000000000L) {
        time *= 1000
    }

    val now = System.currentTimeMillis()
    if (time > now) {
        return "in the future"
    } else if (time <= 0) {
        return "before recorded history"
    }

    val diff = now - time
    return if (diff < MINUTE_MILLIS) {
        "just now"
    } else if (diff < 2 * MINUTE_MILLIS) {
        "moments ago"
    } else if (diff < 50 * MINUTE_MILLIS) {
        (diff / MINUTE_MILLIS).toString() + " minutes ago"
    } else if (diff < 90 * MINUTE_MILLIS) {
        "1 hour ago"
    } else if (diff < 24 * HOUR_MILLIS) {
        (diff / HOUR_MILLIS).toString() + " hours ago"
    } else if (diff < 48 * HOUR_MILLIS) {
        "yesterday"
    } else {
        (diff / DAY_MILLIS).toString() + " days ago"
    }
}
