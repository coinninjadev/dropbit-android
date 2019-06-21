package com.coinninja.coinkeeper.service.client.model

import app.dropbit.annotations.Mockable
import com.google.gson.annotations.SerializedName

@Mockable
data class AddressStats(
        var address: String? = null,
        var balance: Long = 0,
        var received: Long = 0,
        var spent: Long = 0,
        @SerializedName("tx_count")
        var numTransactions: Int = 0
)
