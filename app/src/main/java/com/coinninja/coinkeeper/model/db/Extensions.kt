package com.coinninja.coinkeeper.model.db

fun Wallet.isSegwit() = this.purpose == 84
fun Wallet.isLegacy() = this.purpose == 49
