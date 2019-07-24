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

    val unfulfilledSentInvites: List<InviteTransactionSummary>
        get() {
            val inviteTransactionSummaryQueryBuilder = daoSessionManager.inviteTransactionSummaryDao.queryBuilder()
            return inviteTransactionSummaryQueryBuilder
                    .where(InviteTransactionSummaryDao.Properties.BtcState.eq(BTCState.UNFULFILLED.id),
                            InviteTransactionSummaryDao.Properties.Type.eq(Type.SENT.id))
                    .list()

        }

    val invitesWithTxid: List<InviteTransactionSummary>
        get() = daoSessionManager.inviteTransactionSummaryDao.queryBuilder().where(
                InviteTransactionSummaryDao.Properties.BtcTransactionId.isNotNull(),
                InviteTransactionSummaryDao.Properties.BtcTransactionId.notEq("")
        ).list()

    fun getInviteSummaryByCnId(cnId: String): InviteTransactionSummary? =
            daoSessionManager.inviteTransactionSummaryDao.queryBuilder()
                    .where(InviteTransactionSummaryDao.Properties.ServerId.eq(cnId)).unique()

    fun getOrCreate(cnId: String): InviteTransactionSummary =
            getInviteSummaryByCnId(cnId) ?: daoSessionManager.newInviteTransactionSummary().also {
                it.serverId = cnId;
                daoSessionManager.insert(it)
            }
}
