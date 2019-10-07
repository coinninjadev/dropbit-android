package com.coinninja.coinkeeper.model.db.enums

enum class Type(val id: Int) {
    BLOCKCHAIN_SENT(0),
    BLOCKCHAIN_RECEIVED(10),
    LIGHTNING_SENT(20),
    LIGHTNING_RECEIVED(30);

    companion object {
        fun sentFrom(value: String) = when (value) {
            "lightning" -> LIGHTNING_SENT
            else -> BLOCKCHAIN_SENT
        }

        fun receivedFrom(value: String): Type = when (value) {
            "lightning" -> LIGHTNING_RECEIVED
            else -> BLOCKCHAIN_RECEIVED
        }
    }

}