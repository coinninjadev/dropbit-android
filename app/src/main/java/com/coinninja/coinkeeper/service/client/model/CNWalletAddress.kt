package com.coinninja.coinkeeper.service.client.model

import app.dropbit.annotations.Mockable
import com.google.gson.annotations.SerializedName

@Mockable
class CNWalletAddress(
        var id: String = "",
        @SerializedName("created_at")
        var createdAt: Long = 0,
        @SerializedName("updated_at")
        var updateAt: Long = 0,
        var address: String,
        @SerializedName("wallet_id")
        var walletId: String? = null,
        @SerializedName("address_pubkey")
        var publicKey: String? = null
)
