package com.coinninja.coinkeeper.cn.transaction

import androidx.lifecycle.ViewModelProviders
import app.coinninja.cn.thunderdome.repository.ThunderDomeRepository
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.cn.transaction.notification.FundingViewModel
import com.coinninja.coinkeeper.cn.wallet.tx.FundingModel
import com.coinninja.coinkeeper.cn.wallet.tx.TransactionFundingManager
import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.ui.base.BaseActivity
import com.coinninja.coinkeeper.util.FeesManager

@Mockable
class FundingViewModelProvider constructor(
        val transactionNotificationManager: TransactionNotificationManager,
        val inviteTransactionSummaryHelper: InviteTransactionSummaryHelper,
        val signedCoinKeeperApiClient: SignedCoinKeeperApiClient,
        val thunderDomeRepository: ThunderDomeRepository,
        val transactionFundingManager: TransactionFundingManager,
        val feesManager: FeesManager,
        val fundingModel: FundingModel
) {

    fun provide(activity: BaseActivity): FundingViewModel {
        return bind(ViewModelProviders.of(activity)[FundingViewModel::class.java])
    }

    private fun bind(fundingViewModel: FundingViewModel): FundingViewModel {
        fundingViewModel.transactionNotificationManager = transactionNotificationManager
        fundingViewModel.inviteTransactionSummaryHelper = inviteTransactionSummaryHelper
        fundingViewModel.thunderDomeRepository = thunderDomeRepository
        fundingViewModel.transactionFundingManager = transactionFundingManager
        fundingViewModel.feesManager = feesManager
        fundingViewModel.fundingModel = fundingModel
        fundingViewModel.cnApiClient = signedCoinKeeperApiClient
        return fundingViewModel
    }


}
