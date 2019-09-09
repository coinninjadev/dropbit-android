package app.coinninja.cn.persistance.model

import androidx.room.*
import app.coinninja.cn.persistance.converter.LedgerDirectionConverter
import app.coinninja.cn.persistance.converter.LedgerStatusConverter
import app.coinninja.cn.persistance.converter.LedgerTypeConverter
import app.dropbit.annotations.Mockable
import app.dropbit.commons.currency.BTCCurrency
import java.util.*


@Mockable
@Entity(
        tableName = "LIGHTNING_INVOICE",
        indices = [
            Index(
                    value = ["ACCOUNT_ID"],
                    name = "LIGHTNING_INVOICE__ACCOUNT_ID",
                    unique = false),
            Index(
                    value = ["SERVER_ID"],
                    name = "LIGHTNING_INVOICE__SERVER_ID",
                    unique = true)
        ],
        foreignKeys = [
            ForeignKey(
                    entity = LightningAccount::class,
                    parentColumns = arrayOf("_id"),
                    childColumns = arrayOf("ACCOUNT_ID"),
                    onDelete = ForeignKey.NO_ACTION
            )
        ]
)

@TypeConverters(
        LedgerDirectionConverter::class,
        LedgerTypeConverter::class,
        LedgerStatusConverter::class
)
data class LightningInvoice(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        var id: Long = 0,

        @ColumnInfo(name = "SERVER_ID")
        var serverId: String = "",

        @ColumnInfo(name = "ACCOUNT_ID")
        var accountId: Long? = null,

        @ColumnInfo(name = "CREATED_AT")
        var createdAt: Date? = null,

        @ColumnInfo(name = "UPDATED_AT")
        var updatedAt: Date? = null,

        @ColumnInfo(name = "EXPIRES_AT")
        var expiresAt: Date? = null,

        @ColumnInfo(name = "STATUS")
        var status: LedgerStatus? = null,

        @ColumnInfo(name = "TYPE")
        var type: LedgerType? = null,

        @ColumnInfo(name = "DIRECTION")
        var direction: LedgerDirection? = null,

        @ColumnInfo(name = "GENERATED")
        var isGenerated: Boolean = false,

        @ColumnInfo(name = "VALUE")
        var value: BTCCurrency = BTCCurrency(0),

        @ColumnInfo(name = "NETWORK_FEE")
        var networkFee: BTCCurrency = BTCCurrency(0),

        @ColumnInfo(name = "PROCESSING_FEE")
        var processingFee: BTCCurrency = BTCCurrency(0),

        @ColumnInfo(name = "ADD_INDEX")
        var addIndex: Int? = null,

        @ColumnInfo(name = "MEMO")
        var memo: String = "",

        @ColumnInfo(name = "REQUEST")
        var request: String = "",

        @ColumnInfo(name = "ERROR")
        var error: String = "",

        @ColumnInfo(name = "IS_HIDDEN")
        var isHidden: Boolean = true
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LightningInvoice

        if (id != other.id) return false
        if (serverId != other.serverId) return false
        if (accountId != other.accountId) return false
        if (createdAt?.time != other.createdAt?.time) return false
        if (updatedAt?.time != other.updatedAt?.time) return false
        if (expiresAt?.time != other.expiresAt?.time) return false
        if (status != other.status) return false
        if (type != other.type) return false
        if (direction != other.direction) return false
        if (isGenerated != other.isGenerated) return false
        if (value.toLong() != other.value.toLong()) return false
        if (networkFee.toLong() != other.networkFee.toLong()) return false
        if (processingFee.toLong() != other.processingFee.toLong()) return false
        if (addIndex != other.addIndex) return false
        if (memo != other.memo) return false
        if (request != other.request) return false
        if (error != other.error) return false
        if (isHidden != other.isHidden) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + serverId.hashCode()
        result = 31 * result + (accountId?.hashCode() ?: 0)
        result = 31 * result + (createdAt?.hashCode() ?: 0)
        result = 31 * result + (updatedAt?.hashCode() ?: 0)
        result = 31 * result + (expiresAt?.hashCode() ?: 0)
        result = 31 * result + (status?.hashCode() ?: 0)
        result = 31 * result + (type?.hashCode() ?: 0)
        result = 31 * result + (direction?.hashCode() ?: 0)
        result = 31 * result + isGenerated.hashCode()
        result = 31 * result + value.hashCode()
        result = 31 * result + networkFee.hashCode()
        result = 31 * result + processingFee.hashCode()
        result = 31 * result + (addIndex ?: 0)
        result = 31 * result + memo.hashCode()
        result = 31 * result + request.hashCode()
        result = 31 * result + error.hashCode()
        result = 31 * result + isHidden.hashCode()
        return result
    }
}