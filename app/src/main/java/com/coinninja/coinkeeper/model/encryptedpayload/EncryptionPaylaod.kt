package com.coinninja.coinkeeper.model.encryptedpayload

import com.coinninja.coinkeeper.model.encryptedpayload.v2.MetaV2

data class EncryptionPaylaod(val meta: MetaV2) {
    val version: Int get() = meta.version ?: 0
}