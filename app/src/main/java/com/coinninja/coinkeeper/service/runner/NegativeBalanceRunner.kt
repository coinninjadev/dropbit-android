package com.coinninja.coinkeeper.service.runner

import app.coinninja.cn.thunderdome.repository.ThunderDomeRepository
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.cn.dropbit.DropBitCancellationManager
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import javax.inject.Inject

@Mockable
class NegativeBalanceRunner @Inject constructor(
        internal val cancellationService: DropBitCancellationManager,
        internal val thunderDomeRepository: ThunderDomeRepository,
        internal val walletHelper: WalletHelper
) : Runnable {
    override fun run() {
        if (walletHelper.buildBalances(walletHelper.primaryWallet, false) < 0) {
            cancellationService.markUnfulfilledAsCanceled()
        }

        if (thunderDomeRepository.availableBalance < 0) {
            cancellationService.markUnfulfilledLightningAsCanceled()
        }
    }

}