package app.coinninja.cn.persistance.converter

import androidx.room.TypeConverter
import app.coinninja.cn.persistance.model.SendType

object SendTypeConverter {

    @TypeConverter
    @JvmStatic
    fun to(id: Int?): SendType? = id?.let { return SendType.from(it) }

    @TypeConverter
    @JvmStatic
    fun from(type: SendType?): Int? = type?.let { return it.id }

}
