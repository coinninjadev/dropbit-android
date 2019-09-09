package app.coinninja.cn.persistance.model

import androidx.room.*
import app.coinninja.cn.persistance.converter.OutputStateConverter
import app.dropbit.annotations.Mockable

@Mockable
@Entity(
        tableName = "TARGET_STAT",
        indices = [
            Index(
                    value = ["WALLET_ID"],
                    name = "TARGET_STAT__WALLET_ID",
                    unique = false),
            Index(
                    value = ["ADDRESS_ID"],
                    name = "TARGET_STAT__ADDRESS_ID",
                    unique = false),
            Index(
                    value = ["TSID"],
                    name = "TARGET_STAT__TSID",
                    unique = false),
            Index(
                    value = ["FUNDING_ID"],
                    name = "TARGET_STAT__FUNDING_ID",
                    unique = false)
        ],
        foreignKeys = [
            ForeignKey(
                    entity = Wallet::class,
                    parentColumns = arrayOf("_id"),
                    childColumns = arrayOf("WALLET_ID"),
                    onDelete = ForeignKey.NO_ACTION
            ),
            ForeignKey(
                    entity = Address::class,
                    parentColumns = arrayOf("_id"),
                    childColumns = arrayOf("ADDRESS_ID"),
                    onDelete = ForeignKey.NO_ACTION
            ),
            ForeignKey(
                    entity = TransactionSummary::class,
                    parentColumns = arrayOf("_id"),
                    childColumns = arrayOf("TSID"),
                    onDelete = ForeignKey.NO_ACTION
            ),
            ForeignKey(
                    entity = FundingStat::class,
                    parentColumns = arrayOf("_id"),
                    childColumns = arrayOf("FUNDING_ID"),
                    onDelete = ForeignKey.NO_ACTION
            )
        ]
)
@TypeConverters(OutputStateConverter::class)
data class TargetStat(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        var id: Long = 0,

        @ColumnInfo(name = "WALLET_ID")
        var walletId: Long? = null,

        @ColumnInfo(name = "ADDRESS_ID")
        var addressId: Long? = null,

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

        @ColumnInfo(name = "FUNDING_ID")
        var fundingId: Long? = null,

        @ColumnInfo(name = "STATE")
        var state: OutPutState = OutPutState.PENDING

)