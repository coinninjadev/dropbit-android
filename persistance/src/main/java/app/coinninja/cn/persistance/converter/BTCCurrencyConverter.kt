package app.coinninja.cn.persistance.converter

import androidx.room.TypeConverter
import app.dropbit.commons.currency.BTCCurrency

object BTCCurrencyConverter {

    @TypeConverter
    @JvmStatic
    fun toBtcCurrency(value: Long): BTCCurrency {
        return BTCCurrency(value)
    }

    @TypeConverter
    @JvmStatic
    fun toValue(btcCurrency: BTCCurrency): Long? {
        return btcCurrency.toLong()
    }
}
