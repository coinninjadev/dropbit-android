package com.coinninja.coinkeeper.service.client

import app.dropbit.annotations.Mockable
import com.google.gson.annotations.SerializedName

@Mockable
data class CNUserAccount(
        var id: String? = null,
        var status: String? = null,
        var wallet_id: String? = null,
        @SerializedName("private")
        var isPrivate: Boolean = false,
        var identities: List<CNUserIdentity>? = null,
        @SerializedName("phone_number_hash")
        var phoneNumberHash: String? = null
)

