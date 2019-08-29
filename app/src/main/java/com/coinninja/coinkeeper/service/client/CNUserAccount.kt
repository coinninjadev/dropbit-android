package com.coinninja.coinkeeper.service.client

import app.dropbit.annotations.Mockable
import com.google.gson.annotations.SerializedName

@Mockable
data class CNUserAccount(
        var id: String = "",
        var status: String = "",
        var wallet_id: String = "",
        @SerializedName("private")
        var isPrivate: Boolean = false,
        var identities: List<CNUserIdentity> = emptyList(),
        @SerializedName("phone_number_hash")
        var phoneNumberHash: String = ""
)

