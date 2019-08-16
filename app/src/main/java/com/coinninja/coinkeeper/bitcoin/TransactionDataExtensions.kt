package com.coinninja.coinkeeper.bitcoin

import com.coinninja.bindings.TransactionData

fun TransactionData.isValid(): Boolean = this.isFunded() && !paymentAddress.isNullOrEmpty()
fun TransactionData.isFunded(): Boolean = utxos.isNotEmpty()
fun TransactionData.isNotFunded(): Boolean = utxos.isEmpty()
