package app.coinninja.cn.persistance.converter

import androidx.room.TypeConverter
import app.coinninja.cn.persistance.model.IdentityType

object IdentityTypeConverter {

    @TypeConverter
    @JvmStatic
    fun to(id: Int?): IdentityType? = id?.let { return IdentityType.from(it) }

    @TypeConverter
    @JvmStatic
    fun from(type: IdentityType?): Int? = type?.let { return it.id }

}
