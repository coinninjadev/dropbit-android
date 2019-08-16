package com.coinninja.coinkeeper.bitcoin

enum class BroadcastProvider(provider: String) {
    BLOCK_STREAM("BlockStream"),
    BLOCKCHAIN_INFO("Blockchain.info"),
    LIBBITCOIN("libbitcoin"),
    COIN_NINJA("Coin Ninja"),
    NONE("NA")
}
