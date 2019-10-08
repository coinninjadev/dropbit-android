package com.coinninja.coinkeeper.model.query

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary
import com.coinninja.coinkeeper.model.db.InviteTransactionSummaryDao
import com.coinninja.coinkeeper.model.db.enums.BTCState
import com.coinninja.coinkeeper.model.db.enums.Type
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager

import javax.inject.Inject

@Mockable
class InviteSummaryQueryManager @Inject internal constructor(
        internal val daoSessionManager: DaoSessionManager
) {

    val allUnacknowledgedInvitations: List<InviteTransactionSummary>
        get() = daoSessionManager.inviteTransactionSummaryDao.queryBuilder()
                .where(InviteTransactionSummaryDao.Properties.BtcState.eq(BTCState.UNACKNOWLEDGED.id)).list()

    val completedLightningInvites: List<InviteTransactionSummary>
        get() = daoSessionManager.inviteTransactionSummaryDao.queryBuilder()
                .whereOr(
                        InviteTransactionSummaryDao.Properties.Type.eq(Type.LIGHTNING_SENT.id),
                        InviteTransactionSummaryDao.Properties.Type.eq(Type.LIGHTNING_RECEIVED.id)
                )
                .where(
                        InviteTransactionSummaryDao.Properties.BtcState.eq(BTCState.FULFILLED.id)
                )
                .list()

    val unfulfilledSentLightningInvites: List<InviteTransactionSummary>
        get() = daoSessionManager.inviteTransactionSummaryDao.queryBuilder()
                .where(InviteTransactionSummaryDao.Properties.BtcState.eq(BTCState.UNFULFILLED.id),
                        InviteTransactionSummaryDao.Properties.Type.eq(Type.LIGHTNING_SENT.id))
                .list()

    val unfulfilledSentInvites: List<InviteTransactionSummary>
        get() = daoSessionManager.inviteTransactionSummaryDao.queryBuilder()
                .where(InviteTransactionSummaryDao.Properties.BtcState.eq(BTCState.UNFULFILLED.id),
                        InviteTransactionSummaryDao.Properties.Type.eq(Type.BLOCKCHAIN_SENT.id))
                .list()


    val invitesWithTxid: List<InviteTransactionSummary>
        get() = daoSessionManager.inviteTransactionSummaryDao.queryBuilder().where(
                InviteTransactionSummaryDao.Properties.BtcTransactionId.isNotNull(),
                InviteTransactionSummaryDao.Properties.BtcTransactionId.notEq("")
        ).list()

    fun getInviteSummaryByTxid(txid: String): InviteTransactionSummary? =
            daoSessionManager.inviteTransactionSummaryDao.queryBuilder()
                    .where(
                            InviteTransactionSummaryDao.Properties.BtcTransactionId.eq(txid)
                    ).unique()

    fun getInviteSummaryByCnId(cnId: String): InviteTransactionSummary? =
            daoSessionManager.inviteTransactionSummaryDao.queryBuilder()
                    .where(InviteTransactionSummaryDao.Properties.ServerId.eq(cnId)).unique()

    fun getOrCreate(cnId: String): InviteTransactionSummary =
            getInviteSummaryByCnId(cnId) ?: daoSessionManager.newInviteTransactionSummary().also {
                it.serverId = cnId;
                daoSessionManager.insert(it)
            }
}
