package app.coinninja.cn.persistance.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import app.dropbit.annotations.Mockable
import app.dropbit.commons.currency.BTCCurrency


@Mockable
@Entity(
        tableName = "LIGHTNING_ACCOUNT",
        indices = [
            Index(
                    value = ["SERVER_ID"],
                    name = "LIGHTNING_ACCOUNT__SERVER_ID",
                    unique = true)
        ]
)
data class LightningAccount(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        var id: Long = 0,

        @ColumnInfo(name = "SERVER_ID")
        var serverId: String = "",

        @ColumnInfo(name = "ADDRESS")
        var address: String = "",

        @ColumnInfo(name = "BALANCE")
        var balance: BTCCurrency = BTCCurrency(0L),

        @ColumnInfo(name = "PENDING_IN")
        var pendingIn: BTCCurrency = BTCCurrency(0L),

        @ColumnInfo(name = "PENDING_OUT")
        var pendingOut: BTCCurrency = BTCCurrency(0L),

        @ColumnInfo(name = "CREATED_AT")
        var createdAt: String = "",

        @ColumnInfo(name = "UPDATED_AT")
        var updatedAt: String = ""
)

