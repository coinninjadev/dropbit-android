package app.dropbit.commons.util

import java.text.DecimalFormat


fun Double.decimalFormatted(): String =
        DecimalFormat("#,##0.##").format(this)
