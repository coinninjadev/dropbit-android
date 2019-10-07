package app.coinninja.cn.persistance.dao

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.room.*
import app.coinninja.cn.persistance.model.LedgerSettlement
import app.coinninja.cn.persistance.model.LedgerSettlementDetail
import app.coinninja.cn.persistance.model.LightningInvoice
import app.dropbit.annotations.Mockable
import java.util.*

@Dao
@Mockable
abstract class LedgerSettlementDao {
    @Query("""
select INVITE.VALUE_SATOSHIS  `inviteValue`,
       INVITE.HISTORIC_VALUE  `inviteUsdValue`,
       INVITE.TYPE            `inviteType`,
       INVITE.BTC_STATE       `inviteState`,
       TO_USER.IDENTITY       `toUserIdentity`,
       TO_USER.DISPLAY_NAME   `toUserDisplayName`,
       TO_USER.TYPE           `toUserType`,
       TO_USER.HANDLE         `toUserHandle`,
       TO_USER.AVATAR         `toUserAvatar`,
       FROM_USER.IDENTITY     `fromUserIdentity`,
       FROM_USER.DISPLAY_NAME `fromUserDisplayName`,
       FROM_USER.TYPE         `fromUserType`,
       FROM_USER.HANDLE       `fromUserHandle`,
       FROM_USER.AVATAR       `fromUserAvatar`,
       LI.Value               `invoiceValue`,
       LI.NETWORK_FEE         `invoiceNetworkFee`,
       LI.PROCESSING_FEE      `invoiceProcessingFee`,
       LI.DIRECTION           `invoiceDirection`,
       LI.TYPE                `invoiceType`,
       LI.STATUS              `invoiceStatus`,
       LI.MEMO                `invoiceMemo`,
       LI.CREATED_AT          `invoiceCreatedAt`,
       LS.CREATED_AT          `createdAt`


from LEDGER_SETTLEMENT LS
       left outer join LIGHTNING_INVOICE LI on LI._id = LS.INVOICE_ID
       left outer join INVITE_TRANSACTION_SUMMARY INVITE on LS.INVITE_ID = INVITE._id
       left outer join USER_IDENTITY TO_USER on LS.TO_USER_IDENTITY_ID = TO_USER._id
       left outer join USER_IDENTITY FROM_USER on LS.FROM_USER_IDENTITY_ID = FROM_USER._id
order by createdAt DESC;
    """)
    abstract fun all(): List<LedgerSettlementDetail>

    @Query("""
select INVITE.VALUE_SATOSHIS  `inviteValue`,
       INVITE.HISTORIC_VALUE  `inviteUsdValue`,
       INVITE.TYPE            `inviteType`,
       INVITE.BTC_STATE       `inviteState`,
       TO_USER.IDENTITY       `toUserIdentity`,
       TO_USER.DISPLAY_NAME   `toUserDisplayName`,
       TO_USER.TYPE           `toUserType`,
       TO_USER.HANDLE         `toUserHandle`,
       TO_USER.AVATAR         `toUserAvatar`,
       FROM_USER.IDENTITY     `fromUserIdentity`,
       FROM_USER.DISPLAY_NAME `fromUserDisplayName`,
       FROM_USER.TYPE         `fromUserType`,
       FROM_USER.HANDLE       `fromUserHandle`,
       FROM_USER.AVATAR       `fromUserAvatar`,
       LI.Value               `invoiceValue`,
       LI.NETWORK_FEE         `invoiceNetworkFee`,
       LI.PROCESSING_FEE      `invoiceProcessingFee`,
       LI.DIRECTION           `invoiceDirection`,
       LI.TYPE                `invoiceType`,
       LI.STATUS              `invoiceStatus`,
       LI.MEMO                `invoiceMemo`,
       LI.CREATED_AT          `invoiceCreatedAt`,
       LS.CREATED_AT          `createdAt`


from LEDGER_SETTLEMENT LS
       left outer join LIGHTNING_INVOICE LI on LI._id = LS.INVOICE_ID
       left outer join INVITE_TRANSACTION_SUMMARY INVITE on LS.INVITE_ID = INVITE._id
       left outer join USER_IDENTITY TO_USER on LS.TO_USER_IDENTITY_ID = TO_USER._id
       left outer join USER_IDENTITY FROM_USER on LS.FROM_USER_IDENTITY_ID = FROM_USER._id
where LI.IS_HIDDEN = 0
   or LI.IS_HIDDEN is null
order by createdAt DESC;    """)
    abstract fun allVisible(): List<LedgerSettlementDetail>

