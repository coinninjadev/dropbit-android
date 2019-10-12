package app.coinninja.cn.persistance.converter

import androidx.room.TypeConverter
import app.coinninja.cn.persistance.model.MessageLevel

object MessageLevelConverter {

    @TypeConverter
    @JvmStatic
    fun to(id: Int?): MessageLevel? = id?.let { return MessageLevel.from(it) }

    @TypeConverter
    @JvmStatic
    fun from(level: MessageLevel?): Int? = level?.let { return it.id }

}
