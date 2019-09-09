package app.coinninja.cn.persistance.model

import androidx.room.*
import app.dropbit.annotations.Mockable

@Mockable
@Entity(
        tableName = "TRANSACTIONS_INVITES_SUMMARY",
        indices = [
            Index(
                    value = ["TRANSACTION_SUMMARY_ID"],
                    name = "TRANSACTIONS_INVITES_SUMMARY__TRANSACTION_SUMMARY_ID",
                    unique = true),
            Index(
                    value = ["INVITE_SUMMARY_ID"],
                    name = "TRANSACTIONS_INVITES_SUMMARY__INVITE_SUMMARY_ID",
                    unique = true),
            Index(
                    value = ["TO_USER_IDENTITY_ID"],
                    name = "TRANSACTIONS_INVITES_SUMMARY__TO_USER_IDENTITY_ID",
                    unique = false),
            Index(
                    value = ["FROM_USER_IDENTITY_ID"],
                    name = "TRANSACTIONS_INVITES_SUMMARY__FROM_USER_IDENTITY_ID",
                    unique = false)
        ],
        foreignKeys = [
            ForeignKey(
                    entity = TransactionSummary::class,
                    parentColumns = arrayOf("_id"),
                    childColumns = arrayOf("TRANSACTION_SUMMARY_ID"),
                    onDelete = ForeignKey.CASCADE
            ),
            ForeignKey(
                    entity = InviteTransactionSummary::class,
                    parentColumns = arrayOf("_id"),
                    childColumns = arrayOf("INVITE_SUMMARY_ID"),
                    onDelete = ForeignKey.CASCADE
            ),
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
            )
        ]
)
data class TransactionsInvitesSummary(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        var id: Long = 0,

        @ColumnInfo(name = "INVITE_SUMMARY_ID")
        var inviteSummaryID: Long? = null,

        @ColumnInfo(name = "TRANSACTION_SUMMARY_ID")
        var transactionSummaryId: Long? = null,

        @ColumnInfo(name = "TO_USER_IDENTITY_ID")
        var toUserIdentityId: Long? = null,

        @ColumnInfo(name = "FROM_USER_IDENTITY_ID")
        var fromUserIdentityId: Long? = null,

        @ColumnInfo(name = "INVITE_TIME")
        var inviteTime: Long = 0,

        @ColumnInfo(name = "BTC_TX_TIME")
        var btcTxTime: Long = 0,

        @ColumnInfo(name = "TRANSACTION_TX_ID")
        var transactionTxID: String? = null,

        @ColumnInfo(name = "INVITE_TX_ID")
        var inviteTxID: String? = null
)