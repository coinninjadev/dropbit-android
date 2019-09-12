package com.coinninja.coinkeeper.model.helpers

import app.coinninja.cn.libbitcoin.model.TransactionData
import app.coinninja.cn.libbitcoin.model.UnspentTransactionOutput
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.model.db.FundingStat
import com.coinninja.coinkeeper.model.db.FundingStatDao
import com.coinninja.coinkeeper.model.db.TransactionSummary
import com.coinninja.coinkeeper.service.client.model.VIn
import javax.inject.Inject

@Mockable
class FundingStatHelper @Inject constructor(
        internal val daoSessionManager: DaoSessionManager,
        internal val addressHelper: AddressHelper
) {
    internal fun fundingStatFor(transactionId: Long, input: VIn, address: String? = null): FundingStat? =
            daoSessionManager.fundingStatDao.queryBuilder().where(
                    FundingStatDao.Properties.Tsid.eq(transactionId),
                    FundingStatDao.Properties.FundedTransaction.eq(input.txid),
                    FundingStatDao.Properties.Value.eq(input.previousOutput.value),
                    FundingStatDao.Properties.Position.eq(input.previousOutput.index),
                    FundingStatDao.Properties.Addr.eq(address
                            ?: input.previousOutput.scriptPubKey.addresses[0])
            ).limit(1).unique()

    fun getOrCreateFundingStat(transaction: TransactionSummary, input: VIn, address: String? = null): FundingStat =
            (fundingStatFor(transaction.id, input, address)
                    ?: daoSessionManager.newFundingStat()).also {
                it.addr = address ?: input.previousOutput.scriptPubKey.addresses[0]
                it.position = input.previousOutput.index
                it.transaction = transaction
                it.fundedTransaction = input.txid
                it.value = input.previousOutput.value
                if (it.id == null) {
                    daoSessionManager.insert(it)
                } else {
                    it.update()
                }
            }

    internal fun createInputFor(utxo: UnspentTransactionOutput): FundingStat = daoSessionManager.newFundingStat().apply {
        utxo.path?.let { path ->
            fundedTransaction = utxo.txid
            position = utxo.index
            value = utxo.amount
            addressHelper.addressForPath(path)?.let {
                address = it
                addr = it.address
            }

        }
    }

    fun createInputsFor(transaction: TransactionSummary, transactionData: TransactionData) {
        transactionData.utxos.forEach {
            val input = createInputFor(it)
            input.transaction = transaction
            input.wallet = transaction.wallet
            input.state = FundingStat.State.PENDING
            daoSessionManager.insert(input)
        }
    }

}
