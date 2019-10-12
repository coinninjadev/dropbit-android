package app.coinninja.cn.persistance.converter

import androidx.room.TypeConverter
import app.coinninja.cn.persistance.model.OutPutState

object OutputStateConverter {

    @TypeConverter
    @JvmStatic
    fun to(id: Int?): OutPutState? = id?.let { return OutPutState.from(it) }

    @TypeConverter
    @JvmStatic
    fun from(state: OutPutState?): Int? = state?.let { return it.id }


}
