package com.coinninja.coinkeeper.cn.service

interface PushTokenVerifiedObserver {
    fun onTokenAcquired(token: String)
}
