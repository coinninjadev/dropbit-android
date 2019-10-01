package com.coinninja.coinkeeper.ui.lightning.deposit

import app.coinninja.cn.thunderdome.repository.ThunderDomeRepository
import com.coinninja.coinkeeper.cn.transaction.FundingViewModelProvider
import com.coinninja.coinkeeper.cn.wallet.tx.FundingModel
import com.coinninja.coinkeeper.cn.wallet.tx.TransactionFundingManager
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.util.FeesManager
import dagger.Module
import dagger.Provides

@Module
class LightningDepositActivityModule {
    @Provides
    fun fundingViewModelProvider(
            signedCoinKeeperApiClient: SignedCoinKeeperApiClient,
            thunderDomeRepository: ThunderDomeRepository,
            transactionFundingManager: TransactionFundingManager,
            feesManager: FeesManager,
            fundingModel: FundingModel
    ): FundingViewModelProvider {
        return FundingViewModelProvider(signedCoinKeeperApiClient, thunderDomeRepository, transactionFundingManager, feesManager, fundingModel)
    }

}
