package com.coinninja.coinkeeper.util.uri.parameter

import javax.annotation.Nonnull

enum class BitcoinParameter {
    AMOUNT, BIP70;

    val parameterKey: String
        get() = when (this) {
            AMOUNT -> "amount"
            BIP70 -> "request"
        }

    companion object {
        fun from(@Nonnull param: String?): BitcoinParameter? {
            when (param) {
                "amount" -> return AMOUNT
                "r" -> return BIP70
                "request" -> return BIP70
            }
            return null
        }
    }
}