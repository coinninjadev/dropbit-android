package com.coinninja.coinkeeper.model.encryptedpayload.v2

import com.google.gson.Gson

data class TransactionNotificationV2(
        var meta: MetaV2?,
        var info: InfoV2?,
        var profile: ProfileV2?,
        var txid: String? = null
) {
    override fun toString(): String {
        return Gson().toJson(this)
    }
}