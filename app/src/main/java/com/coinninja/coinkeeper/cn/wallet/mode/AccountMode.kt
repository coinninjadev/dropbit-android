package com.coinninja.coinkeeper.cn.wallet.mode

enum class AccountMode constructor(val which: Int) {

    LIGHTNING(1), BLOCKCHAIN(0);

    companion object {
        fun from(which: Int): AccountMode = when (which) {
            1 -> LIGHTNING
            else -> BLOCKCHAIN
        }
    }
}