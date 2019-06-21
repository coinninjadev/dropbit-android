package app.dropbit.commons.util

import app.dropbit.annotations.Mockable

@Mockable
class DateUtil {
    fun timeInSeconds(): Long {
        return System.currentTimeMillis() / 1000
    }

}