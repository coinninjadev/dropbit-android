package app.coinninja.cn.persistance.model

import androidx.room.*
import app.coinninja.cn.persistance.converter.MemPoolStateConverter
import app.dropbit.annotations.Mockable
import app.dropbit.commons.currency.USDCurrency

@Mockable
@Entity(
        tableName = "TRANSACTION_SUMMARY",
        indices = [
            Index(
                    value = ["WALLET_ID"],
                    name = "TRANSACTION__WALLET_ID",
                    unique = false),
            Index(
                    value = ["TRANSACTIONS_INVITES_SUMMARY_ID"],
                    name = "TRANSACTION__TRANSACTIONS_INVITES_SUMMARY_ID",
                    unique = true),
            Index(
                    value = ["TXID"],
                    name = "TRANSACTION__TXID",
                    unique = true)
        ],
        foreignKeys = [
            ForeignKey(
                    entity = Wallet::class,
                    parentColumns = arrayOf("_id"),
                    childColumns = arrayOf("WALLET_ID"),
                    onDelete = ForeignKey.NO_ACTION
            ),
            ForeignKey(
                    entity = TransactionsInvitesSummary::class,
                    parentColumns = arrayOf("_id"),
                    childColumns = arrayOf("TRANSACTIONS_INVITES_SUMMARY_ID"),
                    onDelete = ForeignKey.NO_ACTION
            )
        ]
)
@TypeConverters(
        MemPoolStateConverter::class
)
data class TransactionSummary(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        var id: Long = 0,

        @ColumnInfo(name = "WALLET_ID")
        var walletId: Long? = null,

        @ColumnInfo(name = "TRANSACTIONS_INVITES_SUMMARY_ID")
        var transactionsInvitesSummaryId: Long? = null,

        @ColumnInfo(name = "TXID")
        var txid: String = "",

        @ColumnInfo(name = "SOUGHT_NOTIFICATION")
        var soughtNotification: Boolean = false,

        @ColumnInfo(name = "FEE")
        var fee: Long? = null,

        @ColumnInfo(name = "TX_TIME")
        var txTime: Long? = null,

        @ColumnInfo(name = "NUM_CONFIRMATIONS")
        var numConfirmations: Int = 0,

        @ColumnInfo(name = "BLOCKHASH")
        var blockhash: String = "",

        @ColumnInfo(name = "NUM_INPUTS")
        var numInputs: Int = 0,

        @ColumnInfo(name = "NUM_OUTPUTS")
        var numOutputs: Int = 0,

        @ColumnInfo(name = "BLOCKHEIGHT")
        var blockheight: Int? = null,

        @ColumnInfo(name = "HISTORIC_PRICE")
        var historicPrice: USDCurrency = USDCurrency(0),

        @ColumnInfo(name = "TRANSACTION_NOTIFICATION_ID")
        var transactionNotificationId: Long? = null,

        @ColumnInfo(name = "MEM_POOL_STATE")
        var memPoolState: MemPoolState = MemPoolState.INIT

)