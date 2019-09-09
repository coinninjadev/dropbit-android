package app.coinninja.cn.persistance.converter

import android.annotation.SuppressLint
import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.*


@SuppressLint("SimpleDateFormat", "ConstantLocale")
object DateTimeConverter {
    private val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'").also {
        it.timeZone = TimeZone.getTimeZone("GMT")
    }

    @TypeConverter
    @JvmStatic
    fun toOffsetDateTime(value: String?): Date? {
        return value?.let {
            try  {
                return formatter.parse(it)
            } catch (e: Exception) {
                return null
            }
        }
    }

    @TypeConverter
    @JvmStatic
    fun fromOffsetDateTime(date: Date?): String? {
        return date?.let {
            return formatter.format(it.time)
        }
    }

}
