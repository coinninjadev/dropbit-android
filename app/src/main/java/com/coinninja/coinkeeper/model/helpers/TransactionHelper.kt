package com.coinninja.coinkeeper.model.helpers

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.model.db.*
import com.coinninja.coinkeeper.model.db.enums.MemPoolState
import com.coinninja.coinkeeper.model.dto.CompletedBroadcastDTO
import com.coinninja.coinkeeper.model.query.TransactionQueryManager
import com.coinninja.coinkeeper.service.client.model.GsonAddress
import com.coinninja.coinkeeper.service.client.model.TransactionDetail
import com.coinninja.coinkeeper.service.client.model.VIn
import com.coinninja.coinkeeper.service.client.model.VOut
import com.coinninja.coinkeeper.util.DateUtil
import javax.inject.Inject

@Mockable
class TransactionHelper @Inject constructor(
        internal val daoSessionManager: DaoSessionManager,
        internal val walletHelper: WalletHelper,
        internal val fundingStatHelper: FundingStatHelper,
        internal val targetStatHelper: TargetStatHelper,
        internal val addressHelper: AddressHelper,
        internal val transactionInviteSummaryHelper: TransactionInviteSummaryHelper,
        internal val dropbitAccountHelper: DropbitAccountHelper,
        internal val transactionQueryManager: TransactionQueryManager,
        internal val userIdentityHelper: UserIdentityHelper,
        internal val dateUtil: DateUtil
) {

    val transactionsWithoutFees: List<TransactionSummary> get() = transactionQueryManager.transactionsWithoutFees

    val transactionsWithoutHistoricPricing: List<TransactionSummary> get() = transactionQueryManager.transactionsWithoutHistoricPricing

    val incompleteTransactions: List<TransactionSummary> get() = transactionQueryManager.incompleteTransactions

    val pendingMindedTransactions: List<TransactionSummary> get() = transactionQueryManager.pendingMindedTransactions

    val requiringNotificationCheck: List<TransactionSummary> get() = transactionQueryManager.requiringNotificationCheck

    fun getPendingTransactionsOlderThan(olderThanMillis: Long): List<TransactionSummary> {
        return transactionQueryManager.pendingTransactionsOlderThan(olderThanMillis)
    }

    fun initializeTransaction(wallet:Wallet, transaction: TransactionSummary, gsonAddress: GsonAddress) {
        transaction.wallet = wallet
        transaction.txid = gsonAddress.txid
        transaction.memPoolState = MemPoolState.PENDING
        transaction.blockhash = ""
        daoSessionManager.insert(transaction)
    }

    fun initTransactions(wallet: Wallet, addresses: List<GsonAddress>) {
        val txids: MutableList<String> = mutableListOf()

        for (address in addresses) {
            val txid = address.txid
            if (txids.contains(txid) || transactionQueryManager.transactionByTxid(txid) != null) continue
            txids.add(txid)
            initializeTransaction(wallet, daoSessionManager.newTransactionSummary(), address)
        }

        txids.clear()
    }

    fun markTransactionSummaryAsAcknowledged(txid: String) {
        transactionQueryManager.transactionByTxid(txid)?.let {
            it.memPoolState = MemPoolState.ACKNOWLEDGE
            it.update()
            it.refresh()
        }
    }

    fun markTransactionSummaryAsFailedToBroadcast(txid: String): String? {
        transactionQueryManager.transactionByTxid(txid)?.let {

            markTargetStatsAsCanceled(it.receiver)
            markFundingStatsAsCanceled(it.funder)
            it.memPoolState = MemPoolState.FAILED_TO_BROADCAST
            it.update()
        }
        return txid
    }

    internal fun markTargetStatsAsCanceled(receivers: List<TargetStat>) {
        for (targetStat in receivers) {
            targetStat.state = TargetStat.State.CANCELED
            targetStat.update()
            targetStat.refresh()
            removeFundingStatTargetStatRelationship(targetStat)
        }
    }

    internal fun markFundingStatsAsCanceled(funders: List<FundingStat>) {
        for (fundingStat in funders) {
            fundingStat.state = FundingStat.State.CANCELED
            fundingStat.update()
            fundingStat.refresh()
            removeFundingStatTargetStatRelationship(fundingStat)
        }
    }

    internal fun removeFundingStatTargetStatRelationship(fundingStat: FundingStat) {
        transactionQueryManager.targetStatFromFundingId(fundingStat.id)?.let {
            it.fundingStat = null
            it.update()
        }
    }

    internal fun removeFundingStatTargetStatRelationship(targetStat: TargetStat) {
        transactionQueryManager.fundingStatFromTargetId(targetStat.id)?.let {
            it.targetStat = null
            it.update()
        }
    }

    fun updateTransactions(fetchedTransactions: List<TransactionDetail>, currentBlockHeight: Int) {
        for (detail in fetchedTransactions) {
            transactionQueryManager.transactionByTxid(detail.txid)?.let {
                try {
                    updateTransaction(it, detail, currentBlockHeight)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }


    internal fun updateTransaction(transaction: TransactionSummary, detail: TransactionDetail, currentBlockHeight: Int) {
        transaction.blockheight = detail.blockheight
        transaction.numConfirmations = detail.numConfirmations(currentBlockHeight)
        transaction.txTime = detail.timeMillis
        transaction.blockhash = detail.blockhash
        transaction.memPoolState = detail.mempoolState
        transaction.numInputs = detail.numberOfInputs
        transaction.numOutputs = detail.numberOfOutputs

        try {
            for (input in detail.vInList) {
                saveIn(detail, transaction, input)
            }
            for (out in detail.vOutList) {
                saveOut(detail, transaction, out)
            }
        } catch (e: IllegalArgumentException) {
            return
        }
        transaction.update()
        transactionInviteSummaryHelper.getOrCreateParentSettlementFor(transaction)
    }

    @Throws(IllegalArgumentException::class)
    internal fun saveOut(detail: TransactionDetail, transaction: TransactionSummary, output: VOut) {
        val addresses = output.scriptPubKey.addresses
        if (addresses.isEmpty() && !detail.isCoinbase) {
            throw IllegalArgumentException()
        }

        val target = targetStatHelper.getOrCreateTargetStat(transaction, output, if (detail.isCoinbase && addresses.isNullOrEmpty()) "Coinbase" else null)

        if (!addresses.isNullOrEmpty()) {
            addressHelper.addressForPubKey(addresses[0])?.let {
                target.address = it
                target.wallet = transaction.wallet
            }
        }

        val targetState: TargetStat.State =
                when (transaction.memPoolState) {
                    MemPoolState.ACKNOWLEDGE,
                    MemPoolState.MINED -> TargetStat.State.ACKNOWLEDGE
                    MemPoolState.FAILED_TO_BROADCAST,
                    MemPoolState.DOUBLE_SPEND,
                    MemPoolState.ORPHANED -> TargetStat.State.CANCELED
                    else -> TargetStat.State.PENDING
                }

        target.state = targetState
        target.update()
    }

    @Throws(IllegalArgumentException::class)
    internal fun saveIn(detail: TransactionDetail, transaction: TransactionSummary, input: VIn) {
        val addresses = input.previousOutput.scriptPubKey.addresses ?: emptyArray()

        if (addresses.isEmpty() && !detail.isCoinbase) {
            throw IllegalArgumentException()
        }

        val funder: FundingStat = fundingStatHelper.getOrCreateFundingStat(transaction, input, if (detail.isCoinbase && addresses.isNullOrEmpty()) "Coinbase" else null)
        if (!addresses.isNullOrEmpty()) {
            addressHelper.addressForPubKey(addresses[0])?.let {
                funder.address = it
                funder.wallet = transaction.wallet
            }
        }

        val fundingState: FundingStat.State =
                when (transaction.memPoolState) {
                    MemPoolState.ACKNOWLEDGE,
                    MemPoolState.MINED -> FundingStat.State.ACKNOWLEDGE
                    MemPoolState.FAILED_TO_BROADCAST,
                    MemPoolState.DOUBLE_SPEND,
                    MemPoolState.ORPHANED -> FundingStat.State.CANCELED
                    else -> FundingStat.State.PENDING
                }

        funder.state = fundingState
        funder.update()
    }

    internal fun addUserIdentitiesToTransaction(identity: Identity?, transactionInviteSummary: TransactionsInvitesSummary) {
        identity?.let {
            dropbitAccountHelper.identityForType(identity.identityType)?.let {
                transactionInviteSummary.fromUser = userIdentityHelper.updateFrom(it)
            }

            transactionInviteSummary.toUser = userIdentityHelper.updateFrom(identity)
            transactionInviteSummary.update()
        }
    }

    fun createInitialTransactionForCompletedBroadcast(completedBroadcastActivityDTO: CompletedBroadcastDTO): TransactionSummary {
        val transactionId = completedBroadcastActivityDTO.transactionId
        val identity = completedBroadcastActivityDTO.identity
        val transaction = daoSessionManager.newTransactionSummary()
        transaction.txid = transactionId
        transaction.wallet = walletHelper.primaryWallet
        transaction.memPoolState = MemPoolState.PENDING
        transaction.txTime = dateUtil.getCurrentTimeInMillis()
        transaction.blockhash = ""
        daoSessionManager.insert(transaction)
        try {
            fundingStatHelper.createInputsFor(transaction, completedBroadcastActivityDTO.transactionData)
            targetStatHelper.createOutputsFor(transaction, completedBroadcastActivityDTO.transactionData)
        } catch (e:java.lang.Exception) { }

        transactionInviteSummaryHelper.getOrCreateParentSettlementFor(transaction).also {
            addUserIdentitiesToTransaction(identity, it)
        }
        return transaction
    }
}