package com.coinninja.coinkeeper.service.client.model


data class Amount(val btc: Long, val usd: Long)

data class Sender(val type:String, val identity:String, val handle:String? = null)

data class Receiver(val type:String, val identity:String, val handle:String? = null)

data class InviteUserPayload(val amount: Amount, val sender: Sender, val receiver: Receiver, val request_id: String)
