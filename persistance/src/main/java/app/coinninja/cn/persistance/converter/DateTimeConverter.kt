package app.coinninja.cn.persistance.converter

import android.annotation.SuppressLint
import androidx.room.TypeConverter
import app.dropbit.commons.util.toDate
import app.dropbit.commons.util.toIso8601TimeStamp
import app.dropbit.commons.util.toLocalDateTimeUTC
import app.dropbit.commons.util.toMillis
import java.time.LocalDateTime


@SuppressLint("SimpleDateFormat", "ConstantLocale")
object DateTimeConverter {

    @TypeConverter
    @JvmStatic
    fun toDate(dateString: String?): LocalDateTime? = dateString?.toDate()

    @TypeConverter
    @JvmStatic
    fun toDate(timestamp: Long?): LocalDateTime? = timestamp?.toLocalDateTimeUTC()

    @TypeConverter
    @JvmStatic
    fun toTimestamp(date: LocalDateTime?): Long? = date?.toMillis()

    @TypeConverter
    @JvmStatic
    fun dateToString(date: LocalDateTime?): String? = date?.toIso8601TimeStamp()

}
