package app.dropbit.commons.util

import app.dropbit.annotations.Mockable

@Mockable
class DateUtil {
    fun timeInSeconds(): Long {
        return timeInMillis() / 1000
    }

    fun timeInMillis():Long {
        return System.currentTimeMillis()
    }

}