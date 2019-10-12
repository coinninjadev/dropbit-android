package app.coinninja.cn.persistance.model

import androidx.room.TypeConverter

object InputStateConverter {
    @TypeConverter
    @JvmStatic
    fun to(id: Int?): InputState? = id?.let { return InputState.from(it) }

    @TypeConverter
    @JvmStatic
    fun from(state: InputState?): Int? = state?.let { return it.id }


}
