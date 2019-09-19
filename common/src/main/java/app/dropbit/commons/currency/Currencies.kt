package app.dropbit.commons.currency

fun Double.toUSDCurrency(): USDCurrency = USDCurrency(this)
fun Float.toUSDCurrency(): USDCurrency = this.toDouble().toUSDCurrency()
fun Float.asFormattedUsdCurrencyString(): String = this.toUSDCurrency().toFormattedCurrency()
fun Double.asFormattedUsdCurrencyString(): String = this.toUSDCurrency().toFormattedCurrency()
fun Long.toBTCCurrency(): BTCCurrency = BTCCurrency(this)
