package com.coinninja.coinkeeper.bitcoin

interface BroadcastListener {
    fun onBroadcastSuccessful(broadcastResult: BroadcastResult)

    fun onBroadcastProgress(progress: Int)

    fun onBroadcastError(broadcastResult: BroadcastResult)
}
