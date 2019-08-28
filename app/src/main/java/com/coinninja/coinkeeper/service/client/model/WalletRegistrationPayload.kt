package com.coinninja.coinkeeper.service.client.model

import app.dropbit.annotations.Mockable
import com.google.gson.annotations.SerializedName

@Mockable
data class WalletRegistrationPayload(
        @SerializedName("public_key_string")
        val pubKey:String,

        val flags: Long = 0
)
