package com.coinninja.coinkeeper.service.client.model

import app.dropbit.annotations.Mockable

@Mockable
class SentInvite (
        var id: String = "",
        var created_at: Long = 0,
        var updated_at: Long = 0,
        var address: String? = null,
        var sender: String? = null,
        var status: String = "new",
        var metadata: InviteMetadata = InviteMetadata(),
        var phone_number_hash: String? = null,
        var txid: String? = null,
        var wallet_id: String? = null,
        var addressPubKey: String? = null,
        var delivery_id: String = ""
) {

    val created_at_millis: Long get() =  created_at * 1000

    val isDuplicate: Boolean get() = delivery_id == "duplicate"
}
