package app.coinninja.cn.persistance.model

import androidx.room.*
import app.dropbit.annotations.Mockable


@Mockable
@Entity(
        tableName = "WORD",
        indices = [
            Index(
                    value = ["WALLET_ID"],
                    name = "WORD__WALLET_ID",
                    unique = false)
        ],
        foreignKeys = [
            ForeignKey(
                    entity = Wallet::class,
                    parentColumns = arrayOf("_id"),
                    childColumns = arrayOf("WALLET_ID"),
                    onDelete = ForeignKey.NO_ACTION
            )
        ]

)
data class Word(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        var id: Long = 0,
        @ColumnInfo(name = "WALLET_ID")
        var walletId: Long? = null,
        @ColumnInfo(name = "WORD")
        var word: String = "",
        @ColumnInfo(name = "SORT_ORDER")
        var sortOrder: Int = 0
)