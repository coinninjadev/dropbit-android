package app.coinninja.cn.persistance.model

import androidx.room.*
import app.dropbit.annotations.Mockable

@Mockable
@Entity(
        tableName = "TRANSACTION_NOTIFICATION",
        indices = [
            Index(
                    value = ["TO_USER_IDENTITY_ID"],
                    name = "TRANSACTION_NOTIFICATION__TO_USER_IDENTITY_ID",
                    unique = false),
            Index(
                    value = ["FROM_USER_IDENTITY_ID"],
                    name = "TRANSACTION_NOTIFICATION__FROM_USER_IDENTITY_ID",
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
            )
        ]
)
data class TransactionNotification(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        var id: Long = 0,

        @ColumnInfo(name = "MEMO")
        var memo: String = "",

        @ColumnInfo(name = "IS_SHARED")
        var isShared: Boolean = false,

        @ColumnInfo(name = "AMOUNT")
        var amount: Long = 0,

        @ColumnInfo(name = "AMOUNT_CURRENCY")
        var amountCurrency: String = "",

        @ColumnInfo(name = "TXID")
        var txid: String = "",

        @ColumnInfo(name = "TO_USER_IDENTITY_ID")
        var toUserIdentityId: Long? = null,

        @ColumnInfo(name = "FROM_USER_IDENTITY_ID")
        var fromUserIdentityId: Long? = null
)