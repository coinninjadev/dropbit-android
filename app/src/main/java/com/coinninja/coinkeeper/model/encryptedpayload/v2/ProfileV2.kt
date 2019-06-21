package com.coinninja.coinkeeper.model.encryptedpayload.v2

import com.google.gson.annotations.SerializedName

data class ProfileV2(
        var type: String? = null,
        var identity: String? = null,
        @SerializedName("display_name")
        var displayName: String? = null,
        @SerializedName("dropbit_me")
        var dropbitMe: String? = null,
        var avatar: String? = null
)