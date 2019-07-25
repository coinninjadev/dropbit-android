package com.coinninja.coinkeeper.service.client.model

import app.dropbit.annotations.Mockable

@Mockable
data class ScriptSig(var asm: String = "", var hex: String = "") {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ScriptSig

        if (asm != other.asm) return false
        if (hex != other.hex) return false

        return true
    }

    override fun hashCode(): Int {
        var result = asm.hashCode()
        result = 31 * result + hex.hashCode()
        return result
    }
}
