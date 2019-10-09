package app.coinninja.cn.persistance.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import app.dropbit.annotations.Mockable


@Mockable
@Entity(
        tableName = "WALLET"
)
data class Wallet(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        var id: Long = 0,

        @ColumnInfo(name = "HD_INDEX")
        var hdIndex: Int = 0,

        @ColumnInfo(name = "USER_ID")
        var userId: Int = 0,

        @ColumnInfo(name = "LAST_SYNC")
        var lastSync: Long = 0,

        @ColumnInfo(name = "INTERNAL_INDEX")
        var internalIndex: Int = 0,

        @ColumnInfo(name = "EXTERNAL_INDEX")
        var externalIndex: Int = 0,

        @ColumnInfo(name = "BALANCE")
        var balance: Long = 0,

        @ColumnInfo(name = "SPENDABLE_BALANCE")
        var spendableBalance: Long = 0,

        @ColumnInfo(name = "BLOCK_TIP")
        var blockTip: Int = 0,

        @ColumnInfo(name = "LAST_USDPRICE")
        var lastUSDPrice: Long = 0,

        @ColumnInfo(name = "PURPOSE")
        var purpose: Int = 0,

        @ColumnInfo(name = "COIN_TYPE")
        var coinType: Int = 0,

        @ColumnInfo(name = "ACCOUNT_INDEX")
        var accountIndex: Int = 0,

        @ColumnInfo(name = "FLAGS")
        var flags: Long = 0
)
