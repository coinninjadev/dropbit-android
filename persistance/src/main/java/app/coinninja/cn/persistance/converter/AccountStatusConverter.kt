package app.coinninja.cn.persistance.converter

import androidx.room.TypeConverter
import app.coinninja.cn.persistance.model.AccountStatus

object AccountStatusConverter {
    @TypeConverter
    @JvmStatic
    fun to(id: Int?): AccountStatus? = id?.let { return AccountStatus.from(it) }

    @TypeConverter
    @JvmStatic
    fun from(status: AccountStatus?): Int? = status?.let { return it.id }
}