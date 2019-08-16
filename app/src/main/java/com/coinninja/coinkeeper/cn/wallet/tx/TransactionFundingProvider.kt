package com.coinninja.coinkeeper.cn.wallet.tx

import com.coinninja.coinkeeper.BuildConfig
import com.coinninja.coinkeeper.cn.account.AccountManager
import com.coinninja.coinkeeper.cn.wallet.LibBitcoinProvider
import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper
import com.coinninja.coinkeeper.model.helpers.TargetStatHelper
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import dagger.Module
import dagger.Provides

@Module
class TransactionFundingProvider {

    @Provides
    fun provideTrasnasctionFundingConfiguration(libBitcoinProvider: LibBitcoinProvider, walletHelper: WalletHelper,
                                                accountManager: AccountManager, targetStatHelper: TargetStatHelper,
                                                inviteTransactionSummaryHelper: InviteTransactionSummaryHelper): FundingModel {
        return FundingModel(libBitcoinProvider, targetStatHelper, inviteTransactionSummaryHelper, walletHelper, accountManager, BuildConfig.DUST_AMOUNT_SATOSHIS)
    }
}