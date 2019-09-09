package app.coinninja.cn.persistance.converter

import androidx.room.TypeConverter
import app.coinninja.cn.persistance.model.LedgerType

object LedgerTypeConverter {

    @TypeConverter
    @JvmStatic
    fun toLedgerType(id: Int?): LedgerType? = id?.let { return LedgerType.from(it) }

    @TypeConverter
    @JvmStatic
    fun fromLedgerType(type: LedgerType?): Int? = type?.let { return it.id }

}
