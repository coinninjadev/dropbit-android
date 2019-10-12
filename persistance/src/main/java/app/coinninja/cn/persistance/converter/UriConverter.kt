package app.coinninja.cn.persistance.converter

import android.net.Uri
import androidx.room.TypeConverter

object UriConverter {

    @TypeConverter
    @JvmStatic
    fun toUri(value: String): Uri {
        return Uri.parse(value)
    }

    @TypeConverter
    @JvmStatic
    fun toValue(uri: Uri): String {
        return uri.toString()
    }
}

