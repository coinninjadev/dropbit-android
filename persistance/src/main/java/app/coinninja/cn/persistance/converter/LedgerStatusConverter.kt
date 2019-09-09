package app.coinninja.cn.persistance.converter

import androidx.room.TypeConverter
import app.coinninja.cn.persistance.model.LedgerStatus

object LedgerStatusConverter {

    @TypeConverter
    @JvmStatic
    fun toLedgerStatus(id: Int?): LedgerStatus? = LedgerStatus.from(id)

    @TypeConverter
    @JvmStatic
    fun fromLedgerStatus(status: LedgerStatus?): Int? = status?.let { return it.id }
}

