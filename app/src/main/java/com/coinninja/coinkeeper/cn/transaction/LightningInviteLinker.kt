package com.coinninja.coinkeeper.cn.transaction

import app.coinninja.cn.persistance.DropbitDatabase
import app.coinninja.cn.persistance.model.LedgerSettlement
import app.coinninja.cn.persistance.model.LightningInvoice
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary
import com.coinninja.coinkeeper.model.query.InviteSummaryQueryManager
import java.util.*
import javax.inject.Inject


@Mockable
class LightningInviteLinker @Inject constructor(
        val dropbitDatabase: DropbitDatabase,
        val inviteSummaryQueryManager: InviteSummaryQueryManager
) {

    fun linkInvitesToInvoices() {
        inviteSummaryQueryManager.completedLightningInvites.forEach { invite ->
            getOrCreateInviteSettlement(invite)?.let { inviteSettlement ->
                invite.btcTransactionId?.let { serverId ->
                    dropbitDatabase.lightningInvoiceDao().ledgerByServerId(invite.btcTransactionId)?.let { invoice ->
                        val invoiceSettlement = dropbitDatabase.ledgerSettlementDao.settlementByInvoiceId(invoiceId = invoice.id)
                        if (invoiceSettlement == null) {
                            inviteSettlement.invoiceId = invoice.id
                            dropbitDatabase.ledgerSettlementDao.update(inviteSettlement)
                        } else {
                            linkSettlements(inviteSettlement, invoiceSettlement, invoice)
                        }
                    }
                }
            }
        }

    }

    private fun getOrCreateInviteSettlement(invite: InviteTransactionSummary): LedgerSettlement? {
        return dropbitDatabase.ledgerSettlementDao.settlementByInviteId(invite.id)
                ?: createSettlementForInvite(invite)
    }

    private fun createSettlementForInvite(invite: InviteTransactionSummary): LedgerSettlement? {
        dropbitDatabase.ledgerSettlementDao.createSettlementForInvite(
                inviteId = invite.id,
                toUserId = invite.toUser.id,
                fromUserId = invite.fromUser.id,
                date = Date(invite.sentDate)
        )
        return dropbitDatabase.ledgerSettlementDao.settlementByInviteId(invite.id)!!
    }

    private fun linkSettlements(inviteSettlement: LedgerSettlement, invoiceSettlement: LedgerSettlement, invoice: LightningInvoice) {
        if (inviteSettlement.id != invoiceSettlement.id) {
            inviteSettlement.invoiceId = invoice.id
            dropbitDatabase.ledgerSettlementDao.update(inviteSettlement)
            dropbitDatabase.ledgerSettlementDao.delete(invoiceSettlement)
        }
    }

}