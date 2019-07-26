package com.coinninja.coinkeeper.cn.wallet.tx

import com.coinninja.coinkeeper.BuildConfig
import com.coinninja.coinkeeper.cn.account.AccountManager
import com.coinninja.coinkeeper.cn.wallet.LibBitcoinProvider
import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper
import com.coinninja.coinkeeper.model.helpers.TargetStatHelper
import dagger.Module
import dagger.Provides

@Module
class TransactionFundingProvider {

    @Provides
    fun provideTrasnasctionFundingConfiguration(libBitcoinProvider: LibBitcoinProvider,
                                                accountManager: AccountManager, targetStatHelper: TargetStatHelper,
                                                inviteTransactionSummaryHelper: InviteTransactionSummaryHelper): FundingModel {
        return FundingModel(libBitcoinProvider, targetStatHelper, inviteTransactionSummaryHelper, accountManager, BuildConfig.DUST_AMOUNT_SATOSHIS)
    }
}