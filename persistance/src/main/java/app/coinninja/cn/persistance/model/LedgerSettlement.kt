package app.coinninja.cn.persistance.model

import androidx.room.*
import app.dropbit.annotations.Mockable
import java.util.*


@Mockable
@Entity(
        tableName = "LEDGER_SETTLEMENT",
        indices = [
            Index(
                    value = ["TO_USER_IDENTITY_ID"],
                    name = "LEDGER_SETTLEMENT__TO_USER_IDENTITY_ID",
                    unique = false),
            Index(
                    value = ["FROM_USER_IDENTITY_ID"],
                    name = "LEDGER_SETTLEMENT__FROM_USER_IDENTITY_ID",
                    unique = false),
            Index(
                    value = ["INVITE_ID"],
                    name = "LEDGER_SETTLEMENT__INVITE_ID",
                    unique = false),
            Index(
                    value = ["INVOICE_ID"],
                    name = "LEDGER_SETTLEMENT__INVOICE_ID",
                    unique = false),
            Index(
                    value = ["NOTIFICATION_ID"],
                    name = "LEDGER_SETTLEMENT__NOTIFICATION_ID",
                    unique = false)
        ],
        foreignKeys = [
            ForeignKey(
                    entity = UserIdentity::class,
                    parentColumns = arrayOf("_id"),
                    childColumns = arrayOf("TO_USER_IDENTITY_ID"),
                    onDelete = ForeignKey.NO_ACTION
            ),
            ForeignKey(
                    entity = UserIdentity::class,
                    parentColumns = arrayOf("_id"),
                    childColumns = arrayOf("FROM_USER_IDENTITY_ID"),
                    onDelete = ForeignKey.NO_ACTION
            ),
            ForeignKey(
                    entity = InviteTransactionSummary::class,
                    parentColumns = arrayOf("_id"),
                    childColumns = arrayOf("INVITE_ID"),
                    onDelete = ForeignKey.NO_ACTION
            ),
            ForeignKey(
                    entity = LightningInvoice::class,
                    parentColumns = arrayOf("_id"),
                    childColumns = arrayOf("INVOICE_ID"),
                    onDelete = ForeignKey.NO_ACTION
            ),
            ForeignKey(
                    entity = TransactionNotification::class,
                    parentColumns = arrayOf("_id"),
                    childColumns = arrayOf("NOTIFICATION_ID"),
                    onDelete = ForeignKey.CASCADE
            )
        ]
)
class LedgerSettlement(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        var id: Long = 0,

        @ColumnInfo(name = "INVOICE_ID")
        var invoiceId: Long? = null,

        @ColumnInfo(name = "TO_USER_IDENTITY_ID")
        var toUserIdentityId: Long? = null,

        @ColumnInfo(name = "FROM_USER_IDENTITY_ID")
        var fromUserIdentityId: Long? = null,

        @ColumnInfo(name = "INVITE_ID")
        var inviteId: Long? = null,

        @ColumnInfo(name = "NOTIFICATION_ID")
        var transactionNotificationId: Long? = null,

        @ColumnInfo(name = "CREATED_AT")
        var createdAt: Date? = null
) {

}