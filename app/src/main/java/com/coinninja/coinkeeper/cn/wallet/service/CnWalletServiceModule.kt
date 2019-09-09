package com.coinninja.coinkeeper.cn.wallet.service

import com.coinninja.coinkeeper.cn.wallet.CNWalletManager
import com.coinninja.coinkeeper.cn.wallet.runner.SaveRecoveryWordsRunner
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil

import dagger.Module
import dagger.Provides

@Module
class CnWalletServiceModule {
    @Provides
    fun provideSaveRecoveryWordsRunner(cnWalletManager: CNWalletManager,
                                       localBroadCastUtil: LocalBroadCastUtil
    ): SaveRecoveryWordsRunner = SaveRecoveryWordsRunner(cnWalletManager, localBroadCastUtil)
}
