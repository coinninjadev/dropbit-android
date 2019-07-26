package com.coinninja.coinkeeper.service.client.model

import app.dropbit.annotations.Mockable

@Mockable
data class InviteMetadata(
        var amount: MetadataAmount = MetadataAmount(),
        var sender: MetadataContact = MetadataContact(),
        var receiver: MetadataContact = MetadataContact(),
        var suppress: Boolean = false,
        var request_id: String = ""
)

@Mockable
data class MetadataAmount(
        var btc: Long = 0,
        var usd: Long = 0
)

@Mockable
data class MetadataContact(
        var type: String = "",
        var identity: String = "",
        var handle: String? = null
) {

    val cleanedHandle: String?
        get() {
            val value = handle
            return if (value == null) {
                null
            } else {
                if (!value.isNullOrEmpty() && value.startsWith("@")) {
                    value.replaceFirst("@".toRegex(), "")
                } else {
                    value
                }
            }
        }
}



