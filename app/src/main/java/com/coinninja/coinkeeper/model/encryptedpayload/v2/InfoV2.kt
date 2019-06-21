package com.coinninja.coinkeeper.model.encryptedpayload.v2

data class InfoV2(
        var memo: String = "",
        var amount: Long = 0,
        var currency: String? = "USD"
)