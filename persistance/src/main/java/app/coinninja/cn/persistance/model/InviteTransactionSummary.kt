package app.coinninja.cn.persistance.model

import androidx.room.*
import app.coinninja.cn.persistance.converter.BTCStateConverter
import app.coinninja.cn.persistance.converter.SendTypeConverter
import app.dropbit.annotations.Mockable
import app.dropbit.commons.currency.USDCurrency

@Mockable
@Entity(
        tableName = "INVITE_TRANSACTION_SUMMARY",
        indices = [
            Index(
                    value = ["TO_USER_IDENTITY_ID"],
                    name = "INVITE_TRANSACTION_SUMMARY__TO_USER_IDENTITY_ID",
                    unique = false),
            Index(
                    value = ["FROM_USER_IDENTITY_ID"],
                    name = "INVITE_TRANSACTION_SUMMARY__FROM_USER_IDENTITY_ID",
                    unique = false),
            Index(
                    value = ["TRANSACTIONS_INVITES_SUMMARY_ID"],
                    name = "INVITE_TRANSACTION_SUMMARY__TRANSACTIONS_INVITES_SUMMARY_ID",
                    unique = false),
            Index(
                    value = ["WALLET_ID"],
                    name = "INVITE_TRANSACTION_SUMMARY__WALLET_ID",
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
                    entity = TransactionsInvitesSummary::class,
                    parentColumns = arrayOf("_id"),
                    childColumns = arrayOf("TRANSACTIONS_INVITES_SUMMARY_ID"),
                    onDelete = ForeignKey.NO_ACTION
            ),
            ForeignKey(
                    entity = Wallet::class,
                    parentColumns = arrayOf("_id"),
                    childColumns = arrayOf("WALLET_ID"),
                    onDelete = ForeignKey.NO_ACTION
            )
        ]
)
@TypeConverters(
        SendTypeConverter::class,
        BTCStateConverter::class
)
data class InviteTransactionSummary(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        var id: Long = 0,

        @ColumnInfo(name = "TO_USER_IDENTITY_ID")
        var toUserIdentityId: Long? = null,

        @ColumnInfo(name = "FROM_USER_IDENTITY_ID")
        var fromUserIdentityId: Long? = null,

        @ColumnInfo(name = "WALLET_ID")
        var walletId: Long? = null,

        @ColumnInfo(name = "TRANSACTIONS_INVITES_SUMMARY_ID")
        var transactionsInvitesSummaryID: Long? = null,

        @ColumnInfo(name = "TYPE")
        var type: SendType = SendType.RECEIVED,

        @ColumnInfo(name = "BTC_STATE")
        var btcState: BTCState = BTCState.UNFULFILLED,

        @ColumnInfo(name = "SERVER_ID")
        var serverId: String = "",

        @ColumnInfo(name = "BTC_TRANSACTION_ID")
        var btcTransactionId: String = "",

        @ColumnInfo(name = "SENT_DATE")
        var sentDate: Long = 0,

        @ColumnInfo(name = "ADDRESS")
        var address: String = "",

        @ColumnInfo(name = "PUBKEY")
        var pubkey: String = "",

        @ColumnInfo(name = "VALUE_SATOSHIS")
        var valueSatoshis: Long = 0,

        @ColumnInfo(name = "VALUE_FEES_SATOSHIS")
        var valueFeesSatoshis: Long = 0,

        @ColumnInfo(name = "HISTORIC_VALUE")
        var historicValue: USDCurrency = USDCurrency(0)

)