package app.coinninja.cn.persistance.converter

import androidx.room.TypeConverter
import app.coinninja.cn.persistance.model.BTCState

object BTCStateConverter {

    @TypeConverter
    @JvmStatic
    fun to(id: Int?): BTCState? = id?.let { return BTCState.from(it) }

    @TypeConverter
    @JvmStatic
    fun from(state: BTCState?): Int? = state?.let { return it.id }

}
