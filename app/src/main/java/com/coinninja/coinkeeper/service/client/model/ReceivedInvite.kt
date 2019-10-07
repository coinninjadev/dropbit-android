package com.coinninja.coinkeeper.service.client.model

import app.dropbit.annotations.Mockable

@Mockable
data class ReceivedInvite(
        var id: String = "",
        var created_at: Long = 0,
        var updated_at: Long = 0,
        var address: String? = null,
        var sender: String = "",
        var status: String = "new",
        var request_ttl: String = "",
        var txid: String? = null,
        var address_type: String = "",
        var metadata: InviteMetadata = InviteMetadata()
) {

    val created_at_millis: Long get() = created_at * 1000
}