    @Query("""
select INVITE.VALUE_SATOSHIS  `inviteValue`,
       INVITE.HISTORIC_VALUE  `inviteUsdValue`,
       INVITE.TYPE            `inviteType`,
       INVITE.BTC_STATE       `inviteState`,
       TO_USER.IDENTITY       `toUserIdentity`,
       TO_USER.DISPLAY_NAME   `toUserDisplayName`,
       TO_USER.TYPE           `toUserType`,
       TO_USER.HANDLE         `toUserHandle`,
       TO_USER.AVATAR         `toUserAvatar`,
       FROM_USER.IDENTITY     `fromUserIdentity`,
       FROM_USER.DISPLAY_NAME `fromUserDisplayName`,
       FROM_USER.TYPE         `fromUserType`,
       FROM_USER.HANDLE       `fromUserHandle`,
       FROM_USER.AVATAR       `fromUserAvatar`,
       LI.Value               `invoiceValue`,
       LI.NETWORK_FEE         `invoiceNetworkFee`,
       LI.PROCESSING_FEE      `invoiceProcessingFee`,
       LI.DIRECTION           `invoiceDirection`,
       LI.TYPE                `invoiceType`,
       LI.STATUS              `invoiceStatus`,
       LI.MEMO                `invoiceMemo`,
       LI.CREATED_AT          `invoiceCreatedAt`,
       LS.CREATED_AT          `createdAt`


from LEDGER_SETTLEMENT LS
       left outer join LIGHTNING_INVOICE LI on LI._id = LS.INVOICE_ID
       left outer join INVITE_TRANSACTION_SUMMARY INVITE on LS.INVITE_ID = INVITE._id
       left outer join USER_IDENTITY TO_USER on LS.TO_USER_IDENTITY_ID = TO_USER._id
       left outer join USER_IDENTITY FROM_USER on LS.FROM_USER_IDENTITY_ID = FROM_USER._id
where LI.IS_HIDDEN = 0
   or LI.IS_HIDDEN is null
order by createdAt DESC;
    """)
    abstract fun allVisibleLive(): LiveData<List<LedgerSettlementDetail>>

    @Query("select * from LEDGER_SETTLEMENT where INVOICE_ID = :invoiceId limit 1")
    abstract fun settlementByInvoiceId(invoiceId: Long): LedgerSettlement?

    @Query("select * from LEDGER_SETTLEMENT where INVITE_ID = :inviteId limit 1")
    abstract fun settlementByInviteId(inviteId: Long): LedgerSettlement?

    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(ledgerSettlement: LedgerSettlement)

    @WorkerThread
    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun update(ledgerSettlement: LedgerSettlement)

    @WorkerThread
    @Query("DELETE FROM ledger_settlement")
    abstract fun deleteAll()

    fun createSettlementFor(invoice: LightningInvoice?) {
        invoice?.let {
            if (settlementByInvoiceId(it.id) == null) {
                insert(LedgerSettlement(invoiceId = it.id, createdAt = it.createdAt))
            }
        }
    }

    fun createSettlementForInvite(inviteId: Long, toUserId: Long, fromUserId: Long, date: Date) {
        if (settlementByInviteId(inviteId) == null) {
            insert(LedgerSettlement(
                    inviteId = inviteId,
                    toUserIdentityId = toUserId,
                    fromUserIdentityId = fromUserId,
                    createdAt = date
            ))
        }
    }

    fun addInvoiceToInvite(inviteId: Long, invoiceId: Long) {
        settlementByInviteId(inviteId)?.let {
            it.invoiceId = invoiceId
            update(it)
        }
    }
}