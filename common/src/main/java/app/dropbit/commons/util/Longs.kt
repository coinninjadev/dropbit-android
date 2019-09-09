package app.dropbit.commons.util

import java.text.DecimalFormat

fun Long.decimalFormat(): String {
    return DecimalFormat("#,###.##").format(this)
}
