package com.coinninja.coinkeeper.ui.lightning.withdrawal

import app.coinninja.cn.thunderdome.repository.ThunderDomeRepository
import com.coinninja.coinkeeper.cn.transaction.FundingViewModelProvider
import com.coinninja.coinkeeper.cn.transaction.TransactionNotificationManager
import com.coinninja.coinkeeper.cn.wallet.tx.FundingModel
import com.coinninja.coinkeeper.cn.wallet.tx.TransactionFundingManager
import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.util.FeesManager
import dagger.Module
import dagger.Provides

@Module
class LightningWithdrawalActivityModule {
    @Provides
    fun fundingViewModelProvider(
            transactionNotificationManager: TransactionNotificationManager,
            inviteTransactionSummaryHelper: InviteTransactionSummaryHelper,
            signedCoinKeeperApiClient: SignedCoinKeeperApiClient,
            thunderDomeRepository: ThunderDomeRepository,
            transactionFundingManager: TransactionFundingManager,
            feesManager: FeesManager,
            fundingModel: FundingModel
    ): FundingViewModelProvider {
        return FundingViewModelProvider(
                transactionNotificationManager,
                inviteTransactionSummaryHelper, signedCoinKeeperApiClient,
                thunderDomeRepository, transactionFundingManager, feesManager, fundingModel
        )
    }

}
