package app.dropbit.commons.util

fun String.hexToBytes(): ByteArray =
        ByteArray(this.length / 2) { this.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
