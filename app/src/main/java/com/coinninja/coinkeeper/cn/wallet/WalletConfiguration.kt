package com.coinninja.coinkeeper.cn.wallet

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.di.interfaces.DefaultAccount
import com.coinninja.coinkeeper.di.interfaces.DefaultCoin
import com.coinninja.coinkeeper.di.interfaces.DefaultPurpose
import com.coinninja.coinkeeper.di.interfaces.isTestnet
import javax.inject.Inject

@Mockable
class WalletConfiguration @Inject constructor(
        @DefaultPurpose val purpose: Int,
        @DefaultCoin val coin: Int,
        @DefaultAccount val account: Int,
        @isTestnet val isTestNet: Boolean
) {
    val walletConfigurationFlags: Long
        get() =
            if (purpose == 84)
                WalletFlags.purpose84v2
            else
                WalletFlags.purpose49v1
}