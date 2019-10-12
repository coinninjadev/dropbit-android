package com.coinninja.coinkeeper.cn.transaction

import app.coinninja.cn.thunderdome.repository.ThunderDomeRepository
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.model.query.TransactionQueryManager
import javax.inject.Inject

@Mockable
class LightningWithdrawalLinker @Inject constructor(
        val transactionQueryManager: TransactionQueryManager,
        val thunderDomeRepository: ThunderDomeRepository
) {
    fun linkWithdraws() {
        thunderDomeRepository.withdrawsFromAccount.forEach { invoice ->
            transactionQueryManager.transactionByTxid(invoice.serverId)?.let { transaction ->
                transaction.isLightningWithdraw = true
                transaction.update()
            }
        }
    }
}
