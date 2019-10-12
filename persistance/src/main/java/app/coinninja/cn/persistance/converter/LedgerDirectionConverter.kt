package app.coinninja.cn.persistance.converter

import androidx.room.TypeConverter
import app.coinninja.cn.persistance.model.LedgerDirection

object LedgerDirectionConverter {

    @TypeConverter
    @JvmStatic
    fun toDirection(id: Int?): LedgerDirection? {
        return id?.let { return LedgerDirection.from(it) }
    }

    @TypeConverter
    @JvmStatic
    fun fromValue(direction: LedgerDirection?): Int? {
        return direction?.let { return it.id }
    }

}
