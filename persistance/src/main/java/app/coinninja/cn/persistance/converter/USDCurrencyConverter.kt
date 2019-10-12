package app.coinninja.cn.persistance.converter

import androidx.room.TypeConverter
import app.dropbit.commons.currency.USDCurrency

object USDCurrencyConverter {

    @TypeConverter
    @JvmStatic
    fun toUsdCurrency(value: Long): USDCurrency {
        return USDCurrency(value)
    }

    @TypeConverter
    @JvmStatic
    fun toValue(usdCurrency: USDCurrency): Long? {
        return usdCurrency.toLong()
    }
}
