package app.coinninja.cn.persistance.converter

import androidx.room.TypeConverter
import app.coinninja.cn.persistance.model.MemPoolState

object MemPoolStateConverter {
    @TypeConverter
    @JvmStatic
    fun to(id: Int?): MemPoolState? = id?.let { return MemPoolState.from(it) }

    @TypeConverter
    @JvmStatic
    fun from(state: MemPoolState?): Int? = state?.let { return it.id }

}
