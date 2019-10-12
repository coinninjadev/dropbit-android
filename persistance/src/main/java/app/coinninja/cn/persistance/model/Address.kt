package app.coinninja.cn.persistance.model

import androidx.room.*
import app.dropbit.annotations.Mockable


@Mockable
@Entity(
        tableName = "ADDRESS",
        indices = [
            Index(
                    value = ["WALLET_ID"],
                    name = "ADDRESS__WALLET_ID",
                    unique = false
            ),
            Index(
                    value = ["ADDRESS"],
                    name = "ADDRESS__ADDRESS",
                    unique = true
            )
        ],
        foreignKeys = [
            ForeignKey(
                    entity = Wallet::class,
                    parentColumns = arrayOf("_id"),
                    childColumns = arrayOf("WALLET_ID"),
                    onDelete = ForeignKey.CASCADE
            )
        ]
)
data class Address(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        var id: Long = 0,
        @ColumnInfo(name = "WALLET_ID")
        var walletId: Long = 0,
        @ColumnInfo(name = "ADDRESS")
        var address: String = "",
        @ColumnInfo(name = "INDEX")
        var index: Int = 0,
        @ColumnInfo(name = "CHANGE_INDEX")
        var changeIndex: Int = 0
)