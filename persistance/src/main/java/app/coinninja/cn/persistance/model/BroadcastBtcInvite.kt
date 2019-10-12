package app.coinninja.cn.persistance.model

import androidx.room.*
import app.coinninja.cn.persistance.converter.BTCStateConverter
import app.dropbit.annotations.Mockable


@Mockable
@Entity(
        tableName = "BROADCAST_BTC_INVITE",
        indices = [
            Index(
                    value = ["INVITE_TRANSACTION_SUMMARY_ID"],
                    name = "BROADCAST_BTC_INVITE__INVITE_TRANSACTION_SUMMARY_ID",
                    unique = true
            ),
            Index(
                    value = ["BROADCAST_TX_ID"],
                    name = "BROADCAST_BTC_INVITE__BROADCAST_TX_ID",
                    unique = true
            )
        ],
        foreignKeys = [
            ForeignKey(
                    entity = InviteTransactionSummary::class,
                    parentColumns = arrayOf("_id"),
                    childColumns = arrayOf("INVITE_TRANSACTION_SUMMARY_ID"),
                    onDelete = ForeignKey.CASCADE
            )
        ]
)
@TypeConverters(
        BTCStateConverter::class
)
data class BroadcastBtcInvite(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        var _id: Long = 0,
        @ColumnInfo(name = "BROADCAST_TX_ID")
        var broadcastTxID: String = "",
        @ColumnInfo(name = "BTC_STATE")
        var btcState: BTCState = BTCState.UNFULFILLED,
        @ColumnInfo(name = "BROADCAST_TO_ADDRESS")
        var broadcastToAddress: String = "",
        @ColumnInfo(name = "INVITE_SERVER_ID")
        var inviteServerID: String = "",
        @ColumnInfo(name = "INVITE_TRANSACTION_SUMMARY_ID")
        var inviteTransactionSummaryID: Long? = null
)
