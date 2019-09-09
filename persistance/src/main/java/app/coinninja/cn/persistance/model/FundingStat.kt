package app.coinninja.cn.persistance.model

import androidx.room.*
import app.dropbit.annotations.Mockable

@Mockable
@Entity(
        tableName = "FUNDING_STAT",
        indices = [
            Index(
                    value = ["ADDRESS_ID"],
                    name = "FUNDING_STAT__ADDRESS_ID",
                    unique = false
            ),
            Index(
                    value = ["WALLET_ID"],
                    name = "FUNDING_STAT__WALLET_ID",
                    unique = false
            ),
            Index(
                    value = ["TSID"],
                    name = "FUNDING_STAT__TSID",
                    unique = false
            )
        ],
        foreignKeys = [
            ForeignKey(
                    entity = Address::class,
                    parentColumns = arrayOf("_id"),
                    childColumns = arrayOf("ADDRESS_ID"),
                    onDelete = ForeignKey.CASCADE
            ),
            ForeignKey(
                    entity = Wallet::class,
                    parentColumns = arrayOf("_id"),
                    childColumns = arrayOf("WALLET_ID"),
                    onDelete = ForeignKey.CASCADE
            ),
            ForeignKey(
                    entity = TransactionSummary::class,
                    parentColumns = arrayOf("_id"),
                    childColumns = arrayOf("TSID"),
                    onDelete = ForeignKey.CASCADE
            )
        ]
)
@TypeConverters(
        InputStateConverter::class
)
data class FundingStat(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        var id: Long = 0,
        @ColumnInfo(name = "ADDRESS_ID")
        var addressId: Long? = null,
        @ColumnInfo(name = "WALLET_ID")
        var walletId: Long? = null,
        @ColumnInfo(name = "TSID")
        var tsid: Long? = null,
        @ColumnInfo(name = "ADDR")
        var addr: String = "",
        @ColumnInfo(name = "POSITION")
        var position: Int = 0,
        @ColumnInfo(name = "VALUE")
        var value: Long = 0,
        @ColumnInfo(name = "TX_TIME")
        var txTime: Long = 0,
        @ColumnInfo(name = "STATE")
        var state: InputState = InputState.PENDING,
        @ColumnInfo(name = "FUNDED_TRANSACTION")
        var fundedTransaction: String = ""
)