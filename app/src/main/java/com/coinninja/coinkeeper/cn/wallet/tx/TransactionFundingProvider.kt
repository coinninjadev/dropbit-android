package com.coinninja.coinkeeper.cn.wallet.tx

import app.coinninja.cn.libbitcoin.AddressUtil
import com.coinninja.coinkeeper.BuildConfig
import com.coinninja.coinkeeper.cn.account.AccountManager
import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper
import com.coinninja.coinkeeper.model.helpers.TargetStatHelper
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import dagger.Module
import dagger.Provides

@Module
class TransactionFundingProvider {

    @Provides
    fun provideTrasnasctionFundingConfiguration(addressUtil: AddressUtil, walletHelper: WalletHelper,
                                                accountManager: AccountManager, targetStatHelper: TargetStatHelper,
                                                inviteTransactionSummaryHelper: InviteTransactionSummaryHelper): FundingModel {
        return FundingModel(addressUtil, targetStatHelper, inviteTransactionSummaryHelper, walletHelper, accountManager, BuildConfig.DUST_AMOUNT_SATOSHIS)
    }
}