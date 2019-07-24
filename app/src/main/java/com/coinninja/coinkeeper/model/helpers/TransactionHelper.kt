package com.coinninja.coinkeeper.model.helpers

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.model.db.*
import com.coinninja.coinkeeper.model.db.TransactionSummaryDao.Properties
import com.coinninja.coinkeeper.model.db.enums.MemPoolState
import com.coinninja.coinkeeper.model.dto.CompletedBroadcastDTO
import com.coinninja.coinkeeper.model.query.TransactionQueryManager
import com.coinninja.coinkeeper.service.client.model.*
import com.coinninja.coinkeeper.util.DateUtil
import javax.inject.Inject

@Mockable
class TransactionHelper @Inject constructor(
        internal val daoSessionManager: DaoSessionManager,
        internal val walletHelper: WalletHelper,
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

    fun transactionWithTxid(txid: String): TransactionSummary?  = transactionQueryManager.transactionByTxid(txid)


    private val transactionDao: TransactionSummaryDao
        get() = daoSessionManager.transactionSummaryDao

    private val transactionsInvitesSummaryDao: TransactionsInvitesSummaryDao
        get() = daoSessionManager.transactionsInvitesSummaryDao

    fun getPendingTransactionsOlderThan(olderThanMillis: Long): List<TransactionSummary> {
        return transactionQueryManager.pendingTransactionsOlderThan(olderThanMillis)
    }

    fun initializeTransaction(transaction: TransactionSummary, gsonAddress: GsonAddress) {
        transaction.wallet = walletHelper.wallet
        transaction.txid = gsonAddress.txid
        transaction.memPoolState = MemPoolState.PENDING
        daoSessionManager.insert(transaction)
    }

    fun initTransactions(addresses: List<GsonAddress>) {
        val txids: MutableList<String> = mutableListOf()

        for (address in addresses) {
            val txid = address.txid
            if (txids.contains(txid) || transactionQueryManager.transactionByTxid(txid) != null) continue
            txids.add(txid)
            initializeTransaction(daoSessionManager.newTransactionSummary(), address)
        }

        txids.clear()
    }

    // TODO --- YOU ARE HERE

    fun updateTransactions(fetchedTransactions: List<TransactionDetail>, currentBlockHeight: Int) {
        for (detail in fetchedTransactions) {
            val transaction = transactionDao.queryBuilder().where(Properties.Txid.eq(detail.transactionId)).limit(1).unique()
                    ?: continue

            transaction.memPoolState = MemPoolState.ACKNOWLEDGE
            if (detail.blockheight > 0) {
                transaction.numConfirmations = CNWalletManager.calcConfirmations(currentBlockHeight, detail.blockheight)
            }
            transaction.update()
            transaction.refresh()


            try {
                saveTransaction(transaction, detail)
            } catch (ex: Exception) {
                continue
            }

        }
    }

    fun joinInviteToTx(invite: InviteTransactionSummary, transaction: TransactionSummary) {

        val joinTableDao = daoSessionManager.transactionsInvitesSummaryDao
        val joinContainingRealTX = joinTableDao.queryBuilder()
                .where(TransactionsInvitesSummaryDao.Properties.TransactionSummaryID.eq(transaction.id)).limit(1).unique()
        val joinContainingInvite = joinTableDao.queryBuilder()
                .where(TransactionsInvitesSummaryDao.Properties.InviteSummaryID.eq(invite.id)).limit(1).unique()

        if (joinContainingRealTX === joinContainingInvite) return

        dropEntry(joinContainingInvite)
        dropEntry(joinContainingRealTX)


        val newJoinTable = daoSessionManager.newTransactionInviteSummary()
        daoSessionManager.insert(newJoinTable)

        newJoinTable.inviteTransactionSummary = invite
        newJoinTable.inviteTxID = invite.btcTransactionId

        newJoinTable.transactionTxID = transaction.txid
        newJoinTable.transactionSummary = transaction

        if (transaction.txTime > 0) {
            newJoinTable.btcTxTime = transaction.txTime
            newJoinTable.inviteTime = 0
        } else {
            newJoinTable.inviteTime = invite.sentDate
        }

        newJoinTable.update()
        newJoinTable.refresh()

        invite.transactionsInvitesSummaryID = newJoinTable.id
        invite.update()
        invite.refresh()

        transaction.transactionsInvitesSummaryID = newJoinTable.id
        transaction.update()
        transaction.refresh()
    }

    fun markTransactionSummaryAsFailedToBroadcast(txid: String): String? {
        val transaction = transactionDao.queryBuilder().where(Properties.Txid.eq(txid)).limit(1).unique()
                ?: return null

        markTargetStatsAsCanceled(transaction.receiver)
        markFundingStatsAsCanceled(transaction.funder)

        transaction.memPoolState = MemPoolState.FAILED_TO_BROADCAST
        transaction.update()
        transaction.refresh()

        return renameTXIDToFailed(txid)
    }

    fun markTransactionSummaryAsAcknowledged(txid: String) {
        val transaction = transactionDao.queryBuilder().where(Properties.Txid.eq(txid)).limit(1).unique()
                ?: return

        transaction.memPoolState = MemPoolState.ACKNOWLEDGE
        transaction.update()
        transaction.refresh()

    }

    fun createInitialTransactionForCompletedBroadcast(completedBroadcastActivityDTO: CompletedBroadcastDTO): TransactionSummary {
        return createInitialTransaction(completedBroadcastActivityDTO.transactionId, completedBroadcastActivityDTO.identity)
    }

    internal fun saveOut(transaction: TransactionSummary, out: VOut) {
        val dao = daoSessionManager.targetStatDao
        val addresses = out.scriptPubKey.addresses

        if (null != addresses && addresses.size == 0)
            throw IllegalArgumentException()

        var target: TargetStat? = dao.queryBuilder().where(TargetStatDao.Properties.Tsid.eq(transaction.id),
                TargetStatDao.Properties.Value.eq(out.value),
                TargetStatDao.Properties.Position.eq(out.index),
                TargetStatDao.Properties.Addr.eq(addresses[0])).unique()

        val dbAddress = daoSessionManager.addressDao.queryBuilder().where(AddressDao.Properties.Address.eq(addresses[0])).unique()

        if (null != target) {
            target.refresh()
        } else {
            target = TargetStat()
            target.__setDaoSession(daoSessionManager.daoSession)
        }

        if (null != dbAddress) {
            target.address = dbAddress
            target.wallet = transaction.wallet
        }

        val transactionState = transaction.memPoolState
        val targetState: TargetStat.State
        when (transactionState) {
            MemPoolState.ACKNOWLEDGE, MemPoolState.MINED -> targetState = TargetStat.State.ACKNOWLEDGE
            MemPoolState.FAILED_TO_BROADCAST, MemPoolState.DOUBLE_SPEND, MemPoolState.ORPHANED -> targetState = TargetStat.State.CANCELED
            else -> targetState = TargetStat.State.PENDING
        }

        target.addr = addresses[0]
        target.position = out.index
        target.transaction = transaction
        target.value = out.value
        target.state = targetState
        target.txTime = transaction.txTime
        dao.save(target)
    }

    internal fun saveIn(transaction: TransactionSummary, `in`: VIn) {
        val dao = daoSessionManager.fundingStatDao

        val addresses = `in`.previousOutput.scriptPubKey.addresses
        if (null != addresses && addresses.size == 0)
            throw IllegalArgumentException()

        var funder: FundingStat? = dao.queryBuilder().where(
                FundingStatDao.Properties.Tsid.eq(transaction.id),
                FundingStatDao.Properties.FundedTransaction.eq(`in`.transactionId),
                FundingStatDao.Properties.Value.eq(`in`.previousOutput.value),
                FundingStatDao.Properties.Position.eq(`in`.previousOutput.index),
                FundingStatDao.Properties.Addr.eq(`in`.previousOutput.scriptPubKey.addresses[0]))
                .unique()

        val dbAddress = daoSessionManager.addressDao.queryBuilder().where(AddressDao.Properties.Address.eq(addresses[0])).unique()

        if (funder == null) {
            funder = FundingStat()
            funder.__setDaoSession(daoSessionManager.daoSession)
        } else {
            funder.refresh()
        }

        if (dbAddress != null) {
            funder.address = dbAddress
            funder.wallet = transaction.wallet
        }

        val transactionState = transaction.memPoolState
        val fundingState: FundingStat.State
        when (transactionState) {
            MemPoolState.ACKNOWLEDGE, MemPoolState.MINED -> fundingState = FundingStat.State.ACKNOWLEDGE
            MemPoolState.FAILED_TO_BROADCAST, MemPoolState.DOUBLE_SPEND, MemPoolState.ORPHANED -> fundingState = FundingStat.State.CANCELED
            else -> fundingState = FundingStat.State.PENDING
        }

        funder.addr = addresses[0]
        funder.position = `in`.previousOutput.index
        funder.transaction = transaction
        funder.fundedTransaction = `in`.transactionId
        funder.state = fundingState
        funder.value = `in`.previousOutput.value
        dao.save(funder)
        dao.refresh(funder)
    }

    internal fun saveTransaction(transaction: TransactionSummary, detail: TransactionDetail) {
        if (detail.blocktimeMillis > 0L) {
            transaction.txTime = detail.blocktimeMillis
        } else if (detail.timeMillis > 0L) {
            transaction.txTime = detail.timeMillis
        } else {
            transaction.txTime = detail.receivedTimeMillis
        }

        transaction.blockhash = detail.blockhash
        transaction.blockheight = detail.blockheight
        if (detail.isInBlock) {
            transaction.memPoolState = MemPoolState.MINED
        }
        transaction.update()
        transaction.refresh()

        try {
            for (`in` in detail.vInList) {
                saveIn(transaction, `in`)
            }

            for (out in detail.vOutList) {
                saveOut(transaction, out)
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            return
        }


        daoSessionManager.clearCacheFor(transaction)
        transaction.numInputs = transaction.funder.size
        transaction.numOutputs = transaction.receiver.size
        transaction.update()
        transaction.refresh()

        transactionInviteSummaryHelper.getOrCreateParentSettlementFor(transaction)
    }

    private fun dropEntry(transInvite: TransactionsInvitesSummary?) {
        val transInvitesDao = daoSessionManager.transactionsInvitesSummaryDao
        if (transInvite == null) {
            return
        }
        transInvitesDao.delete(transInvite)
    }


    private fun markTargetStatsAsCanceled(receivers: List<TargetStat>) {
        for (targetStat in receivers) {
            targetStat.state = TargetStat.State.CANCELED

            targetStat.update()
            targetStat.refresh()

            removeFundingStatTargetStatRelationship(targetStat)
        }
    }

    private fun markFundingStatsAsCanceled(funders: List<FundingStat>) {
        for (fundingStat in funders) {
            fundingStat.state = FundingStat.State.CANCELED

            fundingStat.update()
            fundingStat.refresh()

            removeFundingStatTargetStatRelationship(fundingStat)
        }

    }

    private fun removeFundingStatTargetStatRelationship(fundingStat: FundingStat) {
        val dao = daoSessionManager.targetStatDao

        val targetStat = dao.queryBuilder().where(TargetStatDao.Properties.FundingId.eq(fundingStat.id)).limit(1).unique()
                ?: return

        targetStat.fundingStat = null
        targetStat.update()
        targetStat.refresh()
    }

    private fun removeFundingStatTargetStatRelationship(targetStat: TargetStat) {
        val dao = daoSessionManager.fundingStatDao

        val fundingStat = dao.queryBuilder().where(FundingStatDao.Properties.TargetId.eq(targetStat.id)).limit(1).unique()
                ?: return

        fundingStat.targetStat = null
        fundingStat.update()
        fundingStat.refresh()
    }

    private fun renameTXIDToFailed(txid: String): String {
        val currentTime = dateUtil.getCurrentTimeInMillis()
        val newTxId = String.format("failedToBroadcast_%s_%s", currentTime, txid)
        renameTransSummary(txid, newTxId)
        renameInviteSummary(txid, newTxId)
        renameTransInviteSummary(txid, newTxId)
        return newTxId
    }

    private fun renameTransSummary(originalTxId: String, newTxId: String) {
        val transaction = transactionDao.queryBuilder().where(Properties.Txid.eq(originalTxId)).limit(1).unique()
                ?: return

        transaction.txid = newTxId
        transaction.update()
        transaction.refresh()
    }

    //TODO Move To InviteSummaryHelper
    private fun renameInviteSummary(originalTxId: String, newTxId: String) {
        val invite = daoSessionManager.inviteTransactionSummaryDao.queryBuilder().where(InviteTransactionSummaryDao.Properties.BtcTransactionId.eq(originalTxId)).limit(1).unique()
                ?: return

        invite.btcTransactionId = newTxId
        invite.update()
        invite.refresh()
    }

    private fun renameTransInviteSummary(originalTxId: String, newTxId: String) {
        val transactionsInvitesSummary = transactionsInvitesSummaryDao.queryBuilder().where(TransactionsInvitesSummaryDao.Properties.InviteTxID.eq(originalTxId)).limit(1).unique()
                ?: return

        transactionsInvitesSummary.inviteTxID = newTxId
        transactionsInvitesSummary.update()
        transactionsInvitesSummary.refresh()
    }

    private fun createInitialTransaction(transactionId: String?, identity: Identity?): TransactionSummary {
        val transactionSummary = daoSessionManager.newTransactionSummary()
        transactionSummary.txid = transactionId
        transactionSummary.wallet = walletHelper.wallet
        transactionSummary.memPoolState = MemPoolState.PENDING
        transactionSummary.numConfirmations = 0
        transactionSummary.txTime = dateUtil.getCurrentTimeInMillis()

        daoSessionManager.insert(transactionSummary)

        val transactionInviteSummary = transactionInviteSummaryHelper.getOrCreateTransactionInviteSummaryFor(transactionSummary)
        addUserIdentitiesToTransaction(identity, transactionInviteSummary)
        transactionInviteSummary.update()
        return transactionSummary
    }

    private fun addUserIdentitiesToTransaction(identity: Identity?, transactionInviteSummary: TransactionsInvitesSummary) {
        if (identity == null) return

        val myIdentity = dropbitAccountHelper.identityForType(identity.identityType)
        if (myIdentity != null) {
            val fromUser = userIdentityHelper.updateFrom(myIdentity)
            transactionInviteSummary.fromUser = fromUser
        }

        val toUser = userIdentityHelper.updateFrom(identity)
        transactionInviteSummary.toUser = toUser
    }

    companion object {

        private val TAG = TransactionHelper::class.java.simpleName
    }

}