package com.coinninja.coinkeeper.ui.segwit

import app.coinninja.cn.thunderdome.repository.ThunderDomeRepository
import com.coinninja.coinkeeper.cn.transaction.FundingViewModelProvider
import com.coinninja.coinkeeper.cn.wallet.tx.FundingModel
import com.coinninja.coinkeeper.cn.wallet.tx.TransactionFundingManager
import com.coinninja.coinkeeper.util.FeesManager
import dagger.Module
import dagger.Provides

@Module
class UpgradeToSegwitActivityModule {
    @Provides
    fun fundingViewModelProvider(
            thunderDomeRepository: ThunderDomeRepository,
            transactionFundingManager: TransactionFundingManager,
            feesManager: FeesManager, fundingModel: FundingModel
    ): FundingViewModelProvider {
        return FundingViewModelProvider(thunderDomeRepository, transactionFundingManager, feesManager, fundingModel)
    }

}
