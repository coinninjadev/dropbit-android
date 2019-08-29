package com.coinninja.coinkeeper.cn.wallet

import app.dropbit.annotations.Mockable

@Mockable
class WalletFlags(val flag: Long) {

    val versionBit: Long get() = flag and 0xF
    val purposeBit: Long get() = flag shr 4 and 0xF
    val activeBit: Long get() = flag shr 8

    fun hasPurpose(purpose: Long): Boolean {
        return purposeBit == purpose
    }

    fun hasVersion(version: Long): Boolean {
        return versionBit == version
    }

    fun isActive(): Boolean {
        return activeBit == activated
    }

    companion object {
        internal const val deactivated: Long = 0b0001
        internal const val activated: Long = 0b0000

        internal const val purpose49: Long = 0b0000
        internal const val purpose84: Long = 0b0001

        internal const val v0: Long = 0b0000
        internal const val v1: Long = 0b0001
        internal const val v2: Long = 0b0010

        internal fun compose(purpose: Long, version: Long, isActive: Boolean = true): WalletFlags {

            return WalletFlags(
                    (if (isActive) activated else deactivated)
                            shl 4
                            or purpose
                            shl 4
                            or version
            )
        }

        val purpose49v1: Long get() = compose(purpose49, v1).flag

    }
}