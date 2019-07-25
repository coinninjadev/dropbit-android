package com.coinninja.coinkeeper.model.helpers

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.model.db.FundingStat
import com.coinninja.coinkeeper.model.db.FundingStatDao
import com.coinninja.coinkeeper.model.db.TransactionSummary
import com.coinninja.coinkeeper.service.client.model.VIn
import javax.inject.Inject

@Mockable
class FundingStatHelper @Inject constructor(
        internal val daoSessionManager: DaoSessionManager
) {
    internal fun fundingStatFor(transactionId: Long, input: VIn): FundingStat? =
            daoSessionManager.fundingStatDao.queryBuilder().where(
                    FundingStatDao.Properties.Tsid.eq(transactionId),
                    FundingStatDao.Properties.FundedTransaction.eq(input.txid),
                    FundingStatDao.Properties.Value.eq(input.previousOutput.value),
                    FundingStatDao.Properties.Position.eq(input.previousOutput.index),
                    FundingStatDao.Properties.Addr.eq(input.previousOutput.scriptPubKey.addresses[0])
            ).limit(1).unique()

    fun getOrCreateFundingStat(transaction: TransactionSummary, input: VIn): FundingStat =
            (fundingStatFor(transaction.id, input) ?: daoSessionManager.newFundingStat()).also {
                it.addr = input.previousOutput.scriptPubKey.addresses[0]
                it.position = input.previousOutput.index
                it.transaction = transaction
                it.fundedTransaction = input.txid
                it.value = input.previousOutput.value
                if (it.id > 0) {
                    it.update()
                } else {
                    daoSessionManager.insert(it)
                }
            }


}
