package com.coinninja.coinkeeper.service.client.model

import app.dropbit.annotations.Mockable

@Mockable
data class ScriptPubKey(
        var asm: String = "",
        var hex: String = "",
        var reqSigs: Int = 0,
        var type: String = "",
        var addresses: Array<String> = emptyArray()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ScriptPubKey

        if (asm != other.asm) return false
        if (hex != other.hex) return false
        if (reqSigs != other.reqSigs) return false
        if (type != other.type) return false
        if (!addresses.contentEquals(other.addresses)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = asm.hashCode()
        result = 31 * result + hex.hashCode()
        result = 31 * result + reqSigs
        result = 31 * result + (type?.hashCode() ?: 0)
        result = 31 * result + addresses.contentHashCode()
        return result
    }
}
