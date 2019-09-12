package com.coinninja.coinkeeper.di.module

import app.coinninja.cn.libbitcoin.AddressUtil
import app.coinninja.cn.libbitcoin.SeedWordGenerator
import com.coinninja.coinkeeper.cn.wallet.HDWalletWrapper
import com.coinninja.coinkeeper.cn.wallet.WalletConfiguration
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import dagger.Module
import dagger.Provides

@Module
class LibBitcoinModule {
    @Provides
    fun addressUtil(): AddressUtil = AddressUtil()

    @Provides
    fun hdWallet(walletHelper: WalletHelper, walletConfiguration: WalletConfiguration): HDWalletWrapper =
            HDWalletWrapper(walletHelper, walletConfiguration)

    @Provides
    fun seedwordGenerator(): SeedWordGenerator = SeedWordGenerator()
}
