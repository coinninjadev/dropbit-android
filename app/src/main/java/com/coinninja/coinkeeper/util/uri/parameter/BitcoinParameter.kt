package com.coinninja.coinkeeper.util.uri.parameter

import javax.annotation.Nonnull

enum class BitcoinParameter {
    AMOUNT, BIP70, MEMO, REQUIRED_FEE;

    val parameterKey: String
        get() = when (this) {
            AMOUNT -> "amount"
            BIP70 -> "request"
            MEMO -> "memo"
            REQUIRED_FEE -> "required_fee"
        }

    companion object {
        fun from(@Nonnull param: String?): BitcoinParameter? {
            when (param) {
                "amount" -> return AMOUNT
                "r" -> return BIP70
                "request" -> return BIP70
                "memo" -> return MEMO
                "required_fee" -> return REQUIRED_FEE
            }
            return null
        }
    }
}